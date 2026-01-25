import React from "react";
import { ActivityIndicator, View, ViewProps } from "react-native";

type SpinnerSize = "small" | "large";

interface LoadingSpinnerProps extends ViewProps {
    /** Size of the spinner */
    size?: SpinnerSize;
    /** Color of the spinner */
    color?: string;
    /** Center the spinner in container */
    centered?: boolean;
    /** Custom className */
    className?: string;
}

export default function LoadingSpinner({
    size = "large",
    color = "#4CAF50",
    centered = true,
    className = "",
    ...props
}: LoadingSpinnerProps) {
    const containerClassName = [
        centered ? "flex-1 justify-center items-center" : "",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    if (containerClassName) {
        return (
            <View className={containerClassName} {...props}>
                <ActivityIndicator size={size} color={color} />
            </View>
        );
    }

    return <ActivityIndicator size={size} color={color} {...props} />;
}
