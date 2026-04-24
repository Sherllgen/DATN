import React from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { useChargingStore } from "@/stores/chargingStore";
import { ChargingSessionStatus } from "@/types/charging.types";

const ACTIVE_STATUSES = new Set([
    ChargingSessionStatus.CHARGING,
    ChargingSessionStatus.PREPARING,
    ChargingSessionStatus.SUSPENDED_EV,
    ChargingSessionStatus.SUSPENDED_EVSE,
]);

export interface ActiveChargingNotificationProps {
    className?: string;
}

export default function ActiveChargingNotification({ className }: ActiveChargingNotificationProps) {
    const activeSession = useChargingStore((state) => state.activeSession);
    const lastMonitorData = useChargingStore((state) => state.lastMonitorData);

    if (!activeSession || !ACTIVE_STATUSES.has(activeSession.status)) {
        return null;
    }

    const consumedKwh = lastMonitorData?.consumedKwh ?? 0;
    const statusLabel =
        activeSession.status === ChargingSessionStatus.CHARGING
            ? "Charging in progress"
            : activeSession.status === ChargingSessionStatus.PREPARING
            ? "Preparing..."
            : "Suspended";

    return (
        <TouchableOpacity
            activeOpacity={0.7}
            onPress={() => router.push("/charging")}
            className={[
                "bg-black/85 border border-white/15 rounded-3xl p-5 flex-row justify-between items-center shadow-lg shadow-white/15",
                className,
            ]
                .filter(Boolean)
                .join(" ")}
        >
            {/* kWh Consumed Badge */}
            <View className="flex-row items-center flex-1">
                <View className="bg-secondary/20 rounded-2xl w-14 h-14 items-center justify-center mr-3">
                    <Ionicons name="flash" size={22} color="#00A452" />
                </View>
                <View>
                    <Text className="text-white font-bold text-xl">
                        {consumedKwh.toFixed(2)}
                    </Text>
                    <Text className="text-[#A0AEC0] text-xs mt-0.5">kWh used</Text>
                </View>
            </View>

            {/* Status */}
            <View className="items-end justify-center">
                <Text className="text-[#A0AEC0] text-[13px] mb-1">Status</Text>
                <Text className="text-white font-bold text-base">{statusLabel}</Text>
            </View>
        </TouchableOpacity>
    );
}
