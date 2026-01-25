import React from "react";
import {
    Text,
    TouchableOpacity,
    TouchableOpacityProps,
    ActivityIndicator,
} from "react-native";

type ButtonVariant = "primary" | "secondary" | "outline" | "ghost" | "danger";
type ButtonSize = "sm" | "md" | "lg";

interface ButtonProps extends TouchableOpacityProps {
    /** Button text label */
    children: React.ReactNode;
    /** Visual variant of the button */
    variant?: ButtonVariant;
    /** Size of the button */
    size?: ButtonSize;
    /** Full width button */
    fullWidth?: boolean;
    /** Loading state */
    loading?: boolean;
    /** Custom className for wrapper */
    className?: string;
    /** Custom className for text */
    textClassName?: string;
}

export default function Button({
    children,
    variant = "primary",
    size = "md",
    fullWidth = false,
    loading = false,
    disabled,
    className = "",
    textClassName = "",
    ...props
}: ButtonProps) {
    // Variant styles
    const variantStyles = {
        primary: "bg-secondary",
        secondary: "bg-accent",
        outline: "bg-transparent border-2 border-accent",
        ghost: "bg-transparent",
        danger: "bg-error",
    };

    // Size styles
    const sizeStyles = {
        sm: "py-2 px-4",
        md: "py-3 px-6",
        lg: "py-4 px-8",
    };

    // Text size styles
    const textSizeStyles = {
        sm: "text-sm",
        md: "text-base",
        lg: "text-lg",
    };

    // Disabled/loading styles
    const disabledStyles =
        disabled || loading ? "opacity-50" : "opacity-100";

    // Text color based on variant
    const textColorStyles = {
        primary: "text-white",
        secondary: "text-white",
        outline: "text-accent",
        ghost: "text-accent",
        danger: "text-white",
    };

    const baseStyles = "rounded-full items-center justify-center";
    const widthStyles = fullWidth ? "w-full" : "";

    const combinedClassName = [
        baseStyles,
        variantStyles[variant],
        sizeStyles[size],
        widthStyles,
        disabledStyles,
        className,
    ]
        .filter(Boolean)
        .join(" ");

    const combinedTextClassName = [
        "font-semibold",
        textColorStyles[variant],
        textSizeStyles[size],
        textClassName,
    ]
        .filter(Boolean)
        .join(" ");

    return (
        <TouchableOpacity
            className={combinedClassName}
            disabled={disabled || loading}
            activeOpacity={0.7}
            {...props}
        >
            {loading ? (
                <ActivityIndicator
                    color={variant === "outline" ? "#4A5568" : "#fff"}
                />
            ) : (
                <Text className={combinedTextClassName}>{children}</Text>
            )}
        </TouchableOpacity>
    );
}
