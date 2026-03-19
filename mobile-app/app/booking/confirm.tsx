import React, { useState, useEffect } from "react";
import { View, Text, ScrollView, Image, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams } from "expo-router";
import { MaterialCommunityIcons, MaterialIcons, Ionicons } from "@expo/vector-icons";
import AppHeader from "@/components/ui/AppHeader";
import GradientBackground from "@/components/ui/GradientBackground";
import ListItemCard from "@/components/ui/ListItemCard";
import Button from "@/components/ui/Button";
import { AppColors } from "@/constants/theme";
import { useStationCache } from "@/stores/stationCacheStore";
import { mockVehicles } from "@/data/vehicles";
import { mockChargersResponse } from "@/data/chargers";
import { bookingDurations } from "@/data/booking";
import { getStationById } from "@/apis/stationApi/stationApi";
import { Station } from "@/types/station.types";

export default function ConfirmBookingScreen() {
    const { stationId, vehicleId, portId, date, time, duration } = useLocalSearchParams<{
        stationId: string;
        vehicleId: string;
        portId: string;
        date: string;
        time: string;
        duration: string;
    }>();

    const getCachedStation = useStationCache(state => state.getStation);
    const [station, setStation] = useState<Station | null>(() => {
        return stationId ? getCachedStation(Number(stationId)) : null;
    });
    const [loading, setLoading] = useState(!station);

    useEffect(() => {
        if (!station && stationId) {
            fetchStationDetails();
        }
    }, [stationId]);

    const fetchStationDetails = async () => {
        try {
            setLoading(true);
            const data = await getStationById(Number(stationId));
            setStation(data);
            useStationCache.getState().setStation(data);
        } catch (err) {
            console.error("Error fetching station details:", err);
        } finally {
            setLoading(false);
        }
    };

    const vehicle = mockVehicles.find(v => v.id === vehicleId) || mockVehicles[0];
    
    // Find charger and port
    const allChargers = mockChargersResponse.data;
    let selectedCharger = allChargers[0];
    let selectedPort = selectedCharger.ports[0];

    for (const charger of allChargers) {
        const port = charger.ports.find(p => p.id === Number(portId));
        if (port) {
            selectedCharger = charger;
            selectedPort = port;
            break;
        }
    }

    const durationLabel = bookingDurations.find(d => d.value === duration)?.label || "1 Hour";
    const priceEstimation = 30000 * parseFloat(duration || "1.0");

    if (loading) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <ActivityIndicator size="large" color="#00A452" />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (!station) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <Text className="text-white mb-4">Station data not found.</Text>
                    <Button onPress={() => fetchStationDetails()} variant="primary">
                        Retry
                    </Button>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }}>
                <AppHeader title="Review Summary" showBack />

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
                        title={vehicle.brand}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Model: {vehicle.modelName}
                        </Text>
                        <Text className="mt-1 text-text-secondary text-sm">
                            Connectors: {selectedCharger.connectorType}
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
                        title={`${selectedCharger.name} - Port ${selectedPort.portNumber}`}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <View className="flex-row items-center flex-wrap gap-x-2 mt-2">
                            <Text className="text-text-secondary text-sm">
                                {selectedCharger.connectorType}
                            </Text>
                            <View className="w-[1px] h-8 bg-border-gray mx-1" />
                            <View>
                                <Text className="text-text-secondary text-xs">Max. power</Text>
                                <Text className="text-white font-semibold text-base">
                                    {selectedCharger.maxPower} kW
                                </Text>
                            </View>
                        </View>
                    </ListItemCard>

                    {/* Booking Details Section */}
                    <View className="bg-border/20 border border-border p-4 rounded-lg mt-4 mb-4">
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Booking Date</Text>
                            <Text className="text-white font-semibold text-base">{date}</Text>
                        </View>
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Time of Arrival</Text>
                            <Text className="text-white font-semibold text-base">{time}</Text>
                        </View>
                        <View className="flex-row justify-between">
                            <Text className="text-text-secondary text-base">Charging Duration</Text>
                            <Text className="text-white font-semibold text-base">{durationLabel}</Text>
                        </View>
                    </View>

                    <View className="bg-border/20 border border-border p-4 rounded-lg mb-4">
                        <View className="flex-row justify-between mb-4">
                            <Text className="text-text-secondary text-base">Amount Estimation</Text>
                            <Text className="text-white font-semibold text-base">{priceEstimation.toLocaleString()} VND</Text>
                        </View>
                        <View className="flex-row justify-between pb-4 border-b border-border/50 mb-4">
                            <Text className="text-text-secondary text-base">Tax</Text>
                            <Text className="text-white font-semibold text-base">Free</Text>
                        </View>
                        <View className="flex-row justify-between items-center">
                            <Text className="text-text-secondary text-base">Total Amount</Text>
                            <Text className="text-white font-semibold text-base">{priceEstimation.toLocaleString()} VND</Text>
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
                        onPress={() => console.log("Confirm Booking", { stationId, vehicleId, portId, date, time, duration })}
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
