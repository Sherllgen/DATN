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
import { Ionicons } from "@expo/vector-icons";
import { router, useLocalSearchParams } from "expo-router";
import { LinearGradient } from "expo-linear-gradient";

import GradientBackground from "@/components/ui/GradientBackground";
import Button from "@/components/ui/Button";
import StatusBadge, {
    StatusBadgeVariant,
} from "@/components/station/StatusBadge";
import StationTabs from "@/components/station/StationTabs";
import { getStationById } from "@/apis/stationApi/stationApi";
import { Station, StationStatus } from "@/types/station.types";
import { useStationCache } from "@/stores/stationCacheStore";


export default function StationDetailScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const getCachedStation = useStationCache(state => state.getStation);

    // Try to get cached station first for instant render
    const cachedStation = id ? getCachedStation(Number(id)) : null;

    const [station, setStation] = useState<Station | null>(cachedStation);
    const [loading, setLoading] = useState(!cachedStation); // Only show loading if no cache
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (id) {
            // Always fetch fresh data in background (stale-while-revalidate pattern)
            fetchStationDetails();
        }
    }, [id]);

    const fetchStationDetails = async () => {
        try {
            // Only show loading spinner if we don't already have data
            setLoading(prev => prev && !station); // Fix: check current state, not closure
            setError(null);

            const data = await getStationById(Number(id));
            setStation(data);

            // Update cache with fresh data
            useStationCache.getState().setStation(data);

            if (__DEV__) {
                console.log("[StationDetail] Loaded station:", data.name);
            }
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
        station.status === StationStatus.ACTIVE
            ? "available"
            : station.status === StationStatus.SUSPENDED
                ? "suspended"
                : "occupied";

    const imageUrl = station.imageUrls && station.imageUrls.length > 0
        ? station.imageUrls[0]
        : null;

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <View style={{ flex: 1 }}>
                <ScrollView
                    style={{ flex: 1 }}
                    showsVerticalScrollIndicator={false}
                    keyboardShouldPersistTaps="handled"
                >
                    {/* Station Image - Full bleed to top including status bar */}
                    <View className="relative">
                        {imageUrl ? (
                            <Image
                                source={{ uri: imageUrl }}
                                className="w-full h-80"
                                resizeMode="cover"
                            />
                        ) : (
                            <View className="w-full h-80 bg-border-gray/20 items-center justify-center">
                                <Ionicons
                                    name="business"
                                    size={64}
                                    color="#9BA1A6"
                                />
                            </View>
                        )}

                        {/* Gradient Overlay - Dark at top fading to transparent */}
                        <LinearGradient
                            colors={['rgba(0, 0, 0, 0.8)', 'transparent']}
                            className="absolute top-0 left-0 right-0"
                            style={{ height: 162 }}
                        />

                        {/* Header Buttons - Absolute position on top of image */}
                        <SafeAreaView className="absolute top-0 left-0 right-0">
                            <View className="flex-row items-center justify-between px-4 pt-2">
                                {/* Back Button */}
                                <TouchableOpacity
                                    onPress={() => router.back()}
                                    className="w-10 h-10 rounded-full bg-white/20 items-center justify-center"
                                    activeOpacity={0.7}
                                >
                                    <Ionicons name="chevron-back" size={24} color="white" />
                                </TouchableOpacity>

                                {/* Bookmark/Save Button */}
                                <TouchableOpacity
                                    onPress={() => console.log("Bookmark station")}
                                    className="w-10 h-10 rounded-full bg-white/20 items-center justify-center"
                                    activeOpacity={0.7}
                                >
                                    <Ionicons name="bookmark-outline" size={20} color="white" />
                                </TouchableOpacity>
                            </View>
                        </SafeAreaView>
                    </View>

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
                                    {station.rate?.toFixed(1) ?? "N/A"}
                                </Text>
                                <View className="flex-row">
                                    {[1, 2, 3, 4, 5].map((star) => (
                                        <Ionicons
                                            key={star}
                                            name={
                                                station.rate && star <= station.rate
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
                                onPress={() => console.log("Rate station")}
                                className="flex-1"
                            >
                                <Ionicons name="star-outline" size={20} color="white" />
                                {"  "}Rate
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
                        {/* Tabs Navigation and Content */}
                        <StationTabs station={station} />

                    </View>
                </ScrollView>
            </View>
        </GradientBackground>
    );
}
