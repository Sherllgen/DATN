import { useState, useEffect } from 'react';
import EventSource from 'react-native-sse';
import { ChargingMonitorData } from '@/types/charging.types';
import { useAuthStore } from '@/contexts/auth.store';

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

interface UseChargingMonitorResult {
    monitorData: ChargingMonitorData | null;
    isConnected: boolean;
    isSessionEnded: boolean;
    error: string | null;
}

export const useChargingMonitor = (sessionId: number | null): UseChargingMonitorResult => {
    const [monitorData, setMonitorData] = useState<ChargingMonitorData | null>(null);
    const [isConnected, setIsConnected] = useState<boolean>(false);
    const [isSessionEnded, setIsSessionEnded] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!sessionId) {
            return;
        }

        // Reset state on new connection
        setIsConnected(false);
        setIsSessionEnded(false);
        setError(null);
        setMonitorData(null);

        let eventSource: EventSource<"meter-update" | "session-ended"> | null = null;
        let retryCount = 0;
        const MAX_RETRIES = 10;
        const BASE_DELAY = 2000;
        let isUnmounted = false;

        const connect = () => {
            if (isUnmounted || isSessionEnded) return;

            const accessToken = useAuthStore.getState().accessToken;
            const url = `${API_BACKEND_URL}/api/v1/charging/sessions/${sessionId}/monitor-stream`;

            try {
                if (eventSource) {
                    eventSource.close();
                }

                eventSource = new EventSource<"meter-update" | "session-ended">(url, {
                    headers: {
                        Authorization: `Bearer ${accessToken}`
                    }
                });

                eventSource.addEventListener("open", () => {
                    setIsConnected(true);
                    setError(null);
                    retryCount = 0; // Reset on successful connection
                });

                eventSource.addEventListener("meter-update", (event) => {
                    if (event.data) {
                        try {
                            const data: ChargingMonitorData = JSON.parse(event.data);
                            setMonitorData(data);
                        } catch (err) {
                            console.error("Failed to parse meter-update event", err);
                        }
                    }
                });

                eventSource.addEventListener("session-ended", (event) => {
                    if (event.data) {
                        try {
                            const data: ChargingMonitorData = JSON.parse(event.data);
                            setMonitorData(data);
                        } catch (err) {
                            console.error("Failed to parse session-ended event", err);
                        }
                    }
                    setIsSessionEnded(true);
                    if (eventSource) {
                        eventSource.close();
                    }
                });

                eventSource.addEventListener("error", (event) => {
                    console.error("SSE Error:", event);
                    setIsConnected(false);

                    // Stop built-in reconnect to avoid using a stale token
                    if (eventSource) {
                        eventSource.close();
                    }

                    if (retryCount < MAX_RETRIES) {
                        const delay = BASE_DELAY * Math.pow(2, retryCount);
                        retryCount++;
                        setTimeout(() => {
                            if (!isUnmounted && !isSessionEnded) {
                                connect();
                            }
                        }, delay);
                    } else {
                        setError("Connection lost. Please go back and re-open the charging screen.");
                    }
                });

            } catch (err) {
                console.error("Failed to initialize SSE", err);
                setError("Failed to initialize charging monitor stream.");
            }
        };

        connect();

        return () => {
            isUnmounted = true;
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [sessionId]);

    return { monitorData, isConnected, isSessionEnded, error };
};
