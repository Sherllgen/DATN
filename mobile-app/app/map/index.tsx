import React, { useState, useMemo } from "react";
import { View, Text, TouchableOpacity, TextInput, FlatList, ActivityIndicator } from "react-native";
import MapView, { Polyline } from "react-native-maps";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";

import GradientBackground from "@/components/ui/GradientBackground";
import LocationPermissionModal from "@/components/map/LocationPermissionModal";
import ManualLocationInput from "@/components/map/ManualLocationInput";
import StationQuickInfo from "@/components/map/StationQuickInfo";
import StationCard from "@/components/station/StationCard";
import StationMarker from "@/components/map/StationMarker";
import NavigationInfo from "@/components/map/NavigationInfo";
import { useMapLogic } from "@/hooks/useMapLogic";
import { useStationCache } from "@/stores/stationCacheStore";
import FilterBottomSheet from "@/components/map/FilterBottomSheet";
import { StationFilterParams, StationStatus } from "@/types/station.types";

/**
 * MapScreen - Pure UI Component
 * 
 * Refactored to follow Clean Architecture principles.
 * All business logic is handled by the useMapLogic custom hook.
 * 
 * Responsibilities (UI ONLY):
 * - Render map layout using GradientBackground
 * - Display MapView with markers and navigation polyline
 * - Show modals and loading states
 * - Pass callbacks from hook to UI elements
 */
