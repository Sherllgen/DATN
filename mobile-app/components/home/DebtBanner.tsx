import React from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { Feather } from "@expo/vector-icons";
import { useRouter } from "expo-router";

interface DebtBannerProps {
    isVisible: boolean;
    unpaidCount: number;
}

export default function DebtBanner({ isVisible, unpaidCount }: DebtBannerProps) {
    const router = useRouter();

    if (!isVisible || unpaidCount === 0) return null;

    return (
        <View className="mb-6 rounded-2xl bg-red-500/10 border border-red-500/20 overflow-hidden">
            <View className="p-4 flex-row items-center">
                <View className="w-10 h-10 rounded-full bg-red-500/20 items-center justify-center mr-3">
                    <Feather name="alert-triangle" color="#EF4444" size={20} />
                </View>
                <View className="flex-1">
                    <Text className="text-white font-bold text-base mb-1">Payment Required</Text>
                    <Text className="text-gray-300 text-xs leading-tight">
                        You have {unpaidCount} unpaid invoice{unpaidCount > 1 ? "s" : ""}. Please settle your balance to continue using EV-Go services.
                    </Text>
                </View>
            </View>
            
            <TouchableOpacity 
                activeOpacity={0.7}
                className="bg-red-500/20 py-3 px-4 flex-row justify-between items-center border-t border-red-500/10"
                onPress={() => router.push("/(tabs)/payment" as any)}
            >
                <Text className="text-red-400 font-semibold text-sm">Pay Now</Text>
                <Feather name="chevron-right" color="#F87171" size={16} />
            </TouchableOpacity>
        </View>
    );
}
