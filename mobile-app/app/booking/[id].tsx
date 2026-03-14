import React from "react";
import { View, Text, ScrollView, Image } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams } from "expo-router";
import { MaterialCommunityIcons, MaterialIcons, Ionicons } from "@expo/vector-icons";
import AppHeader from "@/components/ui/AppHeader";
import GradientBackground from "@/components/ui/GradientBackground";
import ListItemCard from "@/components/ui/ListItemCard";
import Button from "@/components/ui/Button";
import { AppColors } from "@/constants/theme";
import { mockBookings } from "@/data/bookingData";

export default function BookingDetailsScreen() {
    const { id } = useLocalSearchParams();

    // Find booking data from mock list
    const booking = mockBookings.find(b => b.id === id) || mockBookings[0];

    const isUpcoming = booking.status === "Upcoming";

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }} edges={["top", "left", "right"]}>
                <AppHeader title="Booking Details" showBack />

                <ScrollView
                    style={{ flex: 1 }}
                    className="px-4"
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ paddingBottom: 0 }}
                    keyboardShouldPersistTaps="handled"
                >
                    {/* Vehicle Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Vehicle</Text>
                    <ListItemCard
                        icon={
                            <MaterialIcons name="electric-bike" size={22} color={AppColors.secondary} />
                        }
                        title="Tesla Bike"
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Model: Tesla Model S
                        </Text>
                        <Text className="mt-1 text-text-secondary text-sm">
                            Connectors: {booking.charger.connectorType}
                        </Text>
                    </ListItemCard>

                    {/* Charging Station Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charging Station</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-station" size={22} color={AppColors.secondary} />
                        }
                        title={booking.station.name}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Address: {booking.station.address}
                        </Text>
                    </ListItemCard>

                    {/* Charger Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charger</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-plug-type2" size={30} color={AppColors.secondary} />
                        }
                        title={`${booking.charger.connectorType} - Port 1`}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <View className="flex-row items-center flex-wrap gap-x-2 mt-2">
                            <Text className="text-text-secondary text-sm">
                                {booking.charger.connectorType}
                            </Text>
                            <View className="w-[1px] h-8 bg-border-gray mx-1" />
                            <View>
                                <Text className="text-text-secondary text-xs">Max. power</Text>
                                <Text className="text-white font-semibold text-base">
                                    {booking.charger.maxPower}
                                </Text>
                            </View>
                        </View>
                    </ListItemCard>

                    {/* Booking Details Section */}
                    <View className="bg-border/20 border border-border p-4 rounded-lg mt-4 mb-4">
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Booking Date</Text>
                            <Text className="text-white font-semibold text-base">{booking.date}</Text>
                        </View>
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Time of Arrival</Text>
                            <Text className="text-white font-semibold text-base">{booking.time}</Text>
                        </View>
                        <View className="flex-row justify-between">
                            <Text className="text-text-secondary text-base">Charging Duration</Text>
                            <Text className="text-white font-semibold text-base">{booking.duration}</Text>
                        </View>
                    </View>

                    <View className="bg-border/20 border border-border p-4 rounded-lg mb-4">
                        <View className="flex-row justify-between mb-4">
                            <Text className="text-text-secondary text-base">Amount Paid</Text>
                            <Text className="text-white font-semibold text-base">{booking.amount.toFixed(2)} VND</Text>
                        </View>
                        <View className="flex-row justify-between pb-4 border-b border-border/50 mb-4">
                            <Text className="text-text-secondary text-base">Tax</Text>
                            <Text className="text-white font-semibold text-base">Free</Text>
                        </View>
                        <View className="flex-row justify-between items-center">
                            <Text className="text-text-secondary text-base">Total Amount</Text>
                            <Text className="text-white font-semibold text-base">{booking.amount.toFixed(2)} VND</Text>
                        </View>
                    </View>

                    {/* Payment Method Section */}
                    <Text className="text-white font-semibold text-base mb-2">Payment Method</Text>
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

                    {/* Alert Message for Upcoming */}
                    {isUpcoming && (
                        <View className="bg-[#051F1A] border border-[#00A452]/30 rounded-lg p-4 mt-6 flex-row items-start">
                            <Ionicons
                                name="information-circle"
                                size={20}
                                color="#00A452"
                                style={{ marginTop: 2 }}
                            />
                            <Text className="flex-1 ml-3 text-secondary text-sm leading-5">
                                Insert the charger connection into your car to start charging. If you do not charge after 15 minutes from the time, this booking will be automatically cancelled.
                            </Text>
                        </View>
                    )}

                    {/* Bottom Padding for scroll when button is present */}
                    {isUpcoming && <View style={{ height: 100 }} />}
                </ScrollView>

                {/* Bottom Fixed Action */}
                <View className="px-6 pt-6 pb-12">
                    <Button
                        onPress={() => console.log("Cancel Booking", booking.id)}
                        className="w-full"
                        textClassName="font-semibold text-base"
                        style={{ height: 56 }}
                        variant={isUpcoming ? "danger" : "outline"}
                        disabled={!isUpcoming}
                    >
                        Cancel Booking
                    </Button>
                </View>
            </SafeAreaView>
        </GradientBackground>
    );
}
