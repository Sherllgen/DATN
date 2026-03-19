import { MaterialCommunityIcons } from "@expo/vector-icons";
import React from "react";
import { Text, TouchableOpacity, View } from "react-native";
import ListItemCard from "@/components/ui/ListItemCard";
import { Charger, ConnectorType, ChargerPort } from "@/data/chargers";
import { AppColors } from "@/constants/theme";

// Map connector type → icon name
const connectorIconMap: Record<ConnectorType, React.ComponentProps<typeof MaterialCommunityIcons>["name"]> = {
    SOCKET_220V: "power-socket",
    CCS1: "ev-plug-ccs1",
    CCS2: "ev-plug-ccs2",
    CHADEMO: "ev-plug-chademo",
    TYPE2: "ev-plug-type2",
    J1772: "ev-plug-type1",
};

export interface SelectChargerCardProps {
    charger: Charger;
    port: ChargerPort;
    isSelected: boolean;
    onSelect: (portId: number) => void;
}

export default function SelectChargerCard({
    charger,
    port,
    isSelected,
    onSelect,
}: SelectChargerCardProps) {
    const iconName = connectorIconMap[charger.connectorType] ?? "lightning-bolt";

    return (
        <TouchableOpacity
            activeOpacity={0.7}
            onPress={() => onSelect(port.id)}
            disabled={port.status !== "AVAILABLE"}
        >
            <ListItemCard
                icon={
                    <MaterialCommunityIcons
                        name={iconName}
                        size={30}
                        color={port.status === "AVAILABLE" ? AppColors.secondary : AppColors.textSecondary}
                    />
                }
                title={`${charger.name} - Port ${port.portNumber}`}
                titleClassName={`${port.status === "AVAILABLE" ? "text-secondary" : "text-text-secondary"} font-semibold text-base`}
                actions={
                    <View
                        className={`w-6 h-6 rounded-full border-2 items-center justify-center ${isSelected ? "border-secondary" : "border-border-gray"
                            } ${port.status !== "AVAILABLE" ? "opacity-50" : ""}`}
                    >
                        {isSelected && (
                            <View className="w-3 h-3 rounded-full bg-secondary" />
                        )}
                    </View>
                }
                elementClassName={port.status !== "AVAILABLE" ? "opacity-60" : ""}
            >
                {/* Children: connector type - max power - status */}
                <View className="flex-row items-center flex-wrap gap-x-2">
                    <Text className="text-text-secondary text-sm">
                        {charger.connectorType.replace("_", " ")}
                    </Text>
                    <View className="w-[1px] h-8 bg-border-gray mx-1" />
                    <View>
                        <Text className="text-text-secondary text-xs">Max. power</Text>
                        <Text className="text-white font-semibold text-base">
                            {charger.maxPower} kW
                        </Text>
                    </View>
                    {/* <View className="w-[1px] h-4 bg-border-gray mx-1" />
                    <Text className={`${port.status === "AVAILABLE" ? "text-secondary" : "text-error"} text-sm font-medium`}>
                        {port.status}
                    </Text> */}
                </View>
            </ListItemCard>
        </TouchableOpacity>
    );
}
