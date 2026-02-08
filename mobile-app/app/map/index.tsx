import React, { useState, useEffect, useRef } from "react";
import {
    View,
    Text,
    TouchableOpacity,
    TextInput,
    FlatList,
    ActivityIndicator,
} from "react-native";
import MapView, { UrlTile, Marker, Region } from "react-native-maps";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons, MaterialCommunityIcons } from "@expo/vector-icons";
import * as Location from "expo-location";
import { router } from "expo-router";

import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import LocationPermissionModal from "@/components/map/LocationPermissionModal";
import StationQuickInfo from "@/components/map/StationQuickInfo";
import StationCard from "@/components/station/StationCard";
// import { searchNearbyStations } from "@/apis/stationApi/stationApi"; // Will use later
import { StationSearchResult, StationStatus } from "@/types/station.types";
import { mockStationsNearby } from "@/data/mockStations";

export default function MapScreen() {
    const [location, setLocation] = useState<Location.LocationObject | null>(
        null
    );
    const [showPermissionModal, setShowPermissionModal] = useState(false);
    const [stations, setStations] = useState<StationSearchResult[]>([]);
    const [selectedStation, setSelectedStation] =
        useState<StationSearchResult | null>(null);
    const [showQuickInfo, setShowQuickInfo] = useState(false);
    const [loading, setLoading] = useState(false);
    const [viewMode, setViewMode] = useState<"map" | "list">("map");
    const [searchQuery, setSearchQuery] = useState("");

    const mapRef = useRef<MapView>(null);

    // Initial region (Ho Chi Minh City)
    const [region, setRegion] = useState<Region>({
        latitude: 10.8231,
        longitude: 106.6297,
        latitudeDelta: 0.05,
        longitudeDelta: 0.05,
    });

    useEffect(() => {
        checkLocationPermission();
        // Load stations immediately with default location
        fetchNearbyStations(10.8231, 106.6297);
    }, []);

    const checkLocationPermission = async () => {
        const { status } = await Location.getForegroundPermissionsAsync();

        if (status !== "granted") {
            setShowPermissionModal(true);
        } else {
            getCurrentLocation();
        }
    };

    const requestLocationPermission = async () => {
        const { status } = await Location.requestForegroundPermissionsAsync();

        if (status === "granted") {
            setShowPermissionModal(false);
            getCurrentLocation();
        } else {
            setShowPermissionModal(false);
            // Show error or fallback
        }
    };

    const getCurrentLocation = async () => {
        try {
            const userLocation = await Location.getCurrentPositionAsync({
                accuracy: Location.Accuracy.Balanced,
            });

            setLocation(userLocation);

            const newRegion = {
                latitude: userLocation.coords.latitude,
                longitude: userLocation.coords.longitude,
                latitudeDelta: 0.05,
                longitudeDelta: 0.05,
            };

            setRegion(newRegion);
            mapRef.current?.animateToRegion(newRegion, 1000);

            // Fetch nearby stations
            fetchNearbyStations(
                userLocation.coords.latitude,
                userLocation.coords.longitude
            );
        } catch (error) {
            console.warn("Error getting location:", error);
            // Fallback to default location (Ho Chi Minh City)
            const defaultLat = 10.8231;
            const defaultLng = 106.6297;

            console.log("Using default location (Ho Chi Minh City)");
            // Fetch stations at default location
            fetchNearbyStations(defaultLat, defaultLng);
        }
    };

    const fetchNearbyStations = async (lat: number, lng: number) => {
        try {
            setLoading(true);

            // Simulate API delay
            await new Promise(resolve => setTimeout(resolve, 500));

            // Use mock data for UI testing
            // TODO: Replace with real API call when UI is approved
            // const results = await searchNearbyStations({
            //     latitude: lat,
            //     longitude: lng,
            //     radiusKm: 10,
            //     maxResults: 50,
            // });

            setStations(mockStationsNearby);
            console.log(`Loaded ${mockStationsNearby.length} stations`);
        } catch (error) {
            console.error("Error fetching stations:", error);
        } finally {
            setLoading(false);
        }
    };

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
        if (station.status === StationStatus.MAINTENANCE) {
            return "#F59E0B"; // Yellow for maintenance
        }
        if (station.availableChargersCount > 0) {
            return "#4CAF50"; // Green for available
        }
        return "#EF4444"; // Red for occupied
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
                            mapType="none"
                            region={region}
                            onRegionChangeComplete={setRegion}
                        >
                            <UrlTile
                                urlTemplate="https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                maximumZ={19}
                                flipY={false}
                                zIndex={1}
                                tileSize={256}
                            />

                            {/* User Location Marker */}
                            {location && (
                                <Marker
                                    coordinate={{
                                        latitude: location.coords.latitude,
                                        longitude: location.coords.longitude,
                                    }}
                                    zIndex={10}
                                >
                                    <View className="w-12 h-12 rounded-full bg-secondary/30 items-center justify-center">
                                        <View className="w-6 h-6 rounded-full bg-secondary border-2 border-white" />
                                    </View>
                                </Marker>
                            )}

                            {/* Station Markers */}
                            {stations.map(renderMarker)}
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
                                        ← Back to Map
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
                onEnableLocation={requestLocationPermission}
                onCancel={() => setShowPermissionModal(false)}
            />

            <StationQuickInfo
                visible={showQuickInfo}
                station={selectedStation}
                onClose={() => setShowQuickInfo(false)}
                onViewDetails={handleViewDetails}
                onBook={handleBook}
            />
        </GradientBackground>
    );
}
