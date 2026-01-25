import React from "react";
import { View, ViewProps } from "react-native";

type CardVariant = "default" | "elevated" | "outlined";

interface CardProps extends ViewProps {
    /** Visual variant of the card */
    variant?: CardVariant;
    /** Children components */
    children: React.ReactNode;
    /** Custom className */
    className?: string;
}

export default function Card({
    variant = "default",
    children,
    className = "",
    ...props
}: CardProps) {
    // Variant styles based on Design System
    const variantStyles = {
        default: "bg-surface-light border border-border",
        elevated: "bg-surface",
        outlined: "bg-transparent border-2 border-border",
    };

    const baseStyles = "p-4 rounded-lg";

    const combinedClassName = [
        baseStyles,
        variantStyles[variant],
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <View className={combinedClassName} {...props}>
            {children}
        </View>
    );
}
