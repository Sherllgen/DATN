import React from "react";
import { View, Text, ViewProps } from "react-native";

type BadgeVariant = "primary" | "success" | "error" | "warning" | "info";
type BadgeSize = "sm" | "md" | "lg";

interface BadgeProps extends ViewProps {
    /** Badge content (number or text) - not needed in dot mode */
    children?: React.ReactNode;
    /** Visual variant */
    variant?: BadgeVariant;
    /** Size of badge */
    size?: BadgeSize;
    /** Show badge as dot (no content) */
    dot?: boolean;
    /** Custom className */
    className?: string;
}

export default function Badge({
    children,
    variant = "primary",
    size = "md",
    dot = false,
    className = "",
    ...props
}: BadgeProps) {
    // Variant styles
    const variantStyles = {
        primary: "bg-primary",
        success: "bg-success",
        error: "bg-error",
        warning: "bg-warning",
        info: "bg-info",
    };

    // Size styles
    const sizeStyles = {
        sm: dot ? "w-2 h-2" : "px-1.5 py-0.5 min-w-4 h-4",
        md: dot ? "w-2.5 h-2.5" : "px-2 py-1 min-w-5 h-5",
        lg: dot ? "w-3 h-3" : "px-2.5 py-1 min-w-6 h-6",
    };

    const textSizeStyles = {
        sm: "text-xs",
        md: "text-xs",
        lg: "text-sm",
    };

    const baseStyles = "rounded-full items-center justify-center";

    const combinedClassName = [
        baseStyles,
        variantStyles[variant],
        sizeStyles[size],
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <View className={combinedClassName} {...props}>
            {!dot && (
                <Text
                    className={[
                        "text-white font-semibold",
                        textSizeStyles[size],
                    ].join(" ")}
                >
                    {children}
                </Text>
            )}
        </View>
    );
}
