import { StyleSheet, Text, TouchableOpacity, View, Image } from "react-native";
import MapView, { UrlTile, Marker } from 'react-native-maps';
import { SafeAreaView } from "react-native-safe-area-context";
import { router } from "expo-router";
import { useState, useEffect } from "react";

import GradientBackground from "@/components/ui/GradientBackground";
import LocationPermissionModal from "@/components/map/LocationPermissionModal";
import ManualLocationInput from "@/components/map/ManualLocationInput";
import { searchNearbyStations } from "@/apis/stationApi/stationApi";
import { StationSearchResult, StationStatus } from "@/types/station.types";
import { useLocationPermission } from "@/hooks/useLocationPermission";
import { useLocationStore } from "@/stores/locationStore";

export default function HomePage() {
    const locationPerm = useLocationPermission(true); // Auto-check on mount
    const globalLocation = useLocationStore((state) => state.location);
    const setGlobalLocation = useLocationStore((state) => state.setLocation);
    const [stations, setStations] = useState<StationSearchResult[]>([]);
    const [loading, setLoading] = useState(false);

    // Sync local location to global store
    useEffect(() => {
        if (locationPerm.location) {
            setGlobalLocation(locationPerm.location);
        }
    }, [locationPerm.location]);

    // Watch for location changes and fetch nearby stations
    // Use global location to ensure we get location from map screen too
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
        <GradientBackground className="flex-1 px-6 pt-6">
            <SafeAreaView className="flex-1">
                <View>
                    <Text style={styles.h3}>EVGo</Text>
                    <Text style={[styles.h3, { marginTop: 20 }]}>
                        Hi, Authur!
                    </Text>
                </View>

                <View className="flex-1 bg-white/10 -mx-6 mt-8 px-6 rounded-t-[3em]">
                    {/* Map Preview */}
                    <View>
                        <View className="flex-row items-center justify-between">
                            <Text style={[styles.h3, { marginTop: 30 }]}>Map</Text>
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

                    {/* Activities */}
                    <View>
                        <Text style={[styles.h3, { marginTop: 30 }]}>
                            Activities
                        </Text>
                        <View className="flex flex-row justify-between mt-4">
                            <View className="bg-white/10 rounded-2xl w-[64%] h-52"></View>
                            <View className="bg-white/10 rounded-2xl w-[33%] h-52"></View>
                        </View>
                    </View>
                </View>
            </SafeAreaView>

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
