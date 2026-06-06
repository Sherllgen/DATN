import { useState, useEffect, useRef, useCallback } from 'react';
import EventSource from 'react-native-sse';
import * as SecureStore from 'expo-secure-store';
import axios from 'axios';
import { ChargingMonitorData } from '@/types/charging.types';
import { useAuthStore, SECURE_KEY_ACCESS_TOKEN, SECURE_KEY_REFRESH_TOKEN } from '@/contexts/auth.store';
import { useChargingStore } from '@/stores/chargingStore';
import { getLatestMeterValue } from '@/apis/chargingApi';

// ─── Constants ────────────────────────────────────────────────────────────────

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

/** Proactively refresh the JWT if it expires within this window (ms). */
const PROACTIVE_REFRESH_THRESHOLD_MS = 15 * 60 * 1000; // 15 minutes

/** SSE reconnect: max attempts before falling back to polling. */
const MAX_SSE_RETRIES = 5;

/** SSE reconnect base delay (exponential back-off). */
const SSE_BASE_DELAY_MS = 2_000;

/** Polling interval when SSE is fully unavailable. */
const POLL_INTERVAL_MS = 5_000;

// ─── JWT helpers ──────────────────────────────────────────────────────────────

/**
 * Decode a JWT payload without verifying the signature.
 * Returns null if the token is malformed.
 */
function decodeJwtExpiry(token: string): number | null {
    try {
        const parts = token.split('.');
        if (parts.length !== 3) return null;
        const payload = JSON.parse(atob(parts[1]));
        return typeof payload.exp === 'number' ? payload.exp * 1000 : null;
    } catch {
        return null;
    }
}

/**
 * Returns true if the token expires within `thresholdMs` from now.
 */
function isTokenExpiringSoon(token: string, thresholdMs: number): boolean {
    const expiry = decodeJwtExpiry(token);
    if (expiry === null) return false; // Cannot decode → assume still valid
    return Date.now() + thresholdMs >= expiry;
}

/**
 * Calls the backend refresh endpoint, persists the new token pair, and
 * returns the fresh access token. Throws on failure.
 */
async function refreshAccessToken(): Promise<string> {
    const storedRefreshToken = await SecureStore.getItemAsync(SECURE_KEY_REFRESH_TOKEN);
    if (!storedRefreshToken) {
        throw new Error('No refresh token available for SSE pre-flight refresh');
    }

    const refreshRes = await axios.post(
        `${API_BACKEND_URL}/api/v1/auth/refresh`,
        { refreshToken: storedRefreshToken },
        { withCredentials: true }
    );

    const { accessToken: newAccessToken, refreshToken: newRefreshToken } = refreshRes.data.data;
    await useAuthStore.getState().saveTokens(newAccessToken, newRefreshToken);
    return newAccessToken;
}

// ─── Public interface ─────────────────────────────────────────────────────────

export interface UseChargingMonitorResult {
    monitorData: ChargingMonitorData | null;
    isConnected: boolean;
    isSessionEnded: boolean;
    error: string | null;
    isPolling: boolean;
}

// ─── Hook ─────────────────────────────────────────────────────────────────────

