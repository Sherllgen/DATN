import React, { useState, useEffect } from "react";
import { View, ScrollView, ActivityIndicator, Alert, BackHandler } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { Toast } from "toastify-react-native";

import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import ChargingIndicator from "@/components/charging/ChargingIndicator";
import StatsGrid from "@/components/charging/StatsGrid";
import ChargingCompleteModal from "@/components/charging/ChargingCompleteModal";

import { startCharging, stopCharging, getChargingSession } from "@/apis/chargingApi";
import { useChargingMonitor } from "@/hooks/useChargingMonitor";
import { useChargingStore } from "@/stores/chargingStore";
import { ChargingSessionStatus } from "@/types/charging.types";

export default function ChargingPage() {
    const params = useLocalSearchParams<{ sessionId?: string, portId?: string, bookingId?: string }>();
    const router = useRouter();

    const activeSession = useChargingStore((state) => state.activeSession);
    const setActiveSession = useChargingStore((state) => state.setActiveSession);
    const clearSession = useChargingStore((state) => state.clearSession);

    const [isStarting, setIsStarting] = useState(false);
    const [isStopping, setIsStopping] = useState(false);
    const [showCompleteModal, setShowCompleteModal] = useState(false);
    const [elapsedTime, setElapsedTime] = useState("00:00:00");

    const { monitorData, isSessionEnded, isConnected, error } = useChargingMonitor(activeSession?.id ?? null);

    // Initialize session
    useEffect(() => {
        const initSession = async () => {
            try {
                if (params.sessionId) {
                    setIsStarting(true);
                    const session = await getChargingSession(Number(params.sessionId));
                    setActiveSession(session);
                } else if (params.portId) {
                    setIsStarting(true);
                    const session = await startCharging({
                        portId: Number(params.portId),
                        bookingId: params.bookingId ? Number(params.bookingId) : undefined
                    });
                    setActiveSession(session);
                }
            } catch (err: any) {
                console.error("Failed to init charging session:", err);
                Toast.error(err?.response?.data?.message || "Failed to initialize charging session");
                router.back();
            } finally {
                setIsStarting(false);
            }
        };

        if (!activeSession && (params.sessionId || params.portId)) {
            initSession();
        }
    }, [params.sessionId, params.portId, params.bookingId]);

    // Timer logic
    useEffect(() => {
        let interval: ReturnType<typeof setInterval>;
        
        if (activeSession?.startTime && !isSessionEnded && activeSession.status !== ChargingSessionStatus.COMPLETED) {
            // Append Z to implicitly make it UTC if dates from backend are UTC without timezone info
            // Ensure proper parsing of startTime string depending on backend format
            const startTimeStr = activeSession.startTime.endsWith('Z') ? activeSession.startTime : `${activeSession.startTime}Z`;
            const start = new Date(startTimeStr).getTime();
            
            interval = setInterval(() => {
                const now = new Date().getTime();
                const diff = Math.max(0, now - start);
                
                const hours = Math.floor((diff / (1000 * 60 * 60)) % 24);
                const minutes = Math.floor((diff / 1000 / 60) % 60);
                const seconds = Math.floor((diff / 1000) % 60);
                
                setElapsedTime(
                    `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`
                );
            }, 1000);
        }

        return () => {
            if (interval) clearInterval(interval);
        };
    }, [activeSession?.startTime, isSessionEnded, activeSession?.status]);

    // Handle session end
    useEffect(() => {
        if (activeSession?.status === ChargingSessionStatus.FAULTED) {
            Alert.alert(
                "Charging Failed",
                "The charging session encountered an error and was terminated.",
                [{ text: "OK", onPress: () => {
                    clearSession();
                    router.replace("/");
                }}]
            );
        } else if (isSessionEnded || activeSession?.status === ChargingSessionStatus.COMPLETED) {
            setShowCompleteModal(true);
        }
    }, [isSessionEnded, activeSession?.status]);

    // Handle hardware back button
    useEffect(() => {
        const onBackPress = () => {
            if (activeSession && activeSession.status !== ChargingSessionStatus.COMPLETED && !isSessionEnded) {
                Alert.alert(
                    "Warning",
                    "Please stop the charging session before leaving this page.",
                    [{ text: "OK" }]
                );
                return true; // Prevent default behavior
            }
            return false; // Allow default behavior
        };

        const subscription = BackHandler.addEventListener('hardwareBackPress', onBackPress);

        return () => {
            subscription.remove();
        };
    }, [activeSession?.status, isSessionEnded]);

    const handleStop = async () => {
        if (!activeSession?.id) return;

        Alert.alert(
            "Stop Charging?",
            "This action will terminate your current session and generate an invoice. Do you want to proceed?",
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Stop",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            setIsStopping(true);
                            await stopCharging({ sessionId: activeSession.id! });
                            // State isStopping remains true to indicate we're waiting for the station to fully stop and invoice to be generated.
                        } catch (err: unknown) {
                            console.error("Failed to stop charging:", err);
                            const error = err as any;
                            Toast.error(error?.response?.data?.message || "Failed to stop charging");
                            setIsStopping(false);
                        }
                    }
                }
            ]
        );
    };

    // Robust Polling mechanism to detect actual stop and invoice generated
    useEffect(() => {
        let pollInterval: ReturnType<typeof setInterval>;
        let attempts = 0;
        const MAX_ATTEMPTS = 30;
        
        if (isStopping && activeSession?.id) {
            pollInterval = setInterval(async () => {
                attempts++;
                if (attempts > MAX_ATTEMPTS) {
                    clearInterval(pollInterval);
                    setIsStopping(false);
                    Toast.error("Request timed out. Please try again.");
                    return;
                }
                
                try {
                    const session = await getChargingSession(activeSession.id);
                    // Also check if status on backend became COMPLETED. 
                    if (session.status === ChargingSessionStatus.COMPLETED) {
                        setIsStopping(false);
                        setShowCompleteModal(true);
                        setActiveSession(session);
                        clearInterval(pollInterval);
                    }
                } catch(err) {
                    console.log("Polling error when stopping:", err);
                }
            }, 2000); // Poll every 2 seconds until backend confirms completion
        }

        return () => {
            if (pollInterval) clearInterval(pollInterval);
        }
    }, [isStopping, activeSession?.id]);

    // Error handling from SSE
    useEffect(() => {
        if (error) {
            Toast.error(error);
        }
    }, [error]);

    const handleDismissModal = () => {
        setShowCompleteModal(false);
        useChargingStore.getState().clearMonitorData();
        clearSession();
        router.replace("/");
    };

    // Determine values to display
    const currentStatus = monitorData?.status || activeSession?.status || ChargingSessionStatus.PREPARING;
    const consumedKwh = monitorData?.consumedKwh || 0;
    const rate = monitorData?.chargingRatePerKwh || 0;
    const cost = monitorData?.estimatedCost || 0;

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }}>
                <AppHeader
                    title="Charging"
                    showBack
                />

                {isStarting ? (
                    <View className="flex-1 items-center justify-center">
                        <ActivityIndicator color="#00A452" size="large" />
                    </View>
                ) : (
                    <>
                        <ScrollView
                            style={{ flex: 1 }}
                            className="px-4"
                            showsVerticalScrollIndicator={false}
                            contentContainerStyle={{ paddingBottom: 0 }}
                            keyboardShouldPersistTaps="handled"
                        >
                            {/* Central Indicator */}
                            <ChargingIndicator
                                consumedKwh={consumedKwh}
                                status={currentStatus}
                            />

                            {/* Stats Grid */}
                            <StatsGrid
                                elapsedTime={elapsedTime}
                                batteryPlaceholder="---"
                                chargingRatePerKwh={rate}
                                estimatedCost={cost}
                            />
                        </ScrollView>

                        {/* Bottom Action Button */}
                        <View className="px-6 pt-6 pb-2">
                            <Button
                                variant="primary"
                                onPress={handleStop}
                                className="w-full"
                                style={{ height: 56 }}
                                loading={isStopping}
                                disabled={isStopping || isSessionEnded || currentStatus === ChargingSessionStatus.COMPLETED}
                            >
                                Stop Charging
                            </Button>
                        </View>
                    </>
                )}
            </SafeAreaView>

            <ChargingCompleteModal 
                showModal={showCompleteModal}
                totalCost={cost}
                consumedKwh={consumedKwh}
                sessionId={activeSession?.id}
                onDismiss={handleDismissModal}
            />
        </GradientBackground>
    );
}

