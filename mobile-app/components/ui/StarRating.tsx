import React from "react";
import { View, TouchableOpacity, ViewStyle } from "react-native";
import { Ionicons } from "@expo/vector-icons";

interface StarRatingProps {
    /** Current rating value */
    rating: number;
    /** Maximum possible rating */
    maxRating?: number;
    /** Callback when a star is pressed */
    onRatingChange?: (rating: number) => void;
    /** Size of each star icon */
    size?: number;
    /** Color of filled stars */
    activeColor?: string;
    /** Color of empty stars */
    inactiveColor?: string;
    /** Custom style for the container */
    style?: ViewStyle;
    /** Whether the rating is read-only */
    readonly?: boolean;
}

export default function StarRating({
    rating,
    maxRating = 5,
    onRatingChange,
    size = 32,
    activeColor = "#F59E0B",
    inactiveColor = "#4A5568",
    style,
    readonly = false,
}: StarRatingProps) {
    const stars = Array.from({ length: maxRating }, (_, i) => i + 1);

    return (
        <View className="flex-row items-center justify-between" style={style}>
            {stars.map((star) => (
                <TouchableOpacity
                    key={star}
                    activeOpacity={readonly ? 1 : 0.7}
                    onPress={() => !readonly && onRatingChange?.(star)}
                    className="p-1"
                >
                    <Ionicons
                        name={star <= rating ? "star" : "star-outline"}
                        size={size}
                        color={star <= rating ? activeColor : inactiveColor}
                    />
                </TouchableOpacity>
            ))}
        </View>
    );
}
