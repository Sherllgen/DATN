import React from "react";
import { View, Text } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import MapView, { Marker } from "react-native-maps";
import { Station, StationStatus } from "@/types/station.types";
import ChargerTypeTag from "@/components/station/ChargerTypeTag";
import Button from "@/components/ui/Button";

interface InfoTabProps {
    station: Station;
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

const InfoTab = ({ station }: InfoTabProps) => {
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
                    <View className="flex-row items-center">
                        <Ionicons
                            name="card"
                            size={20}
                            color="#4CAF50"
                        />
                        <Text className="text-base font-medium text-white ml-3">
                            Payment is required
                        </Text>
                    </View>
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
                        <Marker
                            coordinate={{
                                latitude: station.latitude,
                                longitude: station.longitude,
                            }}
                            pinColor={
                                station.status === StationStatus.ACTIVE
                                    ? "#4CAF50" // success/secondary
                                    : station.status === StationStatus.SUSPENDED
                                        ? "#F59E0B" // warning
                                        : "#EF4444" // error
                            }
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
                onPress={() => {
                    router.push(`/booking/selectVehicle?stationId=${station.id}`);
                }}
                className="mb-4"
            >
                Book Now
            </Button>
        </View>
    );
};

export default InfoTab;
