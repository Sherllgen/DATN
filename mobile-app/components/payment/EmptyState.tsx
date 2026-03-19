import React from "react";
import { Text, View } from "react-native";

interface EmptyStateProps {
    message: string;
}

export default function EmptyState({ message }: EmptyStateProps) {
    return (
        <View className="flex-1 justify-center items-center py-20">
            <Text className="text-gray-500 text-base">{message}</Text>
        </View>
    );
}
