import React from "react";
import { View, Image, Text, ViewProps } from "react-native";

type AvatarSize = "sm" | "md" | "lg" | "xl";

interface AvatarProps extends ViewProps {
    /** Image source URI */
    source?: string;
    /** Fallback initials (e.g., "JD" for John Doe) */
    initials?: string;
    /** Size of avatar */
    size?: AvatarSize;
    /** Custom className */
    className?: string;
}

export default function Avatar({
    source,
    initials,
    size = "md",
    className = "",
    ...props
}: AvatarProps) {
    // Size styles
    const sizeStyles = {
        sm: "w-8 h-8",
        md: "w-12 h-12",
        lg: "w-16 h-16",
        xl: "w-24 h-24",
    };

    const textSizeStyles = {
        sm: "text-xs",
        md: "text-base",
        lg: "text-xl",
        xl: "text-3xl",
    };

    const baseStyles = "rounded-full overflow-hidden items-center justify-center";
    const fallbackBg = "bg-primary";

    const combinedClassName = [
        baseStyles,
        sizeStyles[size],
        !source ? fallbackBg : "",
        className,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <View className={combinedClassName} {...props}>
            {source ? (
                <Image
                    source={{ uri: source }}
                    className="w-full h-full"
                    resizeMode="cover"
                />
            ) : (
                <Text
                    className={[
                        "font-semibold text-white",
                        textSizeStyles[size],
                    ].join(" ")}
                >
                    {initials || "?"}
                </Text>
            )}
        </View>
    );
}
