import { Ionicons } from "@expo/vector-icons";
import React from "react";
import { Text, View } from "react-native";
import { BillingGroup } from "./types";

interface BillingGroupSectionProps {
    group: BillingGroup;
}

export default function BillingGroupSection({
    group,
}: BillingGroupSectionProps) {
    return (
        <View className="mb-4">
            <Text className="mb-3 px-4 py-2 border-gray-600 border-b rounded-md font-semibold text-white text-sm">
                {group.period}
            </Text>
            {group.items.map((billingItem, index: number) => (
                <View
                    key={index}
                    className="flex-row items-center mb-2 px-4 py-3"
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
                </View>
            ))}
        </View>
    );
}
