import AppHeader from "@/components/ui/AppHeader";
import { LinearGradient } from "expo-linear-gradient";
import { useLocalSearchParams } from "expo-router";
import React from "react";
import { ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function PaymentDetailPage() {
    const params = useLocalSearchParams();

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1"
        >
            <SafeAreaView className="flex-1">
                <AppHeader title="Payment Detail" />

                <ScrollView className="flex-1 px-4">
                    {/* Transaction Info Section */}
                    <View className="mb-4 p-4 rounded-lg">
                        <Text className="mb-4 font-semibold text-white">
                            Transaction Information
                        </Text>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Location
                            </Text>
                            <Text className="flex-1 ml-4 font-medium text-white text-sm text-right">
                                EVGO - 123 Main St
                            </Text>
                        </View>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Transaction Code
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                662b7668d946e05d2a201ee2
                            </Text>
                        </View>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Transaction Time
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                {params.date || "04/26/2024 - 16:40"}
                            </Text>
                        </View>

                        <View className="flex-row justify-between">
                            <Text className="text-gray-400 text-sm">
                                Payment Method
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                ****9149
                            </Text>
                        </View>
                    </View>

                    {/* Billing Details Section */}
                    <View className="mb-4 p-4 rounded-lg">
                        <Text className="mb-4 font-semibold text-white text-base">
                            Billing Details
                        </Text>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Invoice Code
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                662ab36a8173001bb8bc3bfd
                            </Text>
                        </View>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Vehicle
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                VinFast Evo Lite Neo
                            </Text>
                        </View>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Charging Power
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                120 kWh
                            </Text>
                        </View>

                        <View className="flex-row justify-between mb-3">
                            <Text className="text-gray-400 text-sm">
                                Charging Time
                            </Text>
                            <Text className="font-medium text-white text-sm">
                                2 hours 30 minutes
                            </Text>
                        </View>

                        <View className="flex-row justify-between">
                            <Text className="text-gray-400 text-sm">
                                Charging Cost
                            </Text>
                            <Text className="font-semibold text-white text-sm">
                                {params.amount || "$9.576"}
                            </Text>
                        </View>
                    </View>

                    {/* Total Section */}
                    <View className="flex-row justify-between items-center mb-6 p-4">
                        <Text className="font-bold text-white">
                            Total Payment
                        </Text>
                        <Text className="font-bold text-white">
                            {params.amount || "$9.576"}
                        </Text>
                    </View>
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
