import React from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Button from "@/components/ui/Button";

/**
 * NavigationInfo - Molecule Component
 * 
 * Displays navigation metrics during active navigation:
 * - Remaining distance
 * - Estimated time remaining
 * - Cancel navigation button
 */

export interface NavigationInfoProps {
    /** Remaining distance to destination in meters */
    remainingDistance: number;

    /** Estimated time remaining in seconds */
    remainingDuration: number;

    /** Callback when cancel button is pressed */
    onCancel: () => void;

    /** Optional custom className for container */
    containerClassName?: string;
}

export default function NavigationInfo({
    remainingDistance,
    remainingDuration,
    onCancel,
    containerClassName = "",
}: NavigationInfoProps) {
    // Format distance: meters → km with 1 decimal
    const formatDistance = (meters: number): string => {
        if (meters < 1000) {
            return `${Math.round(meters)} m`;
        }
        return `${(meters / 1000).toFixed(1)} km`;
    };

    // Format duration: seconds → minutes
    const formatDuration = (seconds: number): string => {
        const minutes = Math.round(seconds / 60);
        if (minutes < 1) return "< 1 min";
        return `${minutes} min`;
    };

    return (
        <View className={`absolute bottom-8 left-4 right-4 ${containerClassName}`}>
            <View className="bg-surface-dark rounded-2xl px-6 py-4 flex-row items-center justify-between shadow-xl">
                {/* Navigation Metrics */}
                <View className="flex-row items-center gap-4">
                    {/* Distance */}
                    <View className="flex-row items-center gap-2">
                        <Ionicons name="navigate" size={20} color="#4285F4" />
                        <Text className="text-white text-lg font-bold">
                            {formatDistance(remainingDistance)}
                        </Text>
                    </View>

                    {/* Separator */}
                    <View className="w-1 h-1 rounded-full bg-gray-400" />

                    {/* Duration */}
                    <View className="flex-row items-center gap-2">
                        <Ionicons name="time-outline" size={20} color="#4285F4" />
                        <Text className="text-white text-lg font-bold">
                            {formatDuration(remainingDuration)}
                        </Text>
                    </View>
                </View>

                {/* Cancel Button */}
                <Button
                    variant="danger"
                    size="sm"
                    onPress={onCancel}
                    textWrapper={false}
                    className="flex-row items-center gap-1"
                >
                    <Ionicons name="close-circle" size={18} color="white" />
                    <Text className="text-white font-semibold text-sm">Exit Navigation</Text>
                </Button>
            </View>
        </View>
    );
}
