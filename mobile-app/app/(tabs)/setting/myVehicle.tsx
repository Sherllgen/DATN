import DeleteConfirmModal from "@/components/setting_page/DeleteConfirmModal";
import EmptyVehicleState from "@/components/setting_page/EmptyVehicleState";
import VehicleCard, {
    Vehicle,
    VehicleBrand,
} from "@/components/setting_page/VehicleCard";
import VehicleFormModal, {
    VehicleBrandItem,
} from "@/components/setting_page/VehicleFormModal";
import AppHeader from "@/components/ui/AppHeader";
import { LinearGradient } from "expo-linear-gradient";
import { useState } from "react";

import { ScrollView, Text, TouchableOpacity, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function MyVehiclePage() {
    const [vehicles, setVehicles] = useState<Vehicle[]>([
        { id: "1", brand: "VINFAST", modelName: "Evo Neo" },
        { id: "2", brand: "YADEA", modelName: "ODORA S2" },
    ]);
    const [showModal, setShowModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deletingVehicleId, setDeletingVehicleId] = useState<string | null>(
        null
    );
    const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
    const [selectedBrand, setSelectedBrand] = useState<VehicleBrand>("VINFAST");
    const [modelName, setModelName] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const [listVehicleBrand] = useState<VehicleBrandItem[]>([
        { label: "VINFAST", value: "VINFAST" },
        { label: "YADEA", value: "YADEA" },
        { label: "PEGA", value: "PEGA" },
        { label: "ANBICO", value: "ANBICO" },
        { label: "DatBike", value: "DAT_BIKE" },
        { label: "OTHER", value: "OTHER" },
    ]);

    const handleAddVehicle = () => {
        setEditingVehicle(null);
        setSelectedBrand("VINFAST");
        setModelName("");
        setErrorMessage("");
        setShowModal(true);
    };

    const handleEditVehicle = (vehicle: Vehicle) => {
        setEditingVehicle(vehicle);
        setSelectedBrand(vehicle.brand);
        setModelName(vehicle.modelName);
        setErrorMessage("");
        setShowModal(true);
    };

    const handleDeleteVehicle = (vehicleId: string) => {
        setDeletingVehicleId(vehicleId);
        setShowDeleteModal(true);
    };

    const confirmDelete = () => {
        if (deletingVehicleId) {
            setVehicles(vehicles.filter((v) => v.id !== deletingVehicleId));
        }
        setShowDeleteModal(false);
        setDeletingVehicleId(null);
    };

    const handleSaveVehicle = () => {
        if (!modelName.trim()) {
            setErrorMessage("Vui lòng nhập tên xe");
            return;
        }

        setErrorMessage("");
        if (editingVehicle) {
            // Update existing vehicle
            setVehicles(
                vehicles.map((v) =>
                    v.id === editingVehicle.id
                        ? { ...v, brand: selectedBrand, modelName }
                        : v
                )
            );
        } else {
            // Add new vehicle
            const newVehicle: Vehicle = {
                id: Date.now().toString(),
                brand: selectedBrand,
                modelName,
            };
            setVehicles([...vehicles, newVehicle]);
        }

        setShowModal(false);
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 pb-[80px]"
        >
            <SafeAreaView className="flex-1">
                {/* Header */}
                <AppHeader title="My Vehicles" />

                {/* Vehicle List Section */}
                <ScrollView className="flex-1 px-6">
                    <View className="mt-6">
                        <View className="flex-row justify-end items-center mb-4">
                            <TouchableOpacity
                                className="bg-secondary mb-4 px-4 py-2 rounded-lg"
                                activeOpacity={0.7}
                                onPress={handleAddVehicle}
                            >
                                <Text className="px-2 font-medium text-white">
                                    New
                                </Text>
                            </TouchableOpacity>
                        </View>

                        {/* Vehicle Cards */}
                        {vehicles.length === 0 ? (
                            <EmptyVehicleState />
                        ) : (
                            vehicles.map((vehicle) => (
                                <VehicleCard
                                    key={vehicle.id}
                                    vehicle={vehicle}
                                    onEdit={handleEditVehicle}
                                    onDelete={handleDeleteVehicle}
                                />
                            ))
                        )}
                    </View>

                    <View className="h-10" />
                </ScrollView>
            </SafeAreaView>

            {/* Add/Edit Vehicle Modal */}
            <VehicleFormModal
                visible={showModal}
                editingVehicle={editingVehicle}
                selectedBrand={selectedBrand}
                modelName={modelName}
                errorMessage={errorMessage}
                listVehicleBrand={listVehicleBrand}
                onClose={() => setShowModal(false)}
                onBrandChange={setSelectedBrand}
                onModelNameChange={setModelName}
                onSave={handleSaveVehicle}
            />

            {/* Delete Confirmation Modal */}
            <DeleteConfirmModal
                visible={showDeleteModal}
                onCancel={() => setShowDeleteModal(false)}
                onConfirm={confirmDelete}
            />
        </LinearGradient>
    );
}
