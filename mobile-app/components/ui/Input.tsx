import React, { useState } from "react";
import { View, Text, TextInput, TextInputProps, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";

type InputVariant = "rounded" | "underline" | "outline";
type InputState = "default" | "error" | "success";

interface InputProps extends TextInputProps {
    /** Label text above input */
    label?: string;
    /** Error message to display */
    error?: string;
    /** Success message to display */
    successMessage?: string;
    /** Visual variant of input */
    variant?: InputVariant;
    /** Left icon name (Ionicons) */
    leftIcon?: keyof typeof Ionicons.glyphMap;
    /** Right icon name (Ionicons) */
    rightIcon?: keyof typeof Ionicons.glyphMap;
    /** Handler for right icon press */
    onRightIconPress?: () => void;
    /** Custom wrapper className */
    wrapperClassName?: string;
    /** Custom input className */
    inputClassName?: string;
    /** Custom label className */
    labelClassName?: string;
}

export default function Input({
    label,
    error,
    successMessage,
    variant = "rounded",
    leftIcon,
    rightIcon,
    onRightIconPress,
    secureTextEntry,
    wrapperClassName = "",
    inputClassName = "",
    labelClassName = "",
    ...props
}: InputProps) {
    const [isFocused, setIsFocused] = useState(false);
    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    // Determine input state
    const inputState: InputState = error ? "error" : successMessage ? "success" : "default";

    // Variant wrapper styles
    const variantWrapperStyles = {
        rounded: "flex-row items-center bg-primary/90 px-4 py-[2px] rounded-full",
        underline: "border-b pb-3",
        outline: "flex-row items-center border-2 rounded-lg px-4 py-3",
    };

    // Border colors based on state and focus
    const getBorderColor = () => {
        if (error) return "border-error";
        if (successMessage) return "border-success";
        if (isFocused) return "border-accent";
        return "border-border";
    };

    const borderStyles = variant !== "rounded" ? getBorderColor() : "";

    // Input text styles
    const inputTextStyles = {
        rounded: "flex-1 text-white text-base",
        underline: "text-accent text-base",
        outline: "flex-1 text-white text-base",
    };

    const shouldShowPasswordToggle = secureTextEntry && !rightIcon;
    const effectiveRightIcon = shouldShowPasswordToggle
        ? (isPasswordVisible ? "eye-off" : "eye")
        : rightIcon;

    const handleRightIconPress = () => {
        if (shouldShowPasswordToggle) {
            setIsPasswordVisible(!isPasswordVisible);
        } else if (onRightIconPress) {
            onRightIconPress();
        }
    };

    return (
        <View className={wrapperClassName}>
            {/* Label */}
            {label && (
                <Text
                    className={[
                        "mb-2 text-text-secondary text-sm font-medium",
                        variant === "rounded" ? "ml-4" : "",
                        labelClassName,
                    ].join(" ")}
                >
                    {label}
                </Text>
            )}

            {/* Input Container */}
            <View
                className={[
                    variantWrapperStyles[variant],
                    borderStyles,
                    variant === "rounded" ? "" : "mb-1",
                ].join(" ")}
            >
                {/* Left Icon */}
                {leftIcon && (
                    <Ionicons
                        name={leftIcon}
                        size={20}
                        color="#9BA1A6"
                        style={{ marginRight: 8 }}
                    />
                )}

                {/* Text Input */}
                <TextInput
                    className={[
                        inputTextStyles[variant],
                        variant === "rounded" ? "ml-3" : "",
                        inputClassName,
                    ].join(" ")}
                    placeholderTextColor="#999"
                    secureTextEntry={secureTextEntry && !isPasswordVisible}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                    {...props}
                />

                {/* Right Icon */}
                {effectiveRightIcon && (
                    <TouchableOpacity
                        onPress={handleRightIconPress}
                        activeOpacity={0.7}
                        hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                    >
                        <Ionicons
                            name={effectiveRightIcon}
                            size={20}
                            color="#9BA1A6"
                        />
                    </TouchableOpacity>
                )}
            </View>

            {/* Error Message */}
            {error && (
                <Text className="mt-1 text-error text-sm ml-4">{error}</Text>
            )}

            {/* Success Message */}
            {successMessage && (
                <Text className="mt-1 text-success text-sm ml-4">
                    {successMessage}
                </Text>
            )}
        </View>
    );
}
