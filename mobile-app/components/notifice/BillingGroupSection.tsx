import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";
import React from "react";
import { Text, TouchableOpacity, View } from "react-native";
import { BillingGroup } from "./types";

interface BillingGroupSectionProps {
    group: BillingGroup;
}

export default function BillingGroupSection({
    group,
}: BillingGroupSectionProps) {
    const router = useRouter();

    const handleItemPress = (item: any) => {
        router.push({
            pathname: "/(tabs)/notifice/detail",
            params: {
                title: item.title,
                date: item.date,
                amount: item.amount,
                id: item.id,
            },
        });
    };

    return (
        <View className="mb-4 px-6">
            <Text className="-mx-6 mb-3 px-6 py-2 border-gray-600 border-b rounded-md font-semibold text-white text-sm">
                {group.period}
            </Text>
            {group.items.map((billingItem, index: number) => (
                <TouchableOpacity
                    key={index}
                    className="flex-row items-center mb-2 py-3"
                    onPress={() => handleItemPress(billingItem)}
                    activeOpacity={0.7}
                >
                    <View className="justify-center items-center mr-3 border-2 border-green-500 rounded-full w-6 h-6">
                        <Ionicons name="checkmark" size={14} color="#22c55e" />
                    </View>
                    <View className="flex-1">
                        <Text className="mb-1 font-medium text-white text-base">
                            {billingItem.title}
                        </Text>
                        <Text className="text-gray-400 text-sm">
                            {billingItem.date}
                        </Text>
                    </View>
                    <Text className="flex-shrink-0 font-semibold text-white text-sm">
                        {billingItem.amount}
                    </Text>
                </TouchableOpacity>
            ))}
        </View>
    );
}
