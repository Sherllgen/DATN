import { StyleSheet, Text, TouchableOpacity, View, Image, ScrollView } from "react-native";
import MapView, { UrlTile, Marker } from 'react-native-maps';
import { SafeAreaView } from "react-native-safe-area-context";
import { router } from "expo-router";
import { useState, useEffect, useCallback } from "react";
import { useFocusEffect } from "expo-router";

import GradientBackground from "@/components/ui/GradientBackground";
import LocationPermissionModal from "@/components/map/LocationPermissionModal";
import ManualLocationInput from "@/components/map/ManualLocationInput";
import { searchNearbyStations } from "@/apis/stationApi/stationApi";
import { StationSearchResult, StationStatus } from "@/types/station.types";
import { useLocationPermission } from "@/hooks/useLocationPermission";
import { useLocationStore } from "@/stores/locationStore";
import { useUserStore } from "@/contexts/user.store";
import { useChargingStore } from "@/stores/chargingStore";
import { getMyChargingSessions } from "@/apis/chargingApi";
import { ChargingSessionStatus, ChargingSessionResponse } from "@/types/charging.types";
import ChargingInfoCard from "@/components/home/ChargingInfoCard";
import HistoryItem from "@/components/home/HistoryItem";
import { mockChargingInfo } from "@/data/homeData"; // keeping mock data only as fallback
import ActiveChargingNotification from "@/components/home/ActiveChargingNotification";
import { Ionicons } from "@expo/vector-icons";
import DebtBanner from "@/components/home/DebtBanner";
import * as paymentApi from "@/apis/paymentApi";

