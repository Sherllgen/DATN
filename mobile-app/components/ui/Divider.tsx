import React from "react";
import { View, ViewProps } from "react-native";

interface DividerProps extends ViewProps {
    /** Orientation of divider */
    orientation?: "horizontal" | "vertical";
    /** Custom className */
    className?: string;
}

export default function Divider({
    orientation = "horizontal",
    className = "",
    ...props
}: DividerProps) {
    const orientationStyles = {
        horizontal: "h-px w-full bg-border-light",
        vertical: "w-px h-full bg-border-light",
    };

    const combinedClassName = [orientationStyles[orientation], className]
        .filter(Boolean)
        .join(" ");

    return <View className={combinedClassName} {...props} />;
}
