import React, { useState, useEffect } from "react";
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    Image,
    ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons, MaterialIcons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import MapView, { UrlTile, Marker } from "react-native-maps";

import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import StatusBadge, {
    StatusBadgeVariant,
} from "@/components/station/StatusBadge";
import ChargerTypeTag from "@/components/station/ChargerTypeTag";
import { getStationById } from "@/apis/stationApi/stationApi";
import { Station, StationStatus } from "@/types/station.types";

const mockOperatingHours = [
    { day: "Monday", hours: "00:00 - 00:00" },
    { day: "Tuesday", hours: "00:00 - 00:00" },
    { day: "Wednesday", hours: "00:00 - 00:00" },
    { day: "Thursday", hours: "00:00 - 00:00" },
    { day: "Friday", hours: "00:00 - 00:00" },
    { day: "Saturday", hours: "00:00 - 00:00" },
    { day: "Sunday", hours: "00:00 - 00:00" },
];

export default function StationDetailScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const [station, setStation] = useState<Station | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (id) {
            fetchStationDetails();
        }
    }, [id]);

    const fetchStationDetails = async () => {
        try {
            setLoading(true);
            setError(null);

            const data = await getStationById(Number(id));
            setStation(data);
            console.log("Loaded station:", data);
        } catch (err) {
            console.error("Error fetching station details:", err);
            setError("Failed to load station details");
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <GradientBackground preset="main" className="flex-1">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <ActivityIndicator size="large" color="#00A452" />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (loading) {
        return (
            <GradientBackground preset="main" className="flex-1">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <ActivityIndicator size="large" color="#00A452" />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (!station) {
        return (
            <GradientBackground preset="main" className="flex-1">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <Text className="text-white text-lg">Station not found</Text>
                    <Button
                        onPress={() => router.back()}
                        variant="primary"
                        className="mt-4"
                    >
                        Go Back
                    </Button>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    // Calculate status variant based on backend status  
    const statusVariant: StatusBadgeVariant =
        station.status === StationStatus.ACTIVE ? "available" : "occupied";

    const imageUrl = station.imageUrls && station.imageUrls.length > 0
        ? station.imageUrls[0]
        : null;

    return (
        <GradientBackground preset="main" className="flex-1">
            <SafeAreaView className="flex-1">
                <AppHeader
                    showBack
                    onBack={() => router.back()}
                    bgClassName="px-4"
                />

                <ScrollView className="flex-1" showsVerticalScrollIndicator={false}>
                    {/* Station Image */}
                    {imageUrl ? (
                        <Image
                            source={{ uri: imageUrl }}
                            className="w-full h-64"
                            resizeMode="cover"
                        />
                    ) : (
                        <View className="w-full h-64 bg-[#4A5568]/20 items-center justify-center">
                            <Ionicons
                                name="business"
                                size={64}
                                color="#9BA1A6"
                            />
                        </View>
                    )}

                    <View className="px-6 py-6">
                        {/* Station Name and Actions */}
                        <View className="flex-row items-start justify-between mb-4">
                            <View className="flex-1">
                                <Text className="text-2xl font-bold text-white mb-2">
                                    {station.name}
                                </Text>
                                <View className="flex-row items-center">
                                    <Ionicons
                                        name="location"
                                        size={16}
                                        color="#9BA1A6"
                                    />
                                    <Text className="text-sm text-[#9BA1A6] ml-1 flex-1">
                                        {station.address}
                                    </Text>
                                </View>
                            </View>
                        </View>

                        {/* Rating and Status */}
                        <View className="flex-row items-center mb-6">
                            <View className="flex-row items-center mr-4">
                                <Text className="text-base font-semibold text-white mr-2">
                                    {station.rate.toFixed(1)}
                                </Text>
                                <View className="flex-row">
                                    {[1, 2, 3, 4, 5].map((star) => (
                                        <Ionicons
                                            key={star}
                                            name={
                                                star <= station.rate
                                                    ? "star"
                                                    : "star-outline"
                                            }
                                            size={16}
                                            color="#F59E0B"
                                        />
                                    ))}
                                </View>
                            </View>
                            <StatusBadge variant={statusVariant} />
                        </View>

                        {/* Action Buttons */}
                        <View className="flex-row gap-3 mb-8">
                            <Button
                                variant="outline"
                                fullWidth={false}
                                onPress={() => console.log("Get Directions")}
                                className="flex-1"
                            >
                                <Ionicons name="navigate" size={20} color="white" />
                                {"  "}Direct
                            </Button>
                            <Button
                                variant="primary"
                                fullWidth={false}
                                onPress={() => console.log("Call station")}
                                className="flex-1"
                            >
                                <Ionicons name="call" size={20} color="white" />
                                {"  "}Call
                            </Button>
                        </View>

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
                            <View className="bg-[#4A5568]/20 rounded-lg p-4 border border-[#4A5568]">
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
                        <View className="mb-8">
                            <View className="flex-row items-center justify-between mb-3">
                                <Text className="text-lg font-semibold text-white">
                                    Opening Hours
                                </Text>
                                <View className="flex-row items-center">
                                    <View className="w-2 h-2 rounded-full bg-[#4CAF50] mr-2" />
                                    <Text className="text-sm font-medium text-[#4CAF50]">
                                        Open · 24 hours
                                    </Text>
                                </View>
                            </View>

                            <View className="bg-[#4A5568]/20 rounded-lg p-4 border border-[#4A5568]">
                                {mockOperatingHours.map((schedule, index) => (
                                    <View
                                        key={index}
                                        className="flex-row justify-between py-2"
                                    >
                                        <Text className="text-base text-white font-medium">
                                            {schedule.day}
                                        </Text>
                                        <Text className="text-base text-[#9BA1A6]">
                                            {schedule.hours}
                                        </Text>
                                    </View>
                                ))}
                            </View>
                        </View>

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
                                    mapType="none"
                                    scrollEnabled={false}
                                    zoomEnabled={false}
                                    initialRegion={{
                                        latitude: station.latitude,
                                        longitude: station.longitude,
                                        latitudeDelta: 0.01,
                                        longitudeDelta: 0.01,
                                    }}
                                >
                                    <UrlTile
                                        urlTemplate="https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                        maximumZ={19}
                                        flipY={false}
                                        zIndex={1}
                                        tileSize={256}
                                    />
                                    <Marker
                                        coordinate={{
                                            latitude: station.latitude,
                                            longitude: station.longitude,
                                        }}
                                        pinColor="#4CAF50"
                                    />
                                </MapView>
                            </View>
                        </View>

                        {/* Book Button (Fixed at bottom) */}
                        <Button
                            variant="primary"
                            fullWidth
                            onPress={() => {
                                // Navigate to QR scan or charging flow
                                router.push("/qr_scan");
                            }}
                            className="mb-6"
                        >
                            Book now
                        </Button>
                    </View>
                </ScrollView>
            </SafeAreaView>
        </GradientBackground>
    );
}