export const useChargingMonitor = (sessionId: number | null): UseChargingMonitorResult => {
    // Initialise from cache so re-entering the screen shows last values instantly.
    const cachedData = useChargingStore.getState().lastMonitorData;

    const [monitorData, setMonitorData] = useState<ChargingMonitorData | null>(cachedData);
    const [isConnected, setIsConnected] = useState<boolean>(false);
    const [isSessionEnded, setIsSessionEnded] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [isPolling, setIsPolling] = useState<boolean>(false);

    // Mutable refs allow closure-based callbacks to read current values without
    // causing the main useEffect to re-run.
    const isUnmountedRef = useRef<boolean>(false);
    const isSessionEndedRef = useRef<boolean>(false);
    const retryCountRef = useRef<number>(0);
    const eventSourceRef = useRef<EventSource<'meter-update' | 'session-ended'> | null>(null);
    const pollTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const retryTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // ── Helpers ────────────────────────────────────────────────────────────────

    const persistData = useCallback((data: ChargingMonitorData) => {
        setMonitorData(data);
        useChargingStore.getState().setLastMonitorData(data);
    }, []);

    const closeEventSource = useCallback(() => {
        if (eventSourceRef.current) {
            eventSourceRef.current.close();
            eventSourceRef.current = null;
        }
    }, []);

    const stopPolling = useCallback(() => {
        if (pollTimerRef.current) {
            clearInterval(pollTimerRef.current);
            pollTimerRef.current = null;
        }
        setIsPolling(false);
    }, []);

    // ── Polling fallback ───────────────────────────────────────────────────────

    const startPolling = useCallback((sId: number) => {
        if (pollTimerRef.current) return; // Already polling
        setIsPolling(true);
        setIsConnected(false);

        pollTimerRef.current = setInterval(async () => {
            if (isUnmountedRef.current || isSessionEndedRef.current) {
                stopPolling();
                return;
            }
            try {
                const data = await getLatestMeterValue(sId);
                persistData(data);
            } catch (pollError) {
                console.warn('[useChargingMonitor] Polling error:', pollError);
            }
        }, POLL_INTERVAL_MS);
    }, [persistData, stopPolling]);

    // ── SSE connection ─────────────────────────────────────────────────────────

    const connect = useCallback(async (sId: number) => {
        if (isUnmountedRef.current || isSessionEndedRef.current) return;

        // ── Step 1: Proactive token refresh ──────────────────────────────────
        let accessToken = useAuthStore.getState().accessToken;

        if (accessToken && isTokenExpiringSoon(accessToken, PROACTIVE_REFRESH_THRESHOLD_MS)) {
            console.log('[useChargingMonitor] Token expiring soon — proactive refresh before SSE open');
            try {
                accessToken = await refreshAccessToken();
            } catch (refreshErr) {
                console.error('[useChargingMonitor] Proactive refresh failed:', refreshErr);
                // Fall through — attempt SSE with stale token; the 401 handler below will retry.
                accessToken = useAuthStore.getState().accessToken;
            }
        }

        // ── Step 2: Open SSE stream ───────────────────────────────────────────
        const url = `${API_BACKEND_URL}/api/v1/charging/sessions/${sId}/monitor-stream`;

        try {
            closeEventSource();

            const es = new EventSource<'meter-update' | 'session-ended'>(url, {
                headers: { Authorization: `Bearer ${accessToken}` },
            });
            eventSourceRef.current = es;

            es.addEventListener('open', () => {
                if (isUnmountedRef.current) return;
                setIsConnected(true);
                setIsPolling(false);
                setError(null);
                stopPolling(); // Stop any active polling — SSE is back
                retryCountRef.current = 0;
                console.log('[useChargingMonitor] SSE connected');
            });

            es.addEventListener('meter-update', (event) => {
                if (!event.data || isUnmountedRef.current) return;
                try {
                    const data: ChargingMonitorData = JSON.parse(event.data);
                    persistData(data);
                } catch (parseErr) {
                    console.error('[useChargingMonitor] Failed to parse meter-update:', parseErr);
                }
            });

            es.addEventListener('session-ended', (event) => {
                if (event.data) {
                    try {
                        const data: ChargingMonitorData = JSON.parse(event.data);
                        persistData(data);
                    } catch (parseErr) {
                        console.error('[useChargingMonitor] Failed to parse session-ended:', parseErr);
                    }
                }
                isSessionEndedRef.current = true;
                setIsSessionEnded(true);
                closeEventSource();
                stopPolling();
            });

            es.addEventListener('error', async (event: any) => {
                if (isUnmountedRef.current || isSessionEndedRef.current) return;
                console.error('[useChargingMonitor] SSE error:', event);
                setIsConnected(false);
                closeEventSource();

                // ── Step 3: 401 detection & token refresh ─────────────────────
                const httpStatus: number | undefined = event?.xhrStatus ?? event?.status;
                if (httpStatus === 401) {
                    console.log('[useChargingMonitor] SSE received 401 — refreshing token then reconnecting');
                    try {
                        await refreshAccessToken();
                    } catch (refreshErr) {
                        console.error('[useChargingMonitor] Token refresh after 401 failed:', refreshErr);
                        await useAuthStore.getState().logout();
                        setError('Session expired. Please log in again.');
                        return;
                    }
                    // Retry immediately after a successful token refresh (no back-off needed)
                    if (!isUnmountedRef.current && !isSessionEndedRef.current) {
                        connect(sId);
                    }
                    return;
                }

                // ── Step 4: Exponential back-off reconnect ────────────────────
                if (retryCountRef.current < MAX_SSE_RETRIES) {
                    const delay = SSE_BASE_DELAY_MS * Math.pow(2, retryCountRef.current)
                        + Math.random() * 1_000;
                    retryCountRef.current++;
                    console.log(
                        `[useChargingMonitor] Reconnect attempt ${retryCountRef.current}/${MAX_SSE_RETRIES} in ${Math.round(delay)}ms`
                    );
                    retryTimerRef.current = setTimeout(() => {
                        if (!isUnmountedRef.current && !isSessionEndedRef.current) {
                            connect(sId);
                        }
                    }, delay);
                } else {
                    // ── Step 5: Polling fallback ──────────────────────────────
                    console.warn('[useChargingMonitor] Max SSE retries reached — switching to polling fallback');
                    startPolling(sId);
                }
            });
        } catch (initErr) {
            console.error('[useChargingMonitor] Failed to initialise SSE:', initErr);
            // Start polling immediately on init failure
            startPolling(sId);
        }
    }, [closeEventSource, persistData, startPolling, stopPolling]);

    // ── Main effect ────────────────────────────────────────────────────────────

    useEffect(() => {
        if (!sessionId) return;

        isUnmountedRef.current = false;
        isSessionEndedRef.current = false;
        retryCountRef.current = 0;

        setIsConnected(false);
        setIsSessionEnded(false);
        setIsPolling(false);
        setError(null);

        connect(sessionId);

        return () => {
            isUnmountedRef.current = true;
            // Clear pending retry timer
            if (retryTimerRef.current) {
                clearTimeout(retryTimerRef.current);
                retryTimerRef.current = null;
            }
            closeEventSource();
            stopPolling();
        };
    }, [sessionId]); // eslint-disable-line react-hooks/exhaustive-deps
    // ^ connect/closeEventSource/stopPolling are stable via useCallback; sessionId is the
    //   only real dependency that should re-run the full setup.

    return { monitorData, isConnected, isSessionEnded, error, isPolling };
};
