import React, { useEffect, useState } from "react";
import { View, Text, ScrollView, Image, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { MaterialCommunityIcons, MaterialIcons, Ionicons } from "@expo/vector-icons";
import AppHeader from "@/components/ui/AppHeader";
import GradientBackground from "@/components/ui/GradientBackground";
import ListItemCard from "@/components/ui/ListItemCard";
import Button from "@/components/ui/Button";
import { AppColors } from "@/constants/theme";
import { getBookingById } from "@/apis/bookingApi";
import { BookingResponse, BookingStatus } from "@/types/booking.types";

const parseUTCDate = (dateString: string) => {
    // Backend returns LocalDateTime (e.g. "2024-12-17T10:00:00") which is in UTC.
    // Appending 'Z' forces JS to parse it as UTC, automatically converting to local time (e.g., UTC+7).
    return new Date(dateString.endsWith('Z') ? dateString : dateString + 'Z');
};

// Date formatting utility
const formatDateTime = (dateString: string) => {
    const date = parseUTCDate(dateString);
    const dateOpts: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
    const timeOpts: Intl.DateTimeFormatOptions = { hour: '2-digit', minute: '2-digit' };
    return {
        dateStr: date.toLocaleDateString('en-US', dateOpts),
        timeStr: date.toLocaleTimeString('en-US', timeOpts)
    };
};

const formatDuration = (start: string, end: string) => {
    const startTime = parseUTCDate(start).getTime();
    const endTime = parseUTCDate(end).getTime();
    const diffHours = (endTime - startTime) / (1000 * 60 * 60);
    return `${diffHours} hour${diffHours > 1 ? 's' : ''}`;
};

export default function BookingDetailsScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const router = useRouter();
    const [booking, setBooking] = useState<BookingResponse | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchBooking = async () => {
            try {
                if (id) {
                    const data = await getBookingById(id);
                    setBooking(data);
                }
            } catch (error) {
                console.error("Failed to fetch booking details:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchBooking();
    }, [id]);

    if (loading) {
        return (
            <GradientBackground preset="main" dismissKeyboard={false}>
                <SafeAreaView style={{ flex: 1 }} edges={["top", "left", "right"]}>
                    <AppHeader title="Booking Details" showBack />
                    <View className="flex-1 justify-center items-center">
                        <ActivityIndicator size="large" color={AppColors.primary} />
                    </View>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (!booking) {
        return (
            <GradientBackground preset="main" dismissKeyboard={false}>
                <SafeAreaView style={{ flex: 1 }} edges={["top", "left", "right"]}>
                    <AppHeader title="Booking Details" showBack />
                    <View className="flex-1 justify-center items-center">
                        <Text className="text-white text-base">Booking not found</Text>
                    </View>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    const isUpcoming = booking.status === BookingStatus.CONFIRMED || booking.status === BookingStatus.PENDING;
    const { dateStr, timeStr } = formatDateTime(booking.startTime);
    const durationStr = formatDuration(booking.startTime, booking.endTime);

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
                        title={booking.vehicleBrand ? `${booking.vehicleBrand} ${booking.vehicleModelName || ''}`.trim() : 'My EV'}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Model: {booking.vehicleModelName || 'Default Model'}
                        </Text>
                        <Text className="mt-1 text-text-secondary text-sm">
                            Connectors: {booking.connectorType || 'N/A'}
                        </Text>
                    </ListItemCard>

                    {/* Charging Station Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charging Station</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-station" size={22} color={AppColors.secondary} />
                        }
                        title={booking.stationName || 'Unknown Station'}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Address: {booking.stationAddress || 'N/A'}
                        </Text>
                    </ListItemCard>

                    {/* Charger Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charger</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-plug-type2" size={30} color={AppColors.secondary} />
                        }
                        title={`${booking.connectorType || 'Unknown'} - Port ${booking.portNumber}`}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <View className="flex-row items-center flex-wrap gap-x-2 mt-2">
                            <Text className="text-text-secondary text-sm">
                                {booking.connectorType || 'N/A'}
                            </Text>
                            <View className="w-[1px] h-8 bg-border-gray mx-1" />
                            <View>
                                <Text className="text-text-secondary text-xs">Max. power</Text>
                                <Text className="text-white font-semibold text-base">
                                    {booking.maxPower ? `${booking.maxPower} kW` : 'N/A'}
                                </Text>
                            </View>
                        </View>
                    </ListItemCard>

                    {/* Booking Details Section */}
                    <View className="bg-border/20 border border-border p-4 rounded-lg mt-4 mb-4">
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Booking Date</Text>
                            <Text className="text-white font-semibold text-base">{dateStr}</Text>
                        </View>
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Time of Arrival</Text>
                            <Text className="text-white font-semibold text-base">{timeStr}</Text>
                        </View>
                        <View className="flex-row justify-between">
                            <Text className="text-text-secondary text-base">Charging Duration</Text>
                            <Text className="text-white font-semibold text-base">{durationStr}</Text>
                        </View>
                    </View>

                    <View className="bg-border/20 border border-border p-4 rounded-lg mb-4">
                        <View className="flex-row justify-between mb-4">
                            <Text className="text-text-secondary text-base">Amount Paid</Text>
                            <Text className="text-white font-semibold text-base">{booking.totalPrice?.toFixed(2) || '0.00'} VND</Text>
                        </View>
                        <View className="flex-row justify-between pb-4 border-b border-border/50 mb-4">
                            <Text className="text-text-secondary text-base">Tax</Text>
                            <Text className="text-white font-semibold text-base">Free</Text>
                        </View>
                        <View className="flex-row justify-between items-center">
                            <Text className="text-text-secondary text-base">Total Amount</Text>
                            <Text className="text-white font-semibold text-base">{booking.totalPrice?.toFixed(2) || '0.00'} VND</Text>
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
                <View className="px-6 pt-6 pb-12 gap-y-3">
                    {isUpcoming && (
                        <Button
                            onPress={() => router.push({ pathname: '/charging', params: { portId: booking.portNumber.toString(), bookingId: booking.id.toString() } })}
                            className="w-full"
                            textClassName="font-semibold text-base"
                            style={{ height: 56 }}
                            variant="primary"
                        >
                            Start Charging
                        </Button>
                    )}
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
