import { Ionicons } from "@expo/vector-icons";
import React from "react";
import { Text, TouchableOpacity, View } from "react-native";
import { AppColors } from "@/constants/theme";

export interface TimeSlot {
    label: string;      // E.g., "08:30"
    value: string;      // E.g., "08:30:00"
    availablePorts: number;
    isPast: boolean;
    isAvailable: boolean;
}

export interface TimeSlotGridProps {
    slots: TimeSlot[];
    selectedSlotValue?: string;
    onSelect: (value: string) => void;
    showIcons?: boolean;
    containerClassName?: string;
}

export default function TimeSlotGrid({
    slots,
    selectedSlotValue,
    onSelect,
    showIcons = true,
    containerClassName = "",
}: TimeSlotGridProps) {
    // Categorize slots by time of day
    const morningSlots = slots.filter((s) => {
        const hour = parseInt(s.value.split(":")[0]);
        return hour < 12;
    });

    const afternoonSlots = slots.filter((s) => {
        const hour = parseInt(s.value.split(":")[0]);
        return hour >= 12 && hour < 18;
    });

    const eveningSlots = slots.filter((s) => {
        const hour = parseInt(s.value.split(":")[0]);
        return hour >= 18;
    });

    const renderSection = (
        title: string,
        iconName: React.ComponentProps<typeof Ionicons>["name"],
        sectionSlots: TimeSlot[]
    ) => {
        if (sectionSlots.length === 0) return null;

        return (
            <View className="mb-6" key={title}>
                {/* Header */}
                <View className="flex-row items-center mb-3">
                    {showIcons && (
                        <Ionicons
                            name={iconName}
                            size={20}
                            color={AppColors.secondary}
                            className="mr-2"
                        />
                    )}
                    <Text className="text-white font-semibold text-base">
                        {title}
                    </Text>
                </View>

                {/* Grid */}
                <View className="flex-row flex-wrap justify-between gap-y-3">
                    {sectionSlots.map((slot) => {
                        const isSelected = selectedSlotValue === slot.value;
                        const isDisabled = !slot.isAvailable;

                        return (
                            <TouchableOpacity
                                key={slot.value}
                                disabled={isDisabled}
                                activeOpacity={0.7}
                                onPress={() => onSelect(slot.value)}
                                style={{ width: "31.3%" }}
                                className={[
                                    "py-3 px-2 rounded-xl items-center justify-center border",
                                    isSelected
                                        ? "bg-secondary border-secondary"
                                        : isDisabled
                                        ? "bg-[#1E293B]/20 border-transparent opacity-30"
                                        : "bg-surface-light border-border-gray",
                                ].join(" ")}
                            >
                                <Text
                                    className={[
                                        "text-sm font-semibold text-center",
                                        isSelected
                                            ? "text-white"
                                            : isDisabled
                                            ? "text-text-secondary line-through"
                                            : "text-white",
                                    ].join(" ")}
                                >
                                    {slot.label}
                                </Text>
                            </TouchableOpacity>
                        );
                    })}

                    {/* Empty elements to keep layout correct if the last row is not full */}
                    {sectionSlots.length % 3 !== 0 &&
                        Array.from({
                            length: 3 - (sectionSlots.length % 3),
                        }).map((_, i) => (
                            <View
                                key={`empty-${i}`}
                                style={{ width: "31.3%" }}
                                className="h-0 bg-transparent border-transparent"
                            />
                        ))}
                </View>
            </View>
        );
    };

    return (
        <View className={["mt-4", containerClassName].join(" ").trim()}>
            {renderSection("Morning (00:00 - 12:00)", "sunny-outline", morningSlots)}
            {renderSection(
                "Afternoon (12:00 - 18:00)",
                "partly-sunny-outline",
                afternoonSlots
            )}
            {renderSection("Evening & Night (18:00 - 24:00)", "moon-outline", eveningSlots)}
        </View>
    );
}
