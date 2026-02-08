import React from "react";
import { View, Text } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Modal from "@/components/ui/Modal";
import Button from "@/components/ui/Button";

export interface LocationPermissionModalProps {
    /** Modal visibility */
    visible: boolean;
    /** Callback when enable location is pressed */
    onEnableLocation: () => void;
    /** Callback when cancel is pressed */
    onCancel: () => void;
}

export default function LocationPermissionModal({
    visible,
    onEnableLocation,
    onCancel,
}: LocationPermissionModalProps) {
    return (
        <Modal visible={visible} onClose={onCancel}>
            <View className="items-center py-6">
                {/* Icon with decorative elements */}
                <View className="relative mb-6">
                    <View className="absolute -top-4 -left-2 w-2 h-2 rounded-full bg-secondary" />
                    <View className="absolute -bottom-2 -right-4 w-2 h-2 rounded-full bg-secondary" />
                    <View className="absolute top-0 right-0 w-2 h-2 rounded-full bg-secondary/50" />
                    <View className="absolute bottom-6 left-6 w-2 h-2 rounded-full bg-secondary/50" />

                    <View className="w-32 h-32 rounded-full bg-secondary items-center justify-center">
                        <Ionicons name="location" size={64} color="#FFFFFF" />
                    </View>
                </View>

                {/* Title */}
                <Text className="text-2xl font-bold text-secondary mb-4">
                    Enable Location
                </Text>

                {/* Description */}
                <Text className="text-base text-center text-[#9BA1A6] mb-8 px-4">
                    We need access to your location to find EV charging stations
                    around you.
                </Text>

                {/* Enable Location Button */}
                <Button
                    variant="primary"
                    fullWidth
                    onPress={onEnableLocation}
                    className="mb-3"
                >
                    Enable Location
                </Button>

                {/* Cancel Button */}
                <Button
                    variant="ghost"
                    fullWidth
                    onPress={onCancel}
                    textClassName="text-secondary"
                >
                    Cancel
                </Button>
            </View>
        </Modal>
    );
}
