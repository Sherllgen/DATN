import React, { useState, useEffect, useRef } from "react";
import {
    View,
    Text,
    TouchableOpacity,
    TextInput,
    FlatList,
    ActivityIndicator,
} from "react-native";
import MapView, { UrlTile, Marker, Region, Polyline } from "react-native-maps";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons, MaterialCommunityIcons } from "@expo/vector-icons";
import * as Location from "expo-location";
import { router } from "expo-router";

import GradientBackground from "@/components/ui/GradientBackground";
import LocationPermissionModal, { LocationModalStatus } from "@/components/map/LocationPermissionModal";
import ManualLocationInput from "@/components/map/ManualLocationInput";
import StationQuickInfo from "@/components/map/StationQuickInfo";
import StationCard from "@/components/station/StationCard";
import { searchNearbyStations, searchStationsInBound } from "@/apis/stationApi/stationApi";
import { StationSearchResult, StationStatus } from "@/types/station.types";
import { getRoute, RouteResponse } from "@/apis/directionApi";
import mapboxPolyline from "@mapbox/polyline";

export default function MapScreen() {
    const [location, setLocation] = useState<Location.LocationObject | null>(
        null
    );
    const [showPermissionModal, setShowPermissionModal] = useState(false);
    const [permissionStatus, setPermissionStatus] = useState<LocationModalStatus>("idle");
    const [showManualInput, setShowManualInput] = useState(false);
    const [stations, setStations] = useState<StationSearchResult[]>([]);
    const [selectedStation, setSelectedStation] =
        useState<StationSearchResult | null>(null);
    const [routeCoordinates, setRouteCoordinates] = useState<{ latitude: number; longitude: number }[]>([]);
    const [isNavigating, setIsNavigating] = useState(false);
    const [showQuickInfo, setShowQuickInfo] = useState(false);
    const [loading, setLoading] = useState(false);
    const [viewMode, setViewMode] = useState<"map" | "list">("map");
    const [searchQuery, setSearchQuery] = useState("");

    const mapRef = useRef<MapView>(null);
    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    // Initial region (Ho Chi Minh City)
    const [region, setRegion] = useState<Region>({
        latitude: 10.8231,
        longitude: 106.6297,
        latitudeDelta: 0.15,  // Increased from 0.05 to show wider area
        longitudeDelta: 0.15, // Increased from 0.05 to show wider area
    });

    useEffect(() => {
        checkLocationPermission();
        // Load stations immediately with default region
        fetchStationsInBound(region);
    }, []);

    const checkLocationPermission = async () => {
        const { status } = await Location.getForegroundPermissionsAsync();

        if (status !== "granted") {
            setPermissionStatus("permission_required");
            setShowPermissionModal(true);
        } else {
            getCurrentLocation();
        }
    };

    const requestLocationPermission = async () => {
        const { status } = await Location.requestForegroundPermissionsAsync();

        if (status === "granted") {
            setShowPermissionModal(false);
            setPermissionStatus("idle");
            getCurrentLocation();
        } else {
            setShowPermissionModal(false);
            // Show error or fallback - handled by initial check or manual input
        }
    };

    const getCurrentLocation = async () => {
        try {
            // Reset status before trying
            setShowPermissionModal(false);

            let userLocation;

            try {
                // First try with Balanced accuracy and timeout
                userLocation = await Location.getCurrentPositionAsync({
                    accuracy: Location.Accuracy.Balanced,
                    timeInterval: 10000, // 10 second timeout
                });
            } catch (balancedError) {
                console.warn("Balanced accuracy failed, trying Low accuracy:", balancedError);

                // Fallback to Low accuracy (uses network location, similar to Google Maps)
                userLocation = await Location.getCurrentPositionAsync({
                    accuracy: Location.Accuracy.Low,
                    timeInterval: 5000, // 5 second timeout for fallback
                });
            }

            setLocation(userLocation);

            const newRegion = {
                latitude: userLocation.coords.latitude,
                longitude: userLocation.coords.longitude,
                latitudeDelta: 0.05,
                longitudeDelta: 0.05,
            };

            setRegion(newRegion);
            mapRef.current?.animateToRegion(newRegion, 1000);

            // Fetch stations in new region's bounds
            fetchStationsInBound(newRegion);
        } catch (error) {
            console.error("Error getting location (all attempts failed):", error);
            // Show modal with error status
            setPermissionStatus("gps_error");
            setShowPermissionModal(true);
        }
    };

    const handleManualLocationSet = (latitude: number, longitude: number) => {
        console.log("Manual location set:", latitude, longitude);

        // Create location object
        const manualLocation = {
            coords: {
                latitude,
                longitude,
                altitude: null,
                accuracy: null,
                altitudeAccuracy: null,
                heading: null,
                speed: null,
            },
            timestamp: Date.now(),
        };

        setLocation(manualLocation as any);

        // Update map region
        const newRegion = {
            latitude,
            longitude,
            latitudeDelta: 0.15,
            longitudeDelta: 0.15,
        };
        setRegion(newRegion);
        mapRef.current?.animateToRegion(newRegion, 1000);

        // Fetch stations in new region's bounds
        fetchStationsInBound(newRegion);
    };

    const handleEnterManually = () => {
        setShowPermissionModal(false);
        setShowManualInput(true);
    };

    const fetchStationsInBound = async (mapRegion: Region) => {
        try {
            setLoading(true);

            // Calculate bounding box from region
            const minLat = mapRegion.latitude - mapRegion.latitudeDelta / 2;
            const maxLat = mapRegion.latitude + mapRegion.latitudeDelta / 2;
            const minLng = mapRegion.longitude - mapRegion.longitudeDelta / 2;
            const maxLng = mapRegion.longitude + mapRegion.longitudeDelta / 2;

            const results = await searchStationsInBound({
                minLat,
                maxLat,
                minLng,
                maxLng,
                // Pass user location if available for distance calculation
                userLat: location?.coords.latitude,
                userLng: location?.coords.longitude,
                maxResults: 50,
            });

            setStations(results);
            console.log(`Loaded ${results.length} stations from in-bound API`);
        } catch (error) {
            console.error("Error fetching stations:", error);
            setStations([]);
        } finally {
            setLoading(false);
        }
    };

    const handleRegionChangeComplete = (newRegion: Region) => {
        setRegion(newRegion);

        // Clear existing debounce timer
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        // Set new debounce timer (800ms)
        debounceTimerRef.current = setTimeout(() => {
            fetchStationsInBound(newRegion);
        }, 800);
    };

    // useEffect(() => {
    //     console.log("STATIONS STATE:", stations);
    //     console.log("STATIONS COUNT:", stations.length);
    //     if (stations.length > 0) {
    //         console.log("FIRST STATION:", JSON.stringify(stations[0], null, 2));
    //     }
    // }, [stations]);

    const handleMarkerPress = (station: StationSearchResult) => {
        setSelectedStation(station);
        setShowQuickInfo(true);
    };

    const handleViewDetails = () => {
        if (selectedStation) {
            setShowQuickInfo(false);
            router.push(`/station/${selectedStation.id}`);
        }
    };

    const handleBook = () => {
        if (selectedStation) {
            setShowQuickInfo(false);
            // Navigate to booking/charging flow
            console.log("Book station:", selectedStation.id);
        }
    };

    const handleNavigate = async () => {
        if (!selectedStation || !location) return;

        setShowQuickInfo(false);
        setIsNavigating(true);

        try {
            setLoading(true);
            const routeData = await getRoute(
                location.coords.latitude,
                location.coords.longitude,
                selectedStation.latitude,
                selectedStation.longitude
            );

            if (routeData && routeData.encodedPolyline) {
                const points = mapboxPolyline.decode(routeData.encodedPolyline);
                const coords = points.map((point: [number, number]) => ({
                    latitude: point[0],
                    longitude: point[1],
                }));
                setRouteCoordinates(coords);

                // Fit map to route
                mapRef.current?.fitToCoordinates(coords, {
                    edgePadding: { top: 50, right: 50, bottom: 50, left: 50 },
                    animated: true,
                });
            }
        } catch (error) {
            console.error("Navigation failed:", error);
            // Show error toast
        } finally {
            setLoading(false);
        }
    };

    const cancelNavigation = () => {
        setIsNavigating(false);
        setRouteCoordinates([]);
        centerToUserLocation();
    };

    const centerToUserLocation = () => {
        if (location) {
            const newRegion = {
                latitude: location.coords.latitude,
                longitude: location.coords.longitude,
                latitudeDelta: 0.05,
                longitudeDelta: 0.05,
            };
            mapRef.current?.animateToRegion(newRegion, 1000);
        }
    };

    const getMarkerColor = (station: StationSearchResult) => {
        // ACTIVE = green, INACTIVE = red
        return station.status === StationStatus.ACTIVE
            ? "#4CAF50" // Green - ACTIVE
            : "#EF4444"; // Red - INACTIVE
    };

    const renderMarker = (station: StationSearchResult) => {
        const pinColor = getMarkerColor(station);

        return (
            <Marker
                key={station.id}
                coordinate={{
                    latitude: station.latitude,
                    longitude: station.longitude,
                }}
                onPress={() => handleMarkerPress(station)}
            >
                <View className="items-center justify-center">
                    <View
                        style={{ backgroundColor: pinColor }}
                        className="w-10 h-10 rounded-full items-center justify-center border-2 border-white shadow-sm"
                    >
                        <MaterialCommunityIcons
                            name="ev-station"
                            size={20}
                            color="white"
                        />
                    </View>
                    {/* Triangle pointer */}
                    <View
                        style={{ borderTopColor: pinColor }}
                        className="w-0 h-0 border-l-[6px] border-r-[6px] border-t-[8px] border-l-transparent border-r-transparent -mt-[1px]"
                    />
                </View>
            </Marker>
        );
    };

    return (
        <GradientBackground preset="main" className="flex-1">
            <SafeAreaView className="flex-1">
                {/* Header with Search */}
                <View className="px-4 pb-4 pt-2">
                    {/* Search Bar Row */}
                    <View className="flex-row items-center gap-3">
                        {/* Back Button */}
                        <TouchableOpacity
                            onPress={() => router.back()}
                            className="w-12 h-12 bg-white/10 rounded-full items-center justify-center border border-white/20"
                            activeOpacity={0.7}
                        >
                            <Ionicons name="arrow-back" size={24} color="#FFF" />
                        </TouchableOpacity>

                        {/* Search Input */}
                        <View className="flex-1 flex-row items-center bg-white rounded-full px-4 py-3">
                            <Ionicons
                                name="search"
                                size={20}
                                color="#9BA1A6"
                            />
                            <TextInput
                                className="flex-1 ml-3 text-base text-[#11181C]"
                                placeholder="Search station"
                                placeholderTextColor="#9BA1A6"
                                value={searchQuery}
                                onChangeText={setSearchQuery}
                            />
                        </View>

                        {/* Filter Button */}
                        <TouchableOpacity
                            className="w-12 h-12 bg-secondary rounded-full items-center justify-center shadow-lg"
                            activeOpacity={0.7}
                        >
                            <Ionicons name="options" size={24} color="#FFF" />
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Map or List View */}
                {viewMode === "map" ? (
                    <View className="flex-1 relative">
                        {/* Debug Info */}
                        {__DEV__ && (
                            <View className="absolute top-2 left-2 bg-black/70 px-3 py-2 rounded-lg z-50">
                                <Text className="text-white text-xs">
                                    Stations: {stations.length}
                                </Text>
                            </View>
                        )}

                        {/* Map */}
                        <MapView
                            ref={mapRef}
                            style={{ flex: 1 }}
                            mapType="standard"
                            region={region}
                            onRegionChangeComplete={handleRegionChangeComplete}
                        >
                            {/* <UrlTile
                                urlTemplate="https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                maximumZ={19}
                                flipY={false}
                                zIndex={1}
                                tileSize={256}
                            /> */}

                            {/* User Location Marker */}
                            {location && (
                                <Marker
                                    coordinate={{
                                        latitude: location.coords.latitude,
                                        longitude: location.coords.longitude,
                                    }}
                                    zIndex={10}
                                >
                                    <View className="w-12 h-12 rounded-full bg-info/30 items-center justify-center">
                                        <View className="w-6 h-6 rounded-full bg-info border-2 border-white" />
                                    </View>
                                </Marker>
                            )}

                            {/* Station Markers */}
                            {stations.map((station) => (
                                <StationMarker
                                    key={station.id}
                                    station={station}
                                    onPress={() => handleMarkerPress(station)}
                                />
                            ))}

                            {/* Navigation Polyline */}
                            {isNavigating && routeCoordinates.length > 0 && (
                                <Polyline
                                    coordinates={routeCoordinates}
                                    strokeColor="#3b82f6" // Tailwind info color
                                    strokeWidth={4}
                                />
                            )}

                            {/* Destination Marker during Navigation */}
                            {isNavigating && selectedStation && (
                                <Marker
                                    coordinate={{
                                        latitude: selectedStation.latitude,
                                        longitude: selectedStation.longitude,
                                    }}
                                    title="Destination"
                                >
                                    <View className="items-center">
                                        <View className="bg-white px-2 py-1 rounded shadow mb-1">
                                            <Text className="text-xs font-bold">Destination</Text>
                                        </View>
                                        <MaterialCommunityIcons name="flag-checkered" size={30} color="#EF4444" />
                                    </View>
                                </Marker>
                            )}
                        </MapView>

                        {/* Loading Indicator */}
                        {loading && (
                            <View className="absolute top-4 left-0 right-0 items-center">
                                <View className="bg-white/90 px-4 py-2 rounded-full">
                                    <ActivityIndicator color="#00A452" />
                                </View>
                            </View>
                        )}

                        {/* Map Controls */}
                        <View className="absolute bottom-6 right-4 gap-3">
                            {/* Toggle List View */}
                            <TouchableOpacity
                                className="w-12 h-12 rounded-full bg-secondary items-center justify-center"
                                activeOpacity={0.7}
                                onPress={() => setViewMode("list")}
                            >
                                <Ionicons name="list" size={24} color="#FFF" />
                            </TouchableOpacity>

                            {/* Location Sharing / Settings */}
                            <TouchableOpacity
                                className="w-12 h-12 rounded-full bg-secondary items-center justify-center"
                                activeOpacity={0.7}
                            >
                                <Ionicons
                                    name="people"
                                    size={24}
                                    color="#FFF"
                                />
                            </TouchableOpacity>

                            {/* Center to Location */}
                            <TouchableOpacity
                                className="w-12 h-12 rounded-full bg-secondary items-center justify-center"
                                activeOpacity={0.7}
                                onPress={centerToUserLocation}
                            >
                                <Ionicons
                                    name="locate"
                                    size={24}
                                    color="#FFF"
                                />
                            </TouchableOpacity>
                        </View>

                        {/* Cancel Navigation Button */}
                        {isNavigating && (
                            <View className="absolute bottom-24 left-4 right-4 items-center">
                                <TouchableOpacity
                                    className="bg-red-500 px-6 py-3 rounded-full flex-row items-center shadow-lg"
                                    onPress={cancelNavigation}
                                    activeOpacity={0.8}
                                >
                                    <Ionicons name="close-circle" size={24} color="white" />
                                    <Text className="text-white font-bold ml-2">Exit Navigation</Text>
                                </TouchableOpacity>
                            </View>
                        )}
                    </View>
                ) : (
                    /* List View */
                    <View className="flex-1 px-4">
                        <FlatList
                            data={stations}
                            keyExtractor={(item) => item.id.toString()}
                            renderItem={({ item }) => (
                                <StationCard
                                    station={item}
                                    onPress={() => handleMarkerPress(item)}
                                />
                            )}
                            contentContainerStyle={{ paddingBottom: 100 }}
                            ListHeaderComponent={
                                <TouchableOpacity
                                    className="mb-4 py-2"
                                    onPress={() => setViewMode("map")}
                                >
                                    <Text className="text-secondary text-sm font-medium">
                                        <Ionicons name="chevron-back-outline" size={12} color="secondary" /> Back to Map
                                    </Text>
                                </TouchableOpacity>
                            }
                        />
                    </View>
                )}
            </SafeAreaView>

            {/* Modals */}
            <LocationPermissionModal
                visible={showPermissionModal}
                status={permissionStatus}
                onPrimaryAction={() => {
                    if (permissionStatus === "permission_required") {
                        requestLocationPermission();
                    } else {
                        // Retry getting location
                        getCurrentLocation();
                    }
                }}
                onEnterManually={handleEnterManually}
                onCancel={() => setShowPermissionModal(false)}
            />

            <StationQuickInfo
                visible={showQuickInfo}
                station={selectedStation}
                onClose={() => setShowQuickInfo(false)}
                onViewDetails={handleViewDetails}
                onBook={handleBook}
                onNavigate={handleNavigate}
            />

            {/* Manual Location Input Modal */}
            <ManualLocationInput
                visible={showManualInput}
                onClose={() => setShowManualInput(false)}
                onLocationSet={handleManualLocationSet}
            />
        </GradientBackground>
    );
}

