import React, { useMemo } from "react";
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
import { useMapLogic } from "@/hooks/useMapLogic";

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
                                className="flex-1 ml-3 text-base text-[#11181C]"
                                placeholder="Search station"
                                placeholderTextColor="#9BA1A6"
                                value={mapLogic.searchQuery}
                                onChangeText={mapLogic.setSearchQuery}
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
                        <View className="absolute bottom-6 right-4 gap-3">
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

                        {/* Cancel Navigation Button */}
                        {mapLogic.isNavigating && (
                            <View className="absolute bottom-8 left-4 right-4 items-center">
                                <TouchableOpacity
                                    className="bg-red-500 px-6 py-3 rounded-full flex-row items-center shadow-lg"
                                    onPress={mapLogic.cancelNavigation}
                                    activeOpacity={0.8}
                                >
                                    <Ionicons name="close-circle" size={24} color="white" />
                                    <Text className="text-white font-bold ml-2">Exit Navigation</Text>
                                </TouchableOpacity>
                            </View>
                        )}
                    </View>
                </View>

                {/* List View Container */}
                <View style={{ flex: 1, display: mapLogic.viewMode === "list" ? "flex" : "none" }}>
                    <View className="flex-1 px-4">
                        <FlatList
                            data={mapLogic.stations}
                            keyExtractor={(item) => item.id.toString()}
                            renderItem={({ item }) => (
                                <StationCard station={item} onPress={() => router.push(`/station/${item.id}`)} />
                            )}
                            contentContainerStyle={{ paddingBottom: 100 }}
                            ListHeaderComponent={
                                <TouchableOpacity className="mb-4 py-2" onPress={() => mapLogic.setViewMode("map")}>
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
        </GradientBackground>
    );
}