export default function HomePage() {
    const locationPerm = useLocationPermission(true); // Auto-check on mount
    const globalLocation = useLocationStore((state) => state.location);
    const setGlobalLocation = useLocationStore((state) => state.setLocation);
    const [stations, setStations] = useState<StationSearchResult[]>([]);
    const [loading, setLoading] = useState(false);
    const [recentSessions, setRecentSessions] = useState<ChargingSessionResponse[]>([]);
    const [lastSession, setLastSession] = useState<ChargingSessionResponse | null>(null);

    const user = useUserStore((state) => state.user);
    const unpaidCount = useUserStore((state) => state.unpaidCount);
    const setActiveSession = useChargingStore((state) => state.setActiveSession);

    // Sync charging session state & debt state on screen focus
    useFocusEffect(
        useCallback(() => {
            if (user?.id) {
                // Fetch active session
                getMyChargingSessions(Number(user.id))
                    .then((sessions) => {
                        const active = sessions.find(
                            (s) =>
                                s.status === ChargingSessionStatus.PREPARING ||
                                s.status === ChargingSessionStatus.CHARGING ||
                                s.status === ChargingSessionStatus.SUSPENDED_EV ||
                                s.status === ChargingSessionStatus.SUSPENDED_EVSE ||
                                s.status === ChargingSessionStatus.FINISHING
                        );
                        if (active) {
                            setActiveSession(active);
                        } else {
                            setActiveSession(null);
                        }

                        // Get completed sessions for history
                        const completedSessions = sessions
                            .filter(s => s.status === ChargingSessionStatus.COMPLETED)
                            .sort((a, b) => new Date(b.startTime || 0).getTime() - new Date(a.startTime || 0).getTime());
                        
                        setRecentSessions(completedSessions);
                        if (completedSessions.length > 0) {
                            setLastSession(completedSessions[0]);
                        }
                    })
                    .catch((err) => {
                        console.log("Failed to fetch charging sessions:", err);
                    });

                // Fetch debt state
                paymentApi.checkUnpaidInvoices()
                    .then((hasUnpaid: boolean) => {
                        useUserStore.getState().setUnpaidCount(hasUnpaid ? 1 : 0);
                    })
                    .catch((err: any) => {
                        console.log("Failed to fetch unpaid invoices:", err);
                    });
            }
        }, [user?.id, setActiveSession])
    );

    // Sync local location to global store
    useEffect(() => {
        if (locationPerm.location) {
            setGlobalLocation(locationPerm.location);
        }
    }, [locationPerm.location]);

    // Watch for location changes and fetch nearby stations
    // Use global location to ensure we get location from map screen too@index.tsx:current_problems 
    useEffect(() => {
        const currentLocation = locationPerm.location || globalLocation;
        if (currentLocation) {
            fetchNearbyStations(
                currentLocation.coords.latitude,
                currentLocation.coords.longitude
            );
        }
    }, [locationPerm.location, globalLocation]);

    const fetchNearbyStations = async (lat: number, lng: number) => {
        try {
            setLoading(true);
            const results = await searchNearbyStations({
                latitude: lat,
                longitude: lng,
                radiusKm: 5,
                maxResults: 20,
            });
            setStations(results);
        } catch (error) {
            console.error("Error fetching stations:", error);
            setStations([]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <GradientBackground preset="main" dismissKeyboard={false} className="flex-1 px-6">
            <SafeAreaView className="flex-1" edges={["top", "left", "right"]}>
                <ScrollView
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ paddingBottom: 0, paddingTop: 10 }}
                >
                    <View className="flex-row justify-between items-center">
                        <View>
                            <Text className="text-white text-2xl font-bold">
                                Welcome to EVGo
                            </Text>
                            <Text className="text-text-secondary text-sm mt-1">
                                Powering Every Mile
                            </Text>
                        </View>
                        <TouchableOpacity
                            activeOpacity={0.7}
                            className="w-12 h-12 rounded-full bg-white/10 items-center justify-center"
                            onPress={() => router.push("/payment")}
                        >
                            <Ionicons name="notifications-outline" size={24} color="white" />
                        </TouchableOpacity>
                    </View>

                    <DebtBanner isVisible={unpaidCount > 0} unpaidCount={unpaidCount} />

                    <View className="flex-1">
                        {/* Map Preview */}
                        <View>
                            <View className="flex-row items-center justify-between">
                                {/* <Text style={[styles.h3, { marginTop: 30 }]}>Map</Text> */}
                                <TouchableOpacity
                                    // @ts-ignore - Dynamic route
                                    onPress={() => router.push("/map")}
                                    activeOpacity={0.7}
                                    className="mt-7"
                                >
                                </TouchableOpacity>
                            </View>

                            {/* Conditional: Show live map if permission granted, otherwise static image */}
                            {(locationPerm.hasPermission || globalLocation) && (locationPerm.location || globalLocation) ? (
                                // Has Location Permission - Show Live Map
                                <TouchableOpacity
                                    style={{ height: 208, marginTop: 16 }}
                                    className="rounded-2xl overflow-hidden"
                                    // @ts-ignore - Dynamic route
                                    onPress={() => router.push("/map")}
                                    activeOpacity={0.9}
                                >
                                    <MapView
                                        style={{ flex: 1 }}
                                        mapType="standard"
                                        scrollEnabled={false}
                                        zoomEnabled={false}
                                        showsUserLocation={true} // Use default user location marker
                                        initialRegion={{
                                            latitude: (locationPerm.location || globalLocation)!.coords.latitude,
                                            longitude: (locationPerm.location || globalLocation)!.coords.longitude,
                                            latitudeDelta: 0.05,
                                            longitudeDelta: 0.05,
                                        }}
                                    >
                                        {/* <UrlTile
                                        urlTemplate="https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                        maximumZ={19}
                                        flipY={false}
                                        zIndex={1}
                                        tileSize={256}
                                    /> */}

                                        {/* User Location Marker - Removed to use default showsUserLocation */}

                                        {/* Station Markers */}
                                        {stations.map((station) => {
                                            const isAvailable = station.status === StationStatus.ACTIVE;
                                            // Use require directly here since we can't easily import constants from map/index.tsx without refactoring
                                            // Ideally these constants should be in a shared constants file
                                            const pinImage = isAvailable
                                                ? require("@/assets/images/pin-active.png")
                                                : require("@/assets/images/pin-inactive.png");

                                            return (
                                                <Marker
                                                    key={station.id}
                                                    coordinate={{
                                                        latitude: station.latitude,
                                                        longitude: station.longitude,
                                                    }}
                                                    zIndex={2}
                                                    image={pinImage}
                                                />
                                            );
                                        })}
                                    </MapView>
                                </TouchableOpacity>
                            ) : (
                                // No Location Permission - Show Static Image
                                <TouchableOpacity
                                    style={{ height: 208, marginTop: 16 }}
                                    className="rounded-2xl overflow-hidden relative"
                                    // @ts-ignore - Dynamic route
                                    onPress={() => router.push("/map")}
                                    activeOpacity={0.9}
                                >
                                    <Image
                                        source={require("@/assets/images/default-map.png")}
                                        style={{ width: "100%", height: "100%" }}
                                        resizeMode="cover"
                                    />
                                    {/* Overlay prompt */}
                                    <View className="absolute inset-0 bg-black/30 items-center justify-center">
                                        <Text className="text-white text-sm font-semibold">
                                            Tap to view map
                                        </Text>
                                    </View>
                                </TouchableOpacity>
                            )}
                        </View>

                        {/* Last Charging Information */}
                        <View className="mt-8">
                            <Text style={styles.h3}>
                                Last Charging Information
                            </Text>
                            <View className="flex-row gap-3 mt-4">
                                <ChargingInfoCard
                                    label="Percentage"
                                    value={85} // Mock percentage as it's not stored in ChargingSessionResponse currently
                                    subValue="%"
                                    iconName="battery-check"
                                    iconColor="#8B5CF6"
                                    bgColor="rgba(139, 92, 246, 0.1)"
                                    iconBgColor="rgba(139, 92, 246, 0.2)"
                                />
                                <ChargingInfoCard
                                    label="kWh"
                                    value={lastSession?.totalKwh ?? mockChargingInfo.kwh}
                                    iconName="flash"
                                    iconColor="#F59E0B"
                                    bgColor="rgba(245, 158, 11, 0.1)"
                                    iconBgColor="rgba(245, 158, 11, 0.2)"
                                />
                                <ChargingInfoCard
                                    label="Total"
                                    value={lastSession ? 'Paid' : `${mockChargingInfo.currency}${mockChargingInfo.total.toFixed(2).replace('.', ',')}`}
                                    iconName="currency-usd"
                                    iconColor="#10B981"
                                    bgColor="rgba(16, 185, 129, 0.1)"
                                    iconBgColor="rgba(16, 185, 129, 0.2)"
                                />
                            </View>
                        </View>

                        {/* History */}
                        <View className="mt-8 mb-10">
                            <View className="flex-row items-center justify-between mb-4">
                                <Text style={styles.h3}>History</Text>
                                <TouchableOpacity onPress={() => console.log("See All")}>
                                    <Text className="text-text-secondary text-sm">See All</Text>
                                </TouchableOpacity>
                            </View>

                            <ScrollView
                                style={{ maxHeight: 200 }}
                                nestedScrollEnabled={true}
                                showsVerticalScrollIndicator={false}
                            >
                                {recentSessions.length > 0 ? recentSessions.slice(0, 5).map((session) => {
                                    const dateObj = new Date(session.startTime || session.createdAt || 0);
                                    const dateStr = `${dateObj.getDate()} ${dateObj.toLocaleString('en-us', { month: 'short' })} ${dateObj.getFullYear()}`;
                                    
                                    // Calculate duration in minutes if possible
                                    let duration = "0 min";
                                    if (session.startTime && session.endTime) {
                                        const diff = new Date(session.endTime).getTime() - new Date(session.startTime).getTime();
                                        const mins = Math.floor(diff / 60000);
                                        duration = `${mins} min`;
                                    }

                                    return (
                                        <HistoryItem
                                            key={session.id}
                                            date={dateStr}
                                            duration={duration}
                                            energy={session.totalKwh || 0}
                                            onPress={() => console.log("View session details", session.id)}
                                        />
                                    );
                                }) : (
                                    <View className="py-4 items-center">
                                        <Text className="text-white/50 text-sm">No charging history yet</Text>
                                    </View>
                                )}
                            </ScrollView>
                        </View>
                    </View>
                </ScrollView>
            </SafeAreaView>

            {/* Active Charging Notification */}
            <View className="absolute bottom-2 left-0 right-0">
                <ActiveChargingNotification />
            </View>

            {/* Location Permission Modal */}
            <LocationPermissionModal
                visible={locationPerm.showPermissionModal}
                status={locationPerm.permissionStatus}
                onPrimaryAction={() => {
                    if (locationPerm.permissionStatus === "permission_required") {
                        locationPerm.requestLocationPermission();
                    }
                }}
                onEnterManually={locationPerm.handleEnterManually}
                onCancel={() => locationPerm.setShowPermissionModal(false)}
            />

            {/* Manual Location Input */}
            <ManualLocationInput
                visible={locationPerm.showManualInput}
                onClose={() => locationPerm.setShowPermissionModal(false)}
                onLocationSet={(lat, lng) => {
                    locationPerm.handleManualLocationSet(lat, lng);
                }}
            />
        </GradientBackground>
    );
}

const styles = StyleSheet.create({
    h3: { fontSize: 16, fontWeight: "600", color: "white" },
});
