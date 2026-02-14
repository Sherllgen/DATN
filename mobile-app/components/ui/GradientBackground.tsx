import React from "react";
import { LinearGradient, LinearGradientProps } from "expo-linear-gradient";
import { ColorValue } from "react-native";
import { cssInterop } from "nativewind";
import { GRADIENTS, GradientKey, DEFAULT_GRADIENT } from "@/constants/gradients";

cssInterop(LinearGradient, {
    className: "style",
});

interface GradientBackgroundProps extends Omit<LinearGradientProps, "colors" | "start" | "end"> {
    /** Use a preset gradient by name, or provide custom colors */
    preset?: GradientKey;
    /** Override/custom gradient colors (must have at least 2 colors) */
    colors?: readonly [ColorValue, ColorValue, ...ColorValue[]];
    /** Custom className */
    className?: string;
    /** Children components */
    children: React.ReactNode;
}

/**
 * Reusable gradient background component with app's design token gradients.
 * Uses the main gradient (#33404F → #000000) by default.
 * 
 * @example
 * // Use default main gradient
 * <GradientBackground className="flex-1 px-6 pt-6">
 *   <Text>Content</Text>
 * </GradientBackground>
 * 
 * @example
 * // Use a preset gradient
 * <GradientBackground preset="primaryDark" className="flex-1">
 *   <Text>Content</Text>
 * </GradientBackground>
 * 
 * @example
 * // Custom gradient colors
 * <GradientBackground colors={["#FF0000", "#0000FF"]} className="flex-1">
 *   <Text>Content</Text>
 * </GradientBackground>
 */
export default function GradientBackground({
    preset,
    colors,
    className = "",
    children,
    ...props
}: GradientBackgroundProps) {
    // Use preset if provided, otherwise use custom colors or default
    const gradientConfig = preset ? GRADIENTS[preset] : DEFAULT_GRADIENT;
    const finalColors = colors || gradientConfig.colors;

    return (
        <LinearGradient
            colors={finalColors as any}
            start={gradientConfig.start}
            end={gradientConfig.end}
            className={className}
            {...props}
        >
            {children}
        </LinearGradient>
    );
}
