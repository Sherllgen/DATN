import React from "react";
import { View, Text } from "react-native";

interface BookingStatItemProps {
    label: string;
    value: string;
    icon?: React.ReactNode;
    showBorderRight?: boolean;
    flexRatio?: number;
}

const BookingStatItem = ({ label, value, icon, showBorderRight, flexRatio = 1 }: BookingStatItemProps) => {
    return (
        <View
            className={`items-center px-1 ${showBorderRight ? 'border-r border-white/10' : ''}`}
            style={{ flex: flexRatio }}
        >
            <Text className="text-white/40 text-[10px] mb-2 uppercase tracking-tight">
                {label}
            </Text>
            <View className="items-center justify-center min-h-[40px]">
                {icon ? (
                    <View className="mb-1">{icon}</View>
                ) : (
                    <Text
                        className="text-white text-[15px] font-semibold text-center"
                        numberOfLines={1}
                    >
                        {value}
                    </Text>
                )}
            </View>
        </View>
    );
};

export default BookingStatItem;
