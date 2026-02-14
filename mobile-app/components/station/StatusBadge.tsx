import React from "react";
import { View, Text } from "react-native";

export type StatusBadgeVariant = "available" | "occupied";

export interface StatusBadgeProps {
    /** Status variant */
    variant: StatusBadgeVariant;
    /** Show dot indicator */
    showDot?: boolean;
    /** Custom className */
    className?: string;
}

export default function StatusBadge({
    variant,
    showDot = true,
    className = "",
}: StatusBadgeProps) {
    const variantStyles = {
        available: {
            container: "bg-success/20",
            text: "text-success",
            dot: "bg-success",
        },
        occupied: {
            container: "bg-error/20",
            text: "text-error",
            dot: "bg-error",
        },
    };

    const labels = {
        available: "Available",
        occupied: "Occupied",
    };

    const styles = variantStyles[variant];

    return (
        <View
            className={[
                "flex-row items-center px-3 py-1 rounded-full",
                styles.container,
                className,
            ].join(" ")}
        >
            {showDot && (
                <View
                    className={[
                        "w-2 h-2 rounded-full mr-2",
                        styles.dot,
                    ].join(" ")}
                />
            )}
            <Text className={["text-xs font-medium", styles.text].join(" ")}>
                {labels[variant]}
            </Text>
        </View>
    );
}
