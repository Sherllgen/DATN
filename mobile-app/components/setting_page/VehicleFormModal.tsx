import Dropdown from "@/components/ui/Dropdown";
import Button from "@/components/ui/Button";
import { Ionicons } from "@expo/vector-icons";
import { Modal, Text, TextInput, TouchableOpacity, View, KeyboardAvoidingView, Platform } from "react-native";

export type VehicleBrand =
    | "VINFAST"
    | "YADEA"
    | "PEGA"
    | "ANBICO"
    | "DAT_BIKE"
    | "OTHER";

export type VehicleBrandItem = { label: string; value: VehicleBrand };

export interface Vehicle {
    id: string;
    brand: VehicleBrand;
    modelName: string;
    connectorTypes?: string[];
}

interface VehicleFormModalProps {
    visible: boolean;
    editingVehicle: Vehicle | null;
    selectedBrand: VehicleBrand;
    modelName: string;
    connectorTypes: string[];
    errorMessage: string;
    listVehicleBrand: VehicleBrandItem[];
    onClose: () => void;
    onBrandChange: (brand: VehicleBrand) => void;
    onModelNameChange: (name: string) => void;
    onConnectorTypesChange: (types: string[]) => void;
    onSave: () => void;
    isSaving?: boolean;
}

export default function VehicleFormModal({
    visible,
    editingVehicle,
    selectedBrand,
    modelName,
    connectorTypes,
    errorMessage,
    listVehicleBrand,
    onClose,
    onBrandChange,
    onModelNameChange,
    onConnectorTypesChange,
    onSave,
    isSaving = false,
}: VehicleFormModalProps) {
    const availableConnectors = [
        { id: "SOCKET_220V", name: "Socket 220V" },
        { id: "VINFAST_STD", name: "VinFast Standard" },
        { id: "DATBIKE_FAST", name: "DatBike Fast" },
        { id: "IEC_TYPE_2", name: "IEC Type 2" },
        { id: "OTHER", name: "Other" },
    ];

    const toggleConnector = (connectorId: string) => {
        if (connectorTypes.includes(connectorId)) {
            onConnectorTypesChange(
                connectorTypes.filter((id) => id !== connectorId)
            );
        } else {
            onConnectorTypesChange([...connectorTypes, connectorId]);
        }
    };
    return (
        <Modal
            visible={visible}
            animationType="slide"
            transparent={true}
            onRequestClose={onClose}
        >
            <KeyboardAvoidingView
                behavior={Platform.OS === "ios" ? "padding" : "height"}
                className="flex-1"
            >
                <TouchableOpacity
                    className="flex-1 justify-end bg-black/60"
                    onPress={onClose}
                    activeOpacity={1}
                >
                    <TouchableOpacity activeOpacity={1}>
                        <View className="bg-surface pb-8 rounded-t-3xl">
                            {/* Swipe Indicator */}
                            <View className="items-center mt-2 mb-2">
                                <View className="bg-border-gray rounded-full w-12 h-1" />
                            </View>

                            <View className="flex-row justify-center items-center px-6 pb-4 border-border-gray border-b">
                                <Text className="font-semibold text-white text-lg text-center">
                                    {editingVehicle
                                        ? "Edit vehicle"
                                        : "Add new vehicle"}
                                </Text>
                            </View>

                            <View className="px-6 pt-4">
                                {/* Error Message */}
                                {errorMessage ? (
                                    <View className="bg-red-500/20 mb-4 p-3 border border-red-500/50 rounded-lg">
                                        <Text className="text-red-400 text-sm">
                                            {errorMessage}
                                        </Text>
                                    </View>
                                ) : null}

                                {/* Brand Dropdown */}
                                <Dropdown
                                    label="Brand name"
                                    value={selectedBrand}
                                    items={listVehicleBrand}
                                    onValueChange={(value) =>
                                        onBrandChange(value as VehicleBrand)
                                    }
                                />

                                {/* Model Name Input */}
                                <View className="mt-4">
                                    <Text className="mb-1 text-[#9BA1A6] text-sm">
                                        Vehicle name*
                                    </Text>
                                    <TextInput
                                        value={modelName}
                                        onChangeText={onModelNameChange}
                                        placeholder="EVO 200"
                                        className="pb-3 border-border-gray border-b text-[#4CAF50] text-base"
                                        placeholderTextColor="#9BA1A6"
                                    />
                                </View>

                                {/* Connector Types */}
                                <View className="mt-4">
                                    <Text className="mb-3 text-[#9BA1A6] text-sm">
                                        Connector Types*
                                    </Text>
                                    <View className="flex-row flex-wrap gap-2">
                                        {availableConnectors.map((connector) => (
                                            <TouchableOpacity
                                                key={connector.id}
                                                onPress={() =>
                                                    toggleConnector(connector.id)
                                                }
                                                className={`px-4 py-2 rounded-lg border ${connectorTypes.includes(
                                                    connector.id
                                                )
                                                    ? "bg-[#4CAF50] border-[#4CAF50]"
                                                    : "bg-transparent border-border-gray"
                                                    }`}
                                                activeOpacity={0.7}
                                            >
                                                <Text
                                                    className={
                                                        connectorTypes.includes(
                                                            connector.id
                                                        )
                                                            ? "text-white"
                                                            : "text-[#9BA1A6]"
                                                    }
                                                >
                                                    {connector.name}
                                                </Text>
                                            </TouchableOpacity>
                                        ))}
                                    </View>
                                </View>

                                {/* Save Button */}
                                <Button
                                    className="mt-6"
                                    variant="primary"
                                    fullWidth
                                    onPress={onSave}
                                    loading={isSaving}
                                >
                                    {editingVehicle ? "Save changes" : "Add vehicle"}
                                </Button>
                            </View>
                        </View>
                    </TouchableOpacity>
                </TouchableOpacity>
            </KeyboardAvoidingView>
        </Modal>
    );
}
