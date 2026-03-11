import React from "react";
import { View, Text } from "react-native";
import { Ionicons, MaterialIcons } from "@expo/vector-icons";

export interface AmenityChipProps {
    /** Amenity name */
    name: string;
    /** Icon name from Ionicons or MaterialIcons */
    icon: string;
    /** Icon library to use */
    iconLibrary?: "ionicons" | "material";
    /** Custom className */
    className?: string;
}

export default function AmenityChip({
    name,
    icon,
    iconLibrary = "ionicons",
    className = "",
}: AmenityChipProps) {
    const IconComponent =
        iconLibrary === "ionicons" ? Ionicons : MaterialIcons;

    return (
        <View
            className={[
                "flex-row items-center px-3 py-2 rounded-lg bg-transparent border border-border-gray",
                className,
            ].join(" ")}
        >
            <IconComponent name={icon as any} size={16} color="#4CAF50" />
            <Text className="text-sm font-medium text-white ml-2">{name}</Text>
        </View>
    );
}
