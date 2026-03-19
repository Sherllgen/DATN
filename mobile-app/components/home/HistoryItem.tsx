import React from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { AppColors } from "@/constants/theme";

export interface HistoryItemProps {
    date: string;
    duration: string;
    energy: number;
    onPress?: () => void;
}

export default function HistoryItem({
    date,
    duration,
    energy,
    onPress,
}: HistoryItemProps) {
    return (
        <TouchableOpacity 
            activeOpacity={0.7}
            onPress={onPress}
            className="flex-row items-center justify-between bg-white/5 p-4 rounded-2xl mb-3 border border-white/10"
        >
            <View>
                <Text className="text-white font-semibold text-base mb-1">{date}</Text>
                <Text className="text-text-secondary text-sm">{duration}</Text>
            </View>
            
            <View className="flex-row items-center gap-3">
                <View className="bg-secondary/20 w-10 h-10 rounded-full items-center justify-center">
                    <Ionicons name="flash" size={18} color={AppColors.secondary} />
                </View>
                <Text className="text-white font-bold text-lg">{energy} kWh</Text>
            </View>
        </TouchableOpacity>
    );
}
