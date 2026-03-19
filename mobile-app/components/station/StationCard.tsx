import React from "react";
import { View, Text, TouchableOpacity, Image } from "react-native";
import { Ionicons, MaterialIcons, MaterialCommunityIcons } from "@expo/vector-icons";
import StatusBadge, {
    StatusBadgeVariant,
} from "@/components/station/StatusBadge";
import { StationSearchResult, StationStatus } from "@/types/station.types";

export interface StationCardProps {
    /** Station data */
    station: StationSearchResult;
    /** Callback when card is pressed */
    onPress?: () => void;
    /** Custom className */
    className?: string;
}

export default function StationCard({
    station,
    onPress,
    className = "",
}: StationCardProps) {
    // Determine status based on backend status value
    const statusVariant: StatusBadgeVariant =
        station.status === StationStatus.ACTIVE
            ? "available"
            : station.status === StationStatus.SUSPENDED
                ? "suspended"
                : "occupied";

    const iconBgColor =
        station.status === StationStatus.ACTIVE
            ? "bg-success"
            : station.status === StationStatus.SUSPENDED
                ? "bg-warning"
                : "bg-error";

    const imageUrl = station.imageUrls && station.imageUrls.length > 0
        ? station.imageUrls[0]
        : null;

    return (
        <TouchableOpacity
            className={[
                "flex-row items-center bg-white/5 rounded-2xl p-3 mb-3 border border-border-gray/20",
                className,
            ].join(" ")}
            onPress={onPress}
            activeOpacity={0.7}
        >
            {/* Marker Icon (Green/Red/Yellow) */}
            <View className="mr-3">
                <View
                    className={[
                        "w-10 h-10 rounded-full items-center justify-center",
                        iconBgColor,
                    ].join(" ")}
                >
                    <MaterialCommunityIcons
                        name="ev-station"
                        size={20}
                        color="#fff"
                    />
                </View>
            </View>

            {/* Station Info */}
            <View className="flex-1">
                <Text className="text-base font-semibold text-white mb-1">
                    {station.name}
                </Text>
                <Text
                    className="text-sm text-[#9BA1A6] mb-2"
                    numberOfLines={1}
                >
                    {station.address}
                </Text>

                {/* Distance and Availability */}
                <View className="flex-row items-center">
                    <StatusBadge
                        variant={statusVariant}
                        showDot={false}
                        className="mr-2"
                    />
                    <MaterialIcons
                        name="location-on"
                        size={14}
                        color="#9BA1A6"
                    />
                    <Text className="text-sm text-[#9BA1A6] ml-1">
                        {station.distanceKm?.toFixed(1) ?? "--"} km
                    </Text>
                </View>
            </View>

            {/* Chevron */}
            <Ionicons
                name="chevron-forward"
                size={20}
                color="#9BA1A6"
            />
        </TouchableOpacity>
    );
}
