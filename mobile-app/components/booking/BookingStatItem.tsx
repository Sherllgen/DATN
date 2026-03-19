import React from "react";
import { View, Text } from "react-native";

interface BookingStatItemProps {
    label: string;
    value: string;
    icon?: React.ReactNode;
    showBorderRight?: boolean;
}

const BookingStatItem = ({ label, value, icon, showBorderRight }: BookingStatItemProps) => {
    return (
        <View
            className={`flex-1 items-center px-1 ${showBorderRight ? 'border-r border-white/10' : ''}`}
        >
            <Text className="text-white/40 text-[10px] mb-2 uppercase tracking-tight">
                {label}
            </Text>
            <View className="items-center justify-center min-h-[40px]">
                {icon ? (
                    <View className="mb-1">{icon}</View>
                ) : (
                    <Text
                        className="text-white text-xl font-semibold text-center"
                        numberOfLines={1}
                        adjustsFontSizeToFit
                    >
                        {value}
                    </Text>
                )}
            </View>
        </View>
    );
};

export default BookingStatItem;
