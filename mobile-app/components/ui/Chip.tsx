import React from "react";
import { View, Text, TouchableOpacity, TouchableOpacityProps } from "react-native";
import { Ionicons } from "@expo/vector-icons";

interface ChipProps extends TouchableOpacityProps {
    /** Chip label */
    label: string;
    /** Whether chip is selected */
    selected?: boolean;
    /** Handler for chip press */
    onPress?: () => void;
    /** Left icon name */
    icon?: keyof typeof Ionicons.glyphMap;
    /** Show close/remove icon */
    onRemove?: () => void;
    /** Custom className */
    className?: string;
}

export default function Chip({
    label,
    selected = false,
    onPress,
    icon,
    onRemove,
    className = "",
    disabled,
    ...props
}: ChipProps) {
    const baseStyles = "px-4 py-2 rounded-lg border flex-row items-center";
    const selectedStyles = selected
        ? "bg-accent border-accent"
        : "bg-transparent border-border";

    const textColor = selected ? "text-white" : "text-text-secondary";

    const combinedClassName = [baseStyles, selectedStyles, className]
        .filter(Boolean)
        .join(" ");

    return (
        <TouchableOpacity
            className={combinedClassName}
            onPress={onPress}
            activeOpacity={0.7}
            disabled={disabled || (!onPress && !onRemove)}
            {...props}
        >
            {/* Left Icon */}
            {icon && (
                <Ionicons
                    name={icon}
                    size={16}
                    color={selected ? "#fff" : "#9BA1A6"}
                    style={{ marginRight: 6 }}
                />
            )}

            {/* Label */}
            <Text className={textColor}>{label}</Text>

            {/* Remove Icon */}
            {onRemove && (
                <TouchableOpacity
                    onPress={onRemove}
                    hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                    style={{ marginLeft: 6 }}
                >
                    <Ionicons
                        name="close-circle"
                        size={16}
                        color={selected ? "#fff" : "#9BA1A6"}
                    />
                </TouchableOpacity>
            )}
        </TouchableOpacity>
    );
}