export default function MapScreen() {
    const mapLogic = useMapLogic();
    const [showFilterSheet, setShowFilterSheet] = useState(false);
    const [activeFilters, setActiveFilters] = useState<StationFilterParams>();

    // Check if there are actual non-default filters applied
    const hasActiveFilters = useMemo(() => {
        if (!activeFilters || !mapLogic.filterMeta) return false;
        const meta = mapLogic.filterMeta;
        if (activeFilters.minPower !== undefined && activeFilters.minPower > meta.minPower) return true;
        if (activeFilters.maxPower !== undefined && activeFilters.maxPower < meta.maxPower) return true;
        if (activeFilters.connectorTypes && activeFilters.connectorTypes.length > 0) return true;
        if (activeFilters.status && activeFilters.status !== StationStatus.ACTIVE) return true;
        return false;
    }, [activeFilters, mapLogic.filterMeta]);

    // Memoize markers with smart state logic
    const markers = useMemo(
        () =>
            mapLogic.stations.map((station) => (
                <StationMarker
                    key={station.id}
                    station={station}
                    isNavigating={mapLogic.isNavigating}
                    isDestination={station.id === mapLogic.selectedStation?.id}
                    onPress={() => mapLogic.handleMarkerPress(station)}
                />
            )),
        [mapLogic.stations, mapLogic.isNavigating, mapLogic.selectedStation]
    );

    // Log polyline state for debugging
    if (mapLogic.isNavigating || mapLogic.routeCoordinates.length > 0) {
        console.log('[MapScreen Polyline State]', {
            isNavigating: mapLogic.isNavigating,
            coordsLength: mapLogic.routeCoordinates.length,
            shouldRender: mapLogic.isNavigating && mapLogic.routeCoordinates.length > 0
        });
    }

    return (
        <GradientBackground preset="main" className="flex-1">
            <SafeAreaView className="flex-1">
                {/* Header with Search */}
                <View className="px-4 pb-4 pt-2">
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
                            <Ionicons name="search" size={20} color="#9BA1A6" />
                            <TextInput
                                className="flex-1 ml-3 mb-1 text-base text-[#11181C]"
                                placeholder="Search station"
                                placeholderTextColor="#9BA1A6"
                                value={mapLogic.searchQuery}
                                onChangeText={mapLogic.setSearchQuery}
                                returnKeyType="search"
                                autoCorrect={false}
                                onSubmitEditing={() => {
                                    mapLogic.setViewMode("list");
                                    mapLogic.fetchListStations(0, activeFilters);
                                }}
                            />
                        </View>

                        {/* Filter Button */}
                        <TouchableOpacity
                            className="w-12 h-12 bg-secondary rounded-full items-center justify-center shadow-lg"
                            activeOpacity={0.7}
                            onPress={() => setShowFilterSheet(true)}
                        >
                            <Ionicons name="options" size={24} color={hasActiveFilters ? "#035c1cff" : "#FFF"} />
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Map View Container */}
                <View style={{ flex: 1, display: mapLogic.viewMode === "map" ? "flex" : "none" }}>
                    <View className="flex-1 relative">
                        {/* Debug Info */}
                        {__DEV__ && (
                            <View className="absolute top-2 left-2 bg-black/70 px-3 py-2 rounded-lg z-50">
                                <Text className="text-white text-xs">Stations: {mapLogic.stations.length}</Text>
                            </View>
                        )}

                        {/* Map */}
                        <MapView
                            key={`map-${mapLogic.mapKey}`}
                            ref={mapLogic.mapRef}
                            style={{ flex: 1 }}
                            mapType="standard"
                            initialRegion={mapLogic.initialRegion}
                            onRegionChangeComplete={mapLogic.handleRegionChangeComplete}
                            showsUserLocation={true}
                        >
                            {/* Station Markers - Smart State */}
                            {markers}

                            {/* Navigation Polyline */}
                            {mapLogic.isNavigating && mapLogic.routeCoordinates.length > 0 && (
                                <Polyline
                                    key={`polyline-${mapLogic.routeCoordinates.length}`}
                                    coordinates={mapLogic.routeCoordinates}
                                    strokeColor="#4285F4"
                                    strokeWidth={6}
                                    lineCap="round"
                                    lineJoin="round"
                                    geodesic={true}
                                    zIndex={1000}
                                />
                            )}
                        </MapView>

                        {/* Loading Indicator */}
                        {mapLogic.isInitialLoading && (
                            <View className="absolute top-4 left-0 right-0 items-center">
                                <View className="bg-white/90 px-4 py-2 rounded-full">
                                    <ActivityIndicator color="#00A452" />
                                </View>
                            </View>
                        )}

                        {/* Map Controls */}
                        <View className={`absolute right-4 gap-3 ${mapLogic.isNavigating ? 'bottom-28' : 'bottom-6'}`}>
                            {/* Toggle List View */}
                            <TouchableOpacity
                                className="w-12 h-12 rounded-full bg-secondary items-center justify-center"
                                activeOpacity={0.7}
                                onPress={() => mapLogic.setViewMode("list")}
                            >
                                <Ionicons name="list" size={24} color="#FFF" />
                            </TouchableOpacity>

                            {/* Center to Location */}
                            <TouchableOpacity
                                className="w-12 h-12 rounded-full bg-secondary items-center justify-center"
                                activeOpacity={0.7}
                                onPress={mapLogic.centerToUserLocation}
                            >
                                <Ionicons name="locate" size={24} color="#FFF" />
                            </TouchableOpacity>
                        </View>

                        {/* Navigation Info - Shows distance, time, and cancel button */}
                        {mapLogic.isNavigating && (
                            <NavigationInfo
                                remainingDistance={mapLogic.remainingDistance * 1000}
                                remainingDuration={mapLogic.routeDuration}
                                onCancel={mapLogic.cancelNavigation}
                            />
                        )}
                    </View>
                </View>

                {/* List View Container */}
                <View style={{ flex: 1, display: mapLogic.viewMode === "list" ? "flex" : "none" }}>
                    <View className="flex-1 px-4">
                        <FlatList
                            data={mapLogic.listStations}
                            keyExtractor={(item) => item.id.toString()}
                            renderItem={({ item }) => (
                                <StationCard
                                    station={item}
                                    onPress={() => {
                                        // Cache station data first for instant navigation
                                        useStationCache.getState().setStation(item);
                                        router.push(`/station/${item.id}`);
                                    }}
                                />
                            )}
                            contentContainerStyle={{ paddingBottom: 100 }}
                            onEndReached={mapLogic.loadMoreListStations}
                            onEndReachedThreshold={0.5}
                            ListFooterComponent={
                                mapLogic.isListLoading ? (
                                    <View className="py-4 items-center">
                                        <ActivityIndicator color="#00A452" />
                                    </View>
                                ) : null
                            }
                            ListHeaderComponent={
                                <TouchableOpacity className="mb-4 py-2" onPress={() => {
                                    mapLogic.setViewMode("map");
                                    // Delay to ensure MapView is rendered before animating camera
                                    setTimeout(() => {
                                        mapLogic.centerToUserLocation();
                                    }, 100);
                                }}>
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
                visible={mapLogic.showPermissionModal}
                status={mapLogic.permissionStatus}
                onPrimaryAction={() => {
                    if (mapLogic.permissionStatus === "permission_required") {
                        mapLogic.requestLocationPermission();
                    }
                }}
                onEnterManually={mapLogic.handleEnterManually}
                onCancel={() => mapLogic.setShowPermissionModal(false)}
            />

            <StationQuickInfo
                visible={mapLogic.showQuickInfo}
                station={mapLogic.selectedStation}
                onClose={() => mapLogic.setShowQuickInfo(false)}
                onViewDetails={mapLogic.handleViewDetails}
                onBook={mapLogic.handleBook}
                onNavigate={mapLogic.handleNavigate}
            />

            <ManualLocationInput
                visible={mapLogic.showManualInput}
                onClose={() => mapLogic.setShowPermissionModal(false)}
                onLocationSet={mapLogic.handleManualLocationSet}
            />

            <FilterBottomSheet
                visible={showFilterSheet}
                onClose={() => setShowFilterSheet(false)}
                initialFilters={activeFilters}
                meta={mapLogic.filterMeta}
                onApply={(filters) => {
                    setActiveFilters(filters);
                    // Switch to List View
                    mapLogic.setViewMode("list");
                    // Refetch list when filters change
                    mapLogic.fetchListStations(0, filters);
                }}
            />
        </GradientBackground>
    );
}
