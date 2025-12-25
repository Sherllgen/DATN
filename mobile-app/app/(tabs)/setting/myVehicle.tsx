import {
    addVehicleApi,
    deleteVehicleApi,
    getAllVehicleApi,
    updateVehicleApi,
} from "@/apis/vehicleApi/vehicleApi";
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
import { logAxiosError } from "@/utils/errorLogger";
import { LinearGradient } from "expo-linear-gradient";
import { useFocusEffect } from "expo-router";
import { useCallback, useState } from "react";

import {
    RefreshControl,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Toast } from "toastify-react-native";

export default function MyVehiclePage() {
    const [vehicles, setVehicles] = useState<Vehicle[]>([
        { id: "1", brand: "VINFAST", modelName: "Evo Neo" },
        { id: "2", brand: "YADEA", modelName: "ODORA S2" },
    ]);
    const [refreshing, setRefreshing] = useState(false);
    const [showModal, setShowModal] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [deletingVehicleId, setDeletingVehicleId] = useState<string | null>(
        null
    );
    const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);
    const [selectedBrand, setSelectedBrand] = useState<VehicleBrand>("VINFAST");
    const [modelName, setModelName] = useState("");
    const [connectorTypes, setConnectorTypes] = useState<string[]>([]);
    const [errorMessage, setErrorMessage] = useState("");
    const [isSaving, setIsSaving] = useState(false);
    const [isDeleting, setIsDeleting] = useState(false);

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
        setConnectorTypes([]);
        setErrorMessage("");
        setShowModal(true);
    };

    const handleEditVehicle = (vehicle: Vehicle) => {
        setEditingVehicle(vehicle);
        setSelectedBrand(vehicle.brand);
        setModelName(vehicle.modelName);
        setConnectorTypes(vehicle.connectorTypes || []);
        setErrorMessage("");
        setShowModal(true);
    };

    const handleDeleteVehicle = (vehicleId: string) => {
        setDeletingVehicleId(vehicleId);
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        if (!deletingVehicleId) {
            return;
        }

        setIsDeleting(true);
        try {
            await deleteVehicleApi(deletingVehicleId);
            await fetchAllVehicles();
            Toast.success("Vehicle deleted successfully");
            setShowDeleteModal(false);
            setDeletingVehicleId(null);
        } catch (error) {
            logAxiosError(error);
            Toast.error("Failed to delete vehicle");
        } finally {
            setIsDeleting(false);
        }
    };

    const validateVehicleInput = () => {
        if (!modelName.trim()) {
            setErrorMessage("Vui lòng nhập tên xe");
            return false;
        }

        if (connectorTypes.length === 0) {
            setErrorMessage("Vui lòng chọn ít nhất 1 loại sạc");
            return false;
        }

        setErrorMessage("");
        return true;
    };

    const handleAddNewVehicle = async () => {
        if (!validateVehicleInput()) return;

        setIsSaving(true);
        try {
            const res = await addVehicleApi(
                selectedBrand,
                modelName,
                connectorTypes
            );

            if (res.status === 200 || res.status === 201) {
                Toast.success("Vehicle added successfully");
                await fetchAllVehicles();
                setShowModal(false);
            }
        } catch (error) {
            logAxiosError(error);
            setErrorMessage("Có lỗi xảy ra khi thêm xe");
        } finally {
            setIsSaving(false);
        }
    };

    const handleUpdateVehicle = async () => {
        if (!validateVehicleInput() || !editingVehicle) return;

        setIsSaving(true);
        try {
            const res = await updateVehicleApi(
                editingVehicle.id,
                selectedBrand,
                modelName,
                connectorTypes
            );

            if (res.status === 200 || res.status === 201) {
                Toast.success("Vehicle updated successfully");
                await fetchAllVehicles();
                setShowModal(false);
            }
        } catch (error) {
            logAxiosError(error);
            setErrorMessage("Có lỗi xảy ra khi cập nhật xe");
        } finally {
            setIsSaving(false);
        }
    };

    const handleSaveVehicle = async () => {
        if (editingVehicle) {
            await handleUpdateVehicle();
        } else {
            await handleAddNewVehicle();
        }
    };

    const fetchAllVehicles = async () => {
        try {
            const res = await getAllVehicleApi();

            const data = res.data.map((item: any) => ({
                id: item.id,
                brand: item.brand,
                modelName: item.modelName,
            }));

            setVehicles(data);
        } catch (error) {
            logAxiosError(error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            fetchAllVehicles();
        }, [])
    );

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
                <ScrollView
                    className="flex-1 px-6"
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={async () => {
                                setRefreshing(true);
                                await fetchAllVehicles();
                                setRefreshing(false);
                            }}
                            tintColor="#4CAF50"
                            colors={["#4CAF50"]}
                        />
                    }
                >
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
                connectorTypes={connectorTypes}
                errorMessage={errorMessage}
                listVehicleBrand={listVehicleBrand}
                onClose={() => setShowModal(false)}
                onBrandChange={setSelectedBrand}
                onModelNameChange={setModelName}
                onConnectorTypesChange={setConnectorTypes}
                onSave={handleSaveVehicle}
                isSaving={isSaving}
            />

            {/* Delete Confirmation Modal */}
            <DeleteConfirmModal
                visible={showDeleteModal}
                onCancel={() => setShowDeleteModal(false)}
                onConfirm={confirmDelete}
                isDeleting={isDeleting}
            />
        </LinearGradient>
    );
}
