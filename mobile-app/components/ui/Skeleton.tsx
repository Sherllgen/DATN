import React, { useEffect, useRef } from "react";
import { Animated, ViewStyle, DimensionValue } from "react-native";

interface SkeletonProps {
    /** Width of the skeleton */
    width?: DimensionValue;
    /** Height of the skeleton */
    height?: DimensionValue;
    /** Border radius of the skeleton */
    borderRadius?: number;
    /** Variant of the skeleton */
    variant?: "box" | "circle" | "text";
    /** Custom container style extension */
    className?: string;
}

/**
 * A reusable Skeleton component for loading states.
 * Follows EV-Go design system and supports pulsing animation.
 */
export default function Skeleton({
    width,
    height,
    borderRadius = 8,
    variant = "box",
    className = "",
}: SkeletonProps) {
    const opacity = useRef(new Animated.Value(0.3)).current;

    useEffect(() => {
        const pulse = Animated.sequence([
            Animated.timing(opacity, {
                toValue: 0.6,
                duration: 1000,
                useNativeDriver: true,
            }),
            Animated.timing(opacity, {
                toValue: 0.3,
                duration: 1000,
                useNativeDriver: true,
            }),
        ]);

        Animated.loop(pulse).start();
    }, [opacity]);

    const getStyle = (): ViewStyle => {
        const style: ViewStyle = { opacity };

        if (variant === "circle") {
            const size = width || 40;
            style.width = size;
            style.height = size;
            if (typeof size === "number") {
                style.borderRadius = size / 2;
            } else {
                style.borderRadius = 999;
            }
        } else if (variant === "text") {
            style.width = width || "100%";
            style.height = height || 16;
            style.borderRadius = 4;
        } else {
            style.width = width || "100%";
            style.height = height || 100;
            style.borderRadius = borderRadius;
        }

        return style;
    };

    return (
        <Animated.View
            style={getStyle()}
            className={`bg-surface-light ${className}`}
        />
    );
}
