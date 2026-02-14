import React from "react";
import { View, Text } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Modal from "@/components/ui/Modal";
import Button from "@/components/ui/Button";

export type LocationModalStatus = "idle" | "permission_required" | "gps_error";

export interface LocationPermissionModalProps {
    /** Modal visibility */
    visible: boolean;
    /** Current status of the location request */
    status: LocationModalStatus;
    /** Callback when enable location/retry is pressed */
    onPrimaryAction: () => void;
    /** Callback when enter manually is pressed */
    onEnterManually: () => void;
    /** Callback when cancel is pressed */
    onCancel: () => void;
}

export default function LocationPermissionModal({
    visible,
    status,
    onPrimaryAction,
    onEnterManually,
    onCancel,
}: LocationPermissionModalProps) {
    const isError = status === "gps_error";

    // Content based on status
    const title = isError ? "Location Unavailable" : "Enable Location";
    const description = isError
        ? "We couldn't get your current location. Please check your GPS settings or enter your location manually."
        : "We need access to your location to find EV charging stations around you.";
    const primaryButtonText = isError ? "Retry" : "Enable Location";

    return (
        <Modal visible={visible} onClose={onCancel}>
            <View className="items-center py-6">
                {/* Icon with decorative elements */}
                <View className="relative mb-6">
                    <View className={`absolute -top-4 -left-2 w-2 h-2 rounded-full ${isError ? 'bg-red-500' : 'bg-secondary'}`} />
                    <View className={`absolute -bottom-2 -right-4 w-2 h-2 rounded-full ${isError ? 'bg-red-500' : 'bg-secondary'}`} />
                    <View className={`absolute top-0 right-0 w-2 h-2 rounded-full ${isError ? 'bg-red-500/50' : 'bg-secondary/50'}`} />
                    <View className={`absolute bottom-6 left-6 w-2 h-2 rounded-full ${isError ? 'bg-red-500/50' : 'bg-secondary/50'}`} />

                    <View className={`w-32 h-32 rounded-full ${isError ? 'bg-red-500' : 'bg-secondary'} items-center justify-center`}>
                        <Ionicons name={isError ? "alert" : "location"} size={64} color="#FFFFFF" />
                    </View>
                </View>

                {/* Title */}
                <Text className={`text-2xl font-bold ${isError ? 'text-red-500' : 'text-secondary'} mb-4`}>
                    {title}
                </Text>

                {/* Description */}
                <Text className="text-base text-center text-[#9BA1A6] mb-8 px-4">
                    {description}
                </Text>

                {/* Primary Button (Enable or Retry) */}
                <Button
                    variant={isError ? "outline" : "primary"}
                    fullWidth
                    onPress={onPrimaryAction}
                    className={`mb-3 ${isError ? "border-red-500" : ""}`}
                    textClassName={isError ? "text-red-500" : ""}
                >
                    {primaryButtonText}
                </Button>

                {/* Enter Manually Button */}
                <Button
                    variant="outline"
                    fullWidth
                    onPress={onEnterManually}
                    className="mb-3"
                >
                    Enter Manually
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
