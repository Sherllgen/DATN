import { MaterialIcons } from "@expo/vector-icons";
import React from "react";
import { Text, TouchableOpacity, View } from "react-native";
import ListItemCard from "@/components/ui/ListItemCard";
import { Vehicle } from "@/components/setting_page/VehicleCard";
import { AppColors } from "@/constants/theme";

export interface SelectVehicleCardProps {
    vehicle: Vehicle;
    isSelected: boolean;
    onSelect: (vehicleId: string) => void;
}

export default function SelectVehicleCard({
    vehicle,
    isSelected,
    onSelect,
}: SelectVehicleCardProps) {
    return (
        <TouchableOpacity activeOpacity={0.7} onPress={() => onSelect(vehicle.id)}>
            <ListItemCard
                icon={
                    <MaterialIcons name="electric-bike" size={22} color={AppColors.secondary} />
                }
                title={vehicle.brand}
                titleClassName="text-secondary font-semibold text-base"
                actions={
                    <View
                        className={`w-6 h-6 rounded-full border-2 items-center justify-center ${isSelected ? "border-secondary" : "border-border-gray"
                            }`}
                    >
                        {isSelected && (
                            <View className="w-3 h-3 rounded-full bg-secondary" />
                        )}
                    </View>
                }
            >
                <Text className="text-text-muted text-sm">
                    Model: {vehicle.modelName}
                </Text>
                <Text className="mt-1 text-text-muted text-sm">
                    Connectors:{" "}
                    {vehicle.connectorTypes?.join(", ") || "N/A"}
                </Text>
            </ListItemCard>
        </TouchableOpacity>
    );
}
