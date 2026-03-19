import React from "react";
import { View, Text } from "react-native";
import { MaterialCommunityIcons, Ionicons, FontAwesome6 } from "@expo/vector-icons";

export interface ChargingInfoCardProps {
    label: string;
    value: string | number;
    subValue?: string;
    iconName: any;
    iconType?: "Ionicons" | "MaterialCommunityIcons" | "FontAwesome6";
    iconColor: string;
    bgColor: string;
    iconBgColor: string;
}

export default function ChargingInfoCard({
    label,
    value,
    subValue,
    iconName,
    iconType = "MaterialCommunityIcons",
    iconColor,
    bgColor,
    iconBgColor,
}: ChargingInfoCardProps) {
    const IconComponent = 
        iconType === "Ionicons" ? Ionicons : 
        iconType === "FontAwesome6" ? FontAwesome6 : 
        MaterialCommunityIcons;

    return (
        <View 
            style={{ backgroundColor: bgColor }}
            className="flex-1 p-4 rounded-2xl h-44 justify-between"
        >
            <View 
                style={{ backgroundColor: iconBgColor }}
                className="w-10 h-10 rounded-lg items-center justify-center"
            >
                <IconComponent name={iconName} size={20} color={iconColor} />
            </View>
            
            <View>
                <Text className="text-text-secondary text-base mb-1">{label}</Text>
                <View className="flex-row items-baseline">
                    <Text className="text-white font-bold text-2xl">{value}</Text>
                    {subValue && (
                        <Text className="text-white text-sm ml-0.5">{subValue}</Text>
                    )}
                </View>
            </View>
        </View>
    );
}
