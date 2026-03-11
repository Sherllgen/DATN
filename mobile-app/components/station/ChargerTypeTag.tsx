import React from "react";
import { View, Text } from "react-native";

export interface ChargerTypeTagProps {
    /** Charger connector type (e.g., CCS2, Type 2, CHAdeMO) */
    type: string;
    /** Number of available chargers of this type */
    available?: number;
    /** Total chargers of this type */
    total?: number;
    /** Custom className */
    className?: string;
}

export default function ChargerTypeTag({
    type,
    available,
    total,
    className = "",
}: ChargerTypeTagProps) {
    const showCount = available !== undefined && total !== undefined;

    return (
        <View
            className={[
                "flex-row items-center px-3 py-2 rounded-lg bg-border-gray/20 border border-border-gray",
                className,
            ].join(" ")}
        >
            <Text className="text-sm font-medium text-white">
                {type}
                {showCount && (
                    <Text className="text-[#9BA1A6]">
                        {" "}
                        ({available}/{total})
                    </Text>
                )}
            </Text>
        </View>
    );
}
