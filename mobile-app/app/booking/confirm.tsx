import React from "react";
import { View, Text, ScrollView, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams } from "expo-router";
import { MaterialCommunityIcons, MaterialIcons, Ionicons } from "@expo/vector-icons";
import AppHeader from "@/components/ui/AppHeader";
import GradientBackground from "@/components/ui/GradientBackground";
import ListItemCard from "@/components/ui/ListItemCard";
import Button from "@/components/ui/Button"; // Use the requested Button component
import { AppColors } from "@/constants/theme";
import { mockBookingReview } from "@/data/mockBookingReview";

export default function ConfirmBookingScreen() {
    const params = useLocalSearchParams();

    // In a real app we'd fetch data based on the IDs from params
    // For now, we use the provided mock data
    const { vehicle, station, charger, bookingDetails, price } = mockBookingReview;

    return (
        <GradientBackground preset="main" className="flex-1">
            <SafeAreaView className="flex-1">
                <AppHeader title="Review Summary" showBack />

                <ScrollView
                    className="flex-1 px-4"
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ paddingBottom: 0 }}
                >
                    {/* Vehicle Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Vehicle</Text>
                    <ListItemCard
                        icon={
                            <MaterialIcons name="electric-bike" size={22} color={AppColors.secondary} />
                        }
                        title={vehicle.brand}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Model: {vehicle.modelName}
                        </Text>
                        <Text className="mt-1 text-text-secondary text-sm">
                            Connectors: {charger.connectorType}
                        </Text>
                    </ListItemCard>

                    {/* Charging Station Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charging Station</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-station" size={22} color={AppColors.secondary} />
                        }
                        title={station.name}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Address: {station.address}
                        </Text>
                    </ListItemCard>

                    {/* Charger Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charger</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-plug-type2" size={30} color={AppColors.secondary} />
                        }
                        title={`${charger.type.split(' - ')[0]} - Port 1`}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <View className="flex-row items-center flex-wrap gap-x-2 mt-2">
                            <Text className="text-text-secondary text-sm">
                                {charger.type.split(' - ')[1]}
                            </Text>
                            <View className="w-[1px] h-8 bg-border-gray mx-1" />
                            <View>
                                <Text className="text-text-secondary text-xs">Max. power</Text>
                                <Text className="text-white font-semibold text-base">
                                    {charger.maxPower}
                                </Text>
                            </View>
                        </View>
                    </ListItemCard>

                    {/* Booking Details Section */}
                    <View className="bg-border/20 border border-border p-4 rounded-lg mt-4 mb-4">
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Booking Date</Text>
                            <Text className="text-white font-semibold text-base">{bookingDetails.date}</Text>
                        </View>
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Time of Arrival</Text>
                            <Text className="text-white font-semibold text-base">{bookingDetails.time}</Text>
                        </View>
                        <View className="flex-row justify-between">
                            <Text className="text-text-secondary text-base">Charging Duration</Text>
                            <Text className="text-white font-semibold text-base">{bookingDetails.duration}</Text>
                        </View>
                    </View>

                    <View className="bg-border/20 border border-border p-4 rounded-lg mb-4">
                        <View className="flex-row justify-between mb-4">
                            <Text className="text-text-secondary text-base">Amount Estimation</Text>
                            <Text className="text-white font-semibold text-base">{price.estimation} VND</Text>
                        </View>
                        <View className="flex-row justify-between pb-4 border-b border-border/50 mb-4">
                            <Text className="text-text-secondary text-base">Tax</Text>
                            <Text className="text-white font-semibold text-base">Free</Text>
                        </View>
                        <View className="flex-row justify-between items-center">
                            <Text className="text-text-secondary text-base">Total Amount</Text>
                            <Text className="text-white font-semibold text-base">{price.total} VND</Text>
                        </View>
                    </View>

                    {/* Payment Method Section */}
                    <Text className="text-white font-semibold text-base mb-2">Selected Payment Method</Text>
                    <ListItemCard
                        icon={
                            <View>
                                <Image
                                    source={require("@/assets/images/zalopay.webp")}
                                    style={{ width: 44, height: 44, borderRadius: 8 }}
                                    resizeMode="contain"
                                />
                            </View>
                        }
                        title="ZaloPay App"
                        titleClassName="text-white font-semibold text-base"
                    />

                    {/* Alert Message */}
                    <View className="bg-[#051F1A] border border-[#00A452]/30 rounded-lg p-4 mt-2 flex-row items-start mb-[100px]">
                        <Ionicons
                            name="information-circle"
                            size={20}
                            color="#00A452"
                            style={{ marginTop: 2 }}
                        />
                        <Text className="flex-1 ml-3 text-secondary text-sm leading-5">
                            You will be redirected to the ZaloPay app to authorize. Your account will only be charged after the charging session is completed.
                        </Text>
                    </View>
                </ScrollView>

                {/* Bottom Fixed Action */}
                <View className="px-6 pt-6 pb-2">
                    <Button
                        onPress={() => console.log("Confirm Booking")}
                        className="w-full"
                        textClassName="font-semibold text-base"
                        style={{ height: 56 }}
                        variant="primary"
                    >
                        Confirm Booking
                    </Button>
                </View>
            </SafeAreaView>
        </GradientBackground>
    );
}
