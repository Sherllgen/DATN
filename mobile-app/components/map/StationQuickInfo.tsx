import React from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { Ionicons, MaterialIcons } from "@expo/vector-icons";
import Modal from "@/components/ui/Modal";
import Button from "@/components/ui/Button";
import StatusBadge, { StatusBadgeVariant } from "@/components/station/StatusBadge";
import { StationSearchResult, StationStatus } from "@/types/station.types";

export interface StationQuickInfoProps {
    /** Modal visibility */
    visible: boolean;
    /** Station data */
    station: StationSearchResult | null;
    /** Callback when modal is closed */
    onClose: () => void;
    /** Callback when View button is pressed */
    onViewDetails: () => void;
    /** Callback when Book button is pressed */
    onBook: () => void;
    /** Callback when Navigate button is pressed */
    onNavigate: () => void;
}

export default function StationQuickInfo({
    visible,
    station,
    onClose,
    onViewDetails,
    onBook,
    onNavigate,
}: StationQuickInfoProps) {
    if (!station) return null;

    // Determine status variant based on backend status
    const statusVariant: StatusBadgeVariant =
        station.status === StationStatus.ACTIVE ? "available" : "occupied";

    return (
        <Modal visible={visible} onClose={onClose}>
            <View className="pb-6">
                {/* Station Name and Navigation */}
                <View className="flex-row items-center justify-between mb-4">
                    <View className="flex-1">
                        <Text className="text-lg font-semibold text-white mb-1">
                            {station.name}
                        </Text>
                        <Text className="text-sm text-[#9BA1A6]">
                            {station.address}
                        </Text>
                    </View>
                    <TouchableOpacity
                        className="w-10 h-10 rounded-full bg-secondary items-center justify-center ml-2"
                        activeOpacity={0.7}
                        onPress={onNavigate}
                    >
                        <Ionicons
                            name="navigate"
                            size={20}
                            color="#FFFFFF"
                        />
                    </TouchableOpacity>
                </View>

                {/* Rating and Reviews */}
                <View className="flex-row items-center mb-4">
                    <Text className="text-base font-semibold text-white mr-2">
                        {station.rate?.toFixed(1) ?? "N/A"}
                    </Text>
                    <View className="flex-row mr-2">
                        {[1, 2, 3, 4, 5].map((star) => (
                            <Ionicons
                                key={star}
                                name={station.rate && star <= station.rate ? "star" : "star-outline"}
                                size={14}
                                color="#F59E0B"
                            />
                        ))}
                    </View>
                    <Text className="text-sm text-[#9BA1A6]">
                        (128 reviews)
                    </Text>
                </View>

                {/* Status and Stats */}
                <View className="flex-row items-center mb-4">
                    <StatusBadge variant={statusVariant} />

                    <View className="flex-row items-center ml-4">
                        <MaterialIcons name="location-on" size={16} color="#9BA1A6" />
                        <Text className="text-sm text-[#9BA1A6] ml-1">
                            {station.distanceKm?.toFixed(1) ?? "--"} km
                        </Text>
                    </View>

                    {/* <View className="flex-row items-center ml-4">
                        <Ionicons name="time-outline" size={16} color="#9BA1A6" />
                        <Text className="text-sm text-[#9BA1A6] ml-1">
                            5 mins
                        </Text>
                    </View> */}
                </View>

                {/* Charger Count */}
                <View className="flex-row items-center mb-6">
                    <Text className="text-sm font-medium text-secondary">
                        {station.totalChargersCount} chargers
                    </Text>
                    <Text className="text-sm text-[#9BA1A6] ml-2">
                        ({station.availableChargersCount} available)
                    </Text>
                </View>

                {/* Action Buttons */}
                <View className="flex-row gap-3">
                    <Button
                        variant="outline"
                        fullWidth={false}
                        onPress={onViewDetails}
                        className="flex-1"
                    >
                        View
                    </Button>
                    <Button
                        variant="primary"
                        fullWidth={false}
                        onPress={onBook}
                        className="flex-1"
                    >
                        Book
                    </Button>
                </View>
            </View>
        </Modal>
    );
}
