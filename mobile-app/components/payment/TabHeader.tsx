import React from "react";
import { Text, TouchableOpacity, View } from "react-native";

interface TabHeaderProps {
    activeTab: "payment" | "system";
    onTabChange: (tab: "payment" | "system") => void;
}

export default function TabHeader({ activeTab, onTabChange }: TabHeaderProps) {
    return (
        <View className="pb-2">
            <View className="flex-row">
                <TouchableOpacity
                    className="flex-1 pb-3"
                    onPress={() => onTabChange("payment")}
                    activeOpacity={0.7}
                >
                    <Text
                        className={`text-center text-base font-semibold ${
                            activeTab === "payment"
                                ? "text-green-600"
                                : "text-gray-400"
                        }`}
                    >
                        Payment
                    </Text>
                    {activeTab === "payment" && (
                        <View className="right-0 bottom-0 left-0 absolute bg-green-600 h-0.5" />
                    )}
                </TouchableOpacity>
                <TouchableOpacity
                    className="flex-1 pb-3"
                    onPress={() => onTabChange("system")}
                    activeOpacity={0.7}
                >
                    <Text
                        className={`text-center text-base font-semibold ${
                            activeTab === "system"
                                ? "text-green-600"
                                : "text-gray-400"
                        }`}
                    >
                        System
                    </Text>
                    {activeTab === "system" && (
                        <View className="right-0 bottom-0 left-0 absolute bg-green-600 h-0.5" />
                    )}
                </TouchableOpacity>
            </View>
        </View>
    );
}
