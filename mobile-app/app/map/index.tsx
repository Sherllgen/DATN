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
import { getRoute, RouteResponse } from "@/apis/stationApi/directionApi";
import mapboxPolyline from "@mapbox/polyline";
import { haversineDistance, simplifyPolyline } from "@/utils/location";

const PIN_ACTIVE = require("@/assets/images/pin-active.png");
const PIN_INACTIVE = require("@/assets/images/pin-inactive.png");
const PIN_NAVIGATE = require("@/assets/images/pin-navigate.png");

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
    const [isInitialLoading, setIsInitialLoading] = useState(true);
    const [viewMode, setViewMode] = useState<"map" | "list">("map");
    const [searchQuery, setSearchQuery] = useState("");

    const mapRef = useRef<MapView>(null);
    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const lastFetchedRegionRef = useRef<Region | null>(null);
    const abortControllerRef = useRef<AbortController | null>(null);

    // Initial region (Ho Chi Minh City)
    // Use useRef for initialRegion to prevent re-renders
    const initialRegionRef = useRef<Region>({
        latitude: 10.8231,
        longitude: 106.6297,
        latitudeDelta: 0.15,
        longitudeDelta: 0.15,
    });

    useEffect(() => {
        checkLocationPermission();
        // Load stations immediately with default region
        fetchStationsInBound(initialRegionRef.current);
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

            // Remove setRegion(newRegion) - use animateToRegion only
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
        // Remove setRegion(newRegion) - use animateToRegion only
        mapRef.current?.animateToRegion(newRegion, 1000);

        // Fetch stations in new region's bounds
        fetchStationsInBound(newRegion);
    };

    const handleEnterManually = () => {
        setShowPermissionModal(false);
        setShowManualInput(true);
    };

    const fetchStationsInBound = async (mapRegion: Region, isBackground = false) => {
        try {
            // STOP fetching if user is navigating. 
            // This prevents re-renders that clear the polyline or cause OOM.
            if (isNavigating) {
                return;
            }

            // Check distance threshold if this is a background fetch (panning)
            if (isBackground && lastFetchedRegionRef.current) {
                const distance = haversineDistance(
                    lastFetchedRegionRef.current.latitude,
                    lastFetchedRegionRef.current.longitude,
                    mapRegion.latitude,
                    mapRegion.longitude
                );

                // Only fetch if moved more than 2km
                if (distance < 2) {
                    return;
                }
            }

            if (!isBackground) {
                setIsInitialLoading(true);
            }

            // Cancel previous request
            if (abortControllerRef.current) {
                abortControllerRef.current.abort();
            }
            const controller = new AbortController();
            abortControllerRef.current = controller;

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
            }, controller.signal);

            setStations(results);
            lastFetchedRegionRef.current = mapRegion;
            console.log(`Loaded ${results.length} stations from in-bound API`);
        } catch (error: any) {
            if (error.name === 'AbortError' || error.message === 'canceled') {
                console.log('Fetch aborted');
                return;
            }
            console.error("Error fetching stations:", error);
            if (!isBackground) setStations([]);
        } finally {
            if (!isBackground) setIsInitialLoading(false);
        }
    };

    const handleRegionChangeComplete = (newRegion: Region) => {
        // Optimization: Do NOT update state `setRegion(newRegion)` here to avoid re-renders.
        // MapView manages its own internal state for panning.
        // We only update our local ref or use the value directly.

        // Clear existing debounce timer
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        // Set new debounce timer (800ms)
        debounceTimerRef.current = setTimeout(() => {
            // Check isNavigating inside the timeout as well to be safe
            // though fetchStationsInBound also checks it.
            if (!isNavigating) {
                fetchStationsInBound(newRegion, true); // isBackground = true
            }
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
            setIsInitialLoading(true);
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
                // Simplify polyline to reduce rendering load
                // 0.0001 degrees is roughly 11 meters, good balance for urban navigation
                const simplifiedCoords = simplifyPolyline(coords, 0.0001);

                setRouteCoordinates(simplifiedCoords);

                // Fit map to route
                mapRef.current?.fitToCoordinates(simplifiedCoords, {
                    edgePadding: { top: 50, right: 50, bottom: 50, left: 50 },
                    animated: true,
                });
            }
        } catch (error) {
            console.error("Navigation failed:", error);
            // Show error toast
        } finally {
            setIsInitialLoading(false);
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

    // Memoize markers to prevent re-renders
    const memoizedMarkers = React.useMemo(() => {
        return stations.map((station) => (
            <StationMarker
                key={station.id}
                station={station}
                onPress={() => handleMarkerPress(station)}
            />
        ));
    }, [stations]);



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

                {/* Map View Container - Persisted */}
                <View style={{ flex: 1, display: viewMode === "map" ? "flex" : "none" }}>
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
                            initialRegion={initialRegionRef.current}
                            onRegionChangeComplete={handleRegionChangeComplete}
                            showsUserLocation={true}
                        >
                            {/* <UrlTile
                                urlTemplate="https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                maximumZ={19}
                                flipY={false}
                                zIndex={1}
                                tileSize={256}
                            /> */}

                            {/* User Location Marker - Removed as showsUserLocation={true} is used */}

                            {/* Station Markers */}
                            {memoizedMarkers}

                            {/* Navigation Polyline */}
                            {isNavigating && routeCoordinates.length > 0 && (
                                <>
                                    {/* Main Polyline (Darker Blue) */}
                                    <Polyline
                                        coordinates={routeCoordinates}
                                        strokeColor="#1D4ED8" // Darker than info (#3B82F6)
                                        strokeWidth={5}
                                        zIndex={2}
                                    />
                                </>
                            )}

                            {/* Destination Marker during Navigation */}
                            {isNavigating && selectedStation && (
                                <Marker
                                    coordinate={{
                                        latitude: selectedStation.latitude,
                                        longitude: selectedStation.longitude,
                                    }}
                                    title="Destination"
                                    zIndex={10}
                                    image={PIN_NAVIGATE}
                                />
                            )}
                        </MapView>

                        {/* Loading Indicator */}
                        {isInitialLoading && (
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
                            {/* <TouchableOpacity
                                className="w-12 h-12 rounded-full bg-secondary items-center justify-center"
                                activeOpacity={0.7}
                            >
                                <Ionicons
                                    name="people"
                                    size={24}
                                    color="#FFF"
                                />
                            </TouchableOpacity> */}

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
                            <View className="absolute bottom-8 left-4 right-4 items-center">
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
                </View>

                {/* List View Container - Persisted */}
                <View style={{ flex: 1, display: viewMode === "list" ? "flex" : "none" }}>
                    <View className="flex-1 px-4">
                        <FlatList
                            data={stations}
                            keyExtractor={(item) => item.id.toString()}
                            renderItem={({ item }) => (
                                <StationCard
                                    station={item}
                                    onPress={() => router.push(`/station/${item.id}`)}
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
                </View>
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

// Optimized Marker Component using Image Asset
const StationMarker = React.memo(({ station, onPress }: { station: StationSearchResult; onPress: () => void }) => {
    const isAvailable = station.status === StationStatus.ACTIVE;
    const pinImage = isAvailable ? PIN_ACTIVE : PIN_INACTIVE;

    return (
        <Marker
            coordinate={{
                latitude: station.latitude,
                longitude: station.longitude,
            }}
            onPress={onPress}
            image={pinImage}
            tracksViewChanges={false} // Optimization: Stop tracking changes for static markers
        />
    );
});
