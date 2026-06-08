import React from "react";
import {
    Modal as RNModal,
    View,
    Text,
    TouchableOpacity,
    KeyboardAvoidingView,
    Platform,
    ModalProps as RNModalProps,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useSafeAreaInsets } from "react-native-safe-area-context";

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
    const insets = useSafeAreaInsets();
    
    // On iOS, use safe area insets.bottom to avoid the home indicator.
    // On Android, use a static padding (16px) for optimal bottom sheet layout.
    const safeBottom = Platform.OS === "ios"
        ? (insets.bottom > 0 ? insets.bottom + 8 : 16)
        : 16;

    // Variant styles
    const containerVariantStyles = {
        "bottom-sheet": "flex-1 justify-end",
        centered: "flex-1 justify-center items-center px-6",
        "full-screen": "flex-1",
    };

    const contentVariantStyles = {
        "bottom-sheet": "bg-surface rounded-t-3xl", // pb handled dynamically via style
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
            statusBarTranslucent
            {...props}
        >
            <KeyboardAvoidingView
                behavior={Platform.OS === "ios" ? "padding" : "padding"}
                style={{ flex: 1 }}
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
                        style={variant === "bottom-sheet" ? { paddingBottom: safeBottom } : undefined}
                    >
                        {/* Header */}
                        {/* Header Container (No Divider) */}
                        <View className="relative">
                            {/* Swipe Indicator (Centered) */}
                            {variant === "bottom-sheet" && (
                                <View className="w-full items-center py-3">
                                    <View className="bg-[#4A5568] rounded-full w-12 h-1" />
                                </View>
                            )}

                            {/* Title (Centered) */}
                            {title && (
                                <View className="px-6 pb-4 pt-1">
                                    <Text className="font-semibold text-white text-lg text-center">
                                        {title}
                                    </Text>
                                </View>
                            )}

                            {/* Close Button - Only for non-bottom-sheet variants if requested */}
                            {showCloseButton && variant !== "bottom-sheet" && (
                                <TouchableOpacity
                                    onPress={onClose}
                                    activeOpacity={0.7}
                                    hitSlop={{ top: 10, bottom: 10, left: 10, right: 10 }}
                                    className="absolute top-3 right-4 z-50"
                                >
                                    <Ionicons
                                        name="close"
                                        size={24}
                                        color="#9BA1A6"
                                    />
                                </TouchableOpacity>
                            )}
                        </View>

                        {/* Content */}
                        <View className={variant === "bottom-sheet" ? "px-6 pt-4" : "p-6"}>
                            {children}
                        </View>
                    </View>
                    </TouchableOpacity>
                </TouchableOpacity>
            </KeyboardAvoidingView>
        </RNModal>
    );
}
