import React from "react";
import {
    Modal as RNModal,
    View,
    Text,
    TouchableOpacity,
    ModalProps as RNModalProps,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";

type ModalVariant = "bottom-sheet" | "centered" | "full-screen";

interface ModalProps extends Omit<RNModalProps, "transparent" | "animationType"> {
    /** Whether modal is visible */
    visible: boolean;
    /** Handler when modal should close */
    onClose: () => void;
    /** Modal variant style */
    variant?: ModalVariant;
    /** Modal title */
    title?: string;
    /** Children content */
    children: React.ReactNode;
    /** Show close button */
    showCloseButton?: boolean;
    /** Custom container className */
    containerClassName?: string;
    /** Custom content className */
    contentClassName?: string;
}

export default function Modal({
    visible,
    onClose,
    variant = "bottom-sheet",
    title,
    children,
    showCloseButton = true,
    containerClassName = "",
    contentClassName = "",
    ...props
}: ModalProps) {
    // Variant styles
    const containerVariantStyles = {
        "bottom-sheet": "flex-1 justify-end",
        centered: "flex-1 justify-center items-center px-6",
        "full-screen": "flex-1",
    };

    const contentVariantStyles = {
        "bottom-sheet": "bg-surface pb-8 rounded-t-3xl",
        centered: "bg-surface rounded-xl w-full max-w-md",
        "full-screen": "bg-surface flex-1",
    };

    const animationType = variant === "full-screen" ? "slide" : "fade";

    return (
        <RNModal
            visible={visible}
            transparent={variant !== "full-screen"}
            animationType={animationType}
            onRequestClose={onClose}
            {...props}
        >
            <TouchableOpacity
                className={[
                    containerVariantStyles[variant],
                    variant !== "full-screen" ? "bg-black/60" : "",
                    containerClassName,
                ].join(" ")}
                activeOpacity={1}
                onPress={variant !== "full-screen" ? onClose : undefined}
            >
                <TouchableOpacity
                    activeOpacity={1}
                    onPress={(e) => e.stopPropagation()}
                >
                    <View
                        className={[
                            contentVariantStyles[variant],
                            contentClassName,
                        ].join(" ")}
                    >
                        {/* Header */}
                        {(title || showCloseButton) && (
                            <View
                                className={[
                                    "flex-row justify-between items-center px-6 py-4",
                                    variant === "bottom-sheet"
                                        ? "border-b border-border"
                                        : "",
                                ].join(" ")}
                            >
                                {/* Swipe Indicator for Bottom Sheet */}
                                {variant === "bottom-sheet" && !title && (
                                    <View className="absolute top-2 left-1/2 -translate-x-1/2">
                                        <View className="bg-border rounded-full w-12 h-1" />
                                    </View>
                                )}

                                {/* Title */}
                                {title && (
                                    <Text className="font-semibold text-white text-lg flex-1">
                                        {title}
                                    </Text>
                                )}

                                {/* Close Button */}
                                {showCloseButton && (
                                    <TouchableOpacity
                                        onPress={onClose}
                                        activeOpacity={0.7}
                                        hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                                    >
                                        <Ionicons
                                            name="close"
                                            size={28}
                                            color="#9BA1A6"
                                        />
                                    </TouchableOpacity>
                                )}
                            </View>
                        )}

                        {/* Content */}
                        <View className={variant === "bottom-sheet" ? "px-6 pt-4" : "p-6"}>
                            {children}
                        </View>
                    </View>
                </TouchableOpacity>
            </TouchableOpacity>
        </RNModal>
    );
}
