import { Feather, FontAwesome5, MaterialIcons } from "@expo/vector-icons";
import { Text, TouchableOpacity, View } from "react-native";
import ListItemCard from "../ui/ListItemCard";

export type VehicleBrand =
    | "VINFAST"
    | "YADEA"
    | "PEGA"
    | "ANBICO"
    | "DAT_BIKE"
    | "OTHER";

export interface Vehicle {
    id: string;
    brand: VehicleBrand;
    modelName: string;
    connectorTypes?: string[];
}

interface VehicleCardProps {
    vehicle: Vehicle;
    onEdit: (vehicle: Vehicle) => void;
    onDelete: (vehicleId: string) => void;
}

export default function VehicleCard({
    vehicle,
    onEdit,
    onDelete,
}: VehicleCardProps) {
    console.log("vehile: ", vehicle);

    return (
        <ListItemCard
            icon={<MaterialIcons name="electric-bike" size={22} color="#4CAF50" />}
            title={vehicle.brand}
            actions={
                <View className="flex-row">
                    <TouchableOpacity
                        className="bg-[#4CAF50]/20 mr-2 p-3 rounded-lg"
                        activeOpacity={0.7}
                        onPress={() => onEdit(vehicle)}
                    >
                        <Feather name="edit" size={18} color="#4CAF50" />
                    </TouchableOpacity>
                    <TouchableOpacity
                        className="bg-red-500/20 p-3 rounded-lg"
                        activeOpacity={0.7}
                        onPress={() => onDelete(vehicle.id)}
                    >
                        <FontAwesome5 name="trash" size={18} color="#EF4444" />
                    </TouchableOpacity>
                </View>
            }
        >
            <Text className="text-[#ccc] text-sm">
                Model: {vehicle.modelName}
            </Text>
            <Text className="mt-2 text-[#ccc] text-sm">
                Connectors:{" "}
                {vehicle.connectorTypes?.join(", ") || "N/A"}
            </Text>
        </ListItemCard>
    );
}
