import React from "react";
import { View, Text } from "react-native";

interface StatItemProps {
    value: string | number;
    label: string;
    showBorderRight?: boolean;
    showBorderBottom?: boolean;
}

const StatItem = ({ value, label, showBorderRight, showBorderBottom }: StatItemProps) => (
    <View
        className={`flex-1 p-6 items-center justify-center 
            ${showBorderRight ? 'border-r border-white/5' : ''} 
            ${showBorderBottom ? 'border-b border-white/5' : ''}`}
    >
        <Text className="text-white text-2xl font-bold mb-2">{value}</Text>
        <Text className="text-text-secondary text-sm">{label}</Text>
    </View>
);

interface StatsGridProps {
    elapsedTime: string;
    batteryPlaceholder: string;
    chargingRatePerKwh: number;
    estimatedCost: number;
}

export default function StatsGrid({ elapsedTime, batteryPlaceholder, chargingRatePerKwh, estimatedCost }: StatsGridProps) {
    return (
        <View className="bg-[#1A202C]/60 rounded-[32px] border border-white/5 overflow-hidden mx-4 mt-6">
            <View className="flex-row">
                <StatItem value={elapsedTime} label="Charging Time" showBorderRight showBorderBottom />
                <StatItem value={batteryPlaceholder} label="Battery" showBorderBottom />
            </View>
            <View className="flex-row">
                <StatItem value={`${chargingRatePerKwh.toLocaleString()} VND`} label="Rate" showBorderRight />
                <StatItem value={`${estimatedCost.toLocaleString()} VND`} label="Total Fees" />
            </View>
        </View>
    );
}
