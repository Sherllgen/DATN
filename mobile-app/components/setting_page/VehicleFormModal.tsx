import Dropdown from "@/components/ui/Dropdown";
import { Ionicons } from "@expo/vector-icons";
import { Modal, Text, TextInput, TouchableOpacity, View } from "react-native";

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
}

interface VehicleFormModalProps {
    visible: boolean;
    editingVehicle: Vehicle | null;
    selectedBrand: VehicleBrand;
    modelName: string;
    errorMessage: string;
    listVehicleBrand: VehicleBrandItem[];
    onClose: () => void;
    onBrandChange: (brand: VehicleBrand) => void;
    onModelNameChange: (name: string) => void;
    onSave: () => void;
}

export default function VehicleFormModal({
    visible,
    editingVehicle,
    selectedBrand,
    modelName,
    errorMessage,
    listVehicleBrand,
    onClose,
    onBrandChange,
    onModelNameChange,
    onSave,
}: VehicleFormModalProps) {
    return (
        <Modal
            visible={visible}
            animationType="slide"
            transparent={true}
            onRequestClose={onClose}
        >
            <TouchableOpacity
                className="flex-1 justify-end bg-black/60"
                onPress={onClose}
                activeOpacity={1}
            >
                <TouchableOpacity activeOpacity={1}>
                    <View className="bg-[#33404F] pb-8 rounded-t-3xl">
                        <View className="flex-row justify-between items-center px-6 py-4 border-[#4A5568] border-b">
                            <Text className="font-semibold text-white text-lg">
                                {editingVehicle
                                    ? "Chỉnh sửa phương tiện"
                                    : "Thêm phương tiện"}
                            </Text>
                            <TouchableOpacity onPress={onClose}>
                                <Ionicons
                                    name="close"
                                    size={28}
                                    color="#9BA1A6"
                                />
                            </TouchableOpacity>
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
                                label="Hãng xe"
                                value={selectedBrand}
                                items={listVehicleBrand}
                                onValueChange={(value) =>
                                    onBrandChange(value as VehicleBrand)
                                }
                            />

                            {/* Model Name Input */}
                            <View className="mt-4">
                                <Text className="mb-1 text-[#9BA1A6] text-sm">
                                    Tên xe
                                </Text>
                                <TextInput
                                    value={modelName}
                                    onChangeText={onModelNameChange}
                                    placeholder="Nhập tên xe"
                                    className="pb-3 border-[#4A5568] border-b text-[#4CAF50] text-base"
                                    placeholderTextColor="#9BA1A6"
                                />
                            </View>

                            {/* Save Button */}
                            <TouchableOpacity
                                className="bg-[#4CAF50] mt-6 py-4 rounded-lg"
                                activeOpacity={0.7}
                                onPress={onSave}
                            >
                                <Text className="font-semibold text-white text-base text-center">
                                    {editingVehicle
                                        ? "Cập nhật"
                                        : "Thêm phương tiện"}
                                </Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </TouchableOpacity>
            </TouchableOpacity>
        </Modal>
    );
}
