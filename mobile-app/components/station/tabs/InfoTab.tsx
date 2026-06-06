import React from "react";
import { View, Text, Alert } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import MapView, { Marker, PROVIDER_GOOGLE } from "react-native-maps";
import StationMarker from "@/components/map/StationMarker";
import { Station, StationStatus, PriceSettingResponse } from "@/types/station.types";
import ChargerTypeTag from "@/components/station/ChargerTypeTag";
import Button from "@/components/ui/Button";
import { useUserStore } from "@/contexts/user.store";
import { useAuthStore } from "@/contexts/auth.store";

interface InfoTabProps {
    station: Station;
    priceSetting?: PriceSettingResponse | null;
}

// Helper function to format day of week
const formatDayOfWeek = (day: string): string => {
    const dayMap: { [key: string]: string } = {
        MONDAY: "Monday",
        TUESDAY: "Tuesday",
        WEDNESDAY: "Wednesday",
        THURSDAY: "Thursday",
        FRIDAY: "Friday",
        SATURDAY: "Saturday",
        SUNDAY: "Sunday",
    };
    return dayMap[day] || day;
};

// Helper function to format time range
const formatTimeRange = (openTime: string | null, closeTime: string | null): string => {
    if (!openTime || !closeTime) {
        return "00:00 - 00:00";
    }
    // Convert "HH:MM:SS" to "HH:MM"
    const formatTime = (time: string) => time.substring(0, 5);
    return `${formatTime(openTime)} - ${formatTime(closeTime)}`;
};

const InfoTab = ({ station, priceSetting }: InfoTabProps) => {
    const unpaidCount = useUserStore((state) => state.unpaidCount) || 0;

    const handleBookPress = () => {
        const { accessToken } = useAuthStore.getState();
        if (!accessToken) {
            Alert.alert(
                "Login Required",
                "You must be logged in to book a charging slot.",
                [
                    { text: "Cancel", style: "cancel" },
                    { text: "Log In", onPress: () => router.push("/auth/login") }
                ]
            );
            return;
        }

        if (unpaidCount > 0) {
            Alert.alert("Action Blocked", "Please settle your unpaid invoices before booking a slot.");
            return;
        }
        router.push(`/booking/selectVehicle?stationId=${station.id}`);
    };

    return (
        <View>
            {/* About Section */}
            {station.description && (
                <View className="mb-8">
                    <Text className="text-lg font-semibold text-white mb-3">
                        About
                    </Text>
                    <Text className="text-base text-[#9BA1A6] leading-6">
                        {station.description}
                    </Text>
                </View>
            )}

            {/* Pricing */}
            <View className="mb-8">
                <Text className="text-lg font-semibold text-white mb-3">
                    Cost
                </Text>
                <View className="bg-border-gray/20 rounded-lg p-4 border border-border-gray">
                    {priceSetting ? (
                        <View>
                            <View className="flex-row items-center justify-between mb-3">
                                <View className="flex-row items-center">
                                    <Ionicons name="flash" size={20} color="#F59E0B" />
                                    <Text className="text-base font-medium text-white ml-3">Charging Rate</Text>
                                </View>
                                <Text className="text-base text-white font-medium">{priceSetting.chargingRatePerKwh.toLocaleString()} VND/kWh</Text>
                            </View>
                            <View className="flex-row items-center justify-between">
                                <View className="flex-row items-center">
                                    <Ionicons name="calendar" size={20} color="#00A452" />
                                    <Text className="text-base font-medium text-white ml-3">Booking Fee</Text>
                                </View>
                                <Text className="text-base text-white font-medium">{priceSetting.bookingFee.toLocaleString()} VND/Hour</Text>
                            </View>
                        </View>
                    ) : (
                        <View className="flex-row items-center">
                            <Ionicons
                                name="card"
                                size={20}
                                color="#4CAF50"
                            />
                            <Text className="text-base font-medium text-white ml-3">
                                Please contact for pricing
                            </Text>
                        </View>
                    )}
                </View>
            </View>

            {/* Operating Hours */}
            {station.openingHours && station.openingHours.length > 0 && (
                <View className="mb-8">
                    <View className="flex-row items-center justify-between mb-3">
                        <Text className="text-lg font-semibold text-white">
                            Opening Hours
                        </Text>
                    </View>

                    <View className="bg-border-gray/20 rounded-lg p-4 border border-border-gray">
                        {station.openingHours.map((hours, index) => (
                            <View
                                key={hours.id || index}
                                className="flex-row justify-between py-2"
                            >
                                <Text className="text-base text-white font-medium">
                                    {formatDayOfWeek(hours.dayOfWeek)}
                                </Text>
                                <Text className="text-base text-[#9BA1A6]">
                                    {formatTimeRange(hours.openTime, hours.closeTime)}
                                </Text>
                            </View>
                        ))}
                    </View>
                </View>
            )}

            {/* Charger Types */}
            <View className="mb-8">
                <Text className="text-lg font-semibold text-white mb-3">
                    Charger Types
                </Text>
                <View className="flex-row flex-wrap gap-3">
                    {station.chargers && station.chargers.length > 0 ? (
                        station.chargers.map((charger, index) => (
                            <ChargerTypeTag
                                key={index}
                                type={charger.connectorType}
                                available={charger.available}
                                total={charger.total}
                            />
                        ))
                    ) : (
                        <Text className="text-sm text-[#9BA1A6]">
                            No charger information available
                        </Text>
                    )}
                </View>
            </View>

            {/* Location Map */}
            <View className="mb-8">
                <Text className="text-lg font-semibold text-white mb-3">
                    Location
                </Text>
                <Text className="text-sm text-[#9BA1A6] mb-3">
                    <Ionicons name="location" size={14} color="#00A452" />
                    {" "}{station.address}
                </Text>
                <View className="h-48 rounded-2xl overflow-hidden">
                    <MapView
                        style={{ flex: 1 }}
                        provider={PROVIDER_GOOGLE}
                        mapType="standard"
                        scrollEnabled={false}
                        zoomEnabled={false}
                        initialRegion={{
                            latitude: station.latitude,
                            longitude: station.longitude,
                            latitudeDelta: 0.01,
                            longitudeDelta: 0.01,
                        }}
                    >

                        <StationMarker
                            station={station}
                        />
                    </MapView>
                </View>
            </View>
            {/* Book Button (Fixed at bottom) */}
            <Button
                variant="primary"
                size="lg"
                fullWidth
                style={{ height: 52 }}
                onPress={handleBookPress}
                className="mb-4"
                disabled={unpaidCount > 0}
            >
                Book Now
            </Button>
        </View>
    );
};

export default InfoTab;