// Optimized Marker Component to prevent OOM
const StationMarker = React.memo(({ station, onPress }: { station: StationSearchResult; onPress: () => void }) => {
    const [tracksViewChanges, setTracksViewChanges] = useState(true);
    const markerRef = useRef<any>(null);

    // Stop tracking view changes after initial render to save memory
    useEffect(() => {
        const timer = setTimeout(() => {
            setTracksViewChanges(false);
        }, 100); // Small delay to allow render
        return () => clearTimeout(timer);
    }, []);

    const pinColor = station.status === StationStatus.ACTIVE ? "#4CAF50" : "#EF4444";

    return (
        <Marker
            coordinate={{
                latitude: station.latitude,
                longitude: station.longitude,
            }}
            onPress={onPress}
            tracksViewChanges={tracksViewChanges}
            ref={markerRef}
        >
            <View className="items-center justify-center">
                <View
                    style={{ backgroundColor: pinColor }}
                    className="w-10 h-10 rounded-full items-center justify-center border-2 border-white shadow-sm"
                >
                    <MaterialCommunityIcons
                        name="ev-station"
                        size={20}
                        color="white"
                    />
                </View>
                {/* Triangle pointer */}
                <View
                    style={{ borderTopColor: pinColor }}
                    className="w-0 h-0 border-l-[6px] border-r-[6px] border-t-[8px] border-l-transparent border-r-transparent -mt-[1px]"
                />
            </View>
        </Marker>
    );
});
