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

        const accessToken = useAuthStore.getState().accessToken;
        const url = `${API_BACKEND_URL}/api/v1/charging/sessions/${sessionId}/monitor-stream`;

        let eventSource: EventSource<"meter-update" | "session-ended"> | null = null;

        try {
            eventSource = new EventSource<"meter-update" | "session-ended">(url, {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            });

            eventSource.addEventListener("open", () => {
                setIsConnected(true);
                setError(null);
            });

            eventSource.addEventListener("meter-update", (event: any) => {
                if (event.data) {
                    try {
                        const data: ChargingMonitorData = JSON.parse(event.data);
                        setMonitorData(data);
                    } catch (err) {
                        console.error("Failed to parse meter-update event", err);
                    }
                }
            });

            eventSource.addEventListener("session-ended", (event: any) => {
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

            eventSource.addEventListener("error", (event: any) => {
                console.error("SSE Error:", event);
                setError("Failed to connect to charging monitor stream.");
                setIsConnected(false);
            });

        } catch (err) {
            console.error("Failed to initialize SSE", err);
            setError("Failed to initialize charging monitor stream.");
        }

        return () => {
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [sessionId]);

    return { monitorData, isConnected, isSessionEnded, error };
};
