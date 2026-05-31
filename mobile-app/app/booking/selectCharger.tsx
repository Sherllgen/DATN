import SelectChargerCard from "@/components/booking/SelectChargerCard";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import GradientBackground from "@/components/ui/GradientBackground";
import { getChargersByStationId } from "@/apis/chargerApi";
import { getAllVehicleApi } from "@/apis/vehicleApi/vehicleApi";
import { ChargerResponse } from "@/types/charger.types";
import { router, useLocalSearchParams } from "expo-router";
import { useState, useEffect } from "react";
import { ScrollView, View, ActivityIndicator, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";

export default function SelectChargerPage() {
    const { stationId, vehicleId } = useLocalSearchParams<{
        stationId: string;
        vehicleId: string;
    }>();

    const [chargers, setChargers] = useState<ChargerResponse[]>([]);
    const [vehicle, setVehicle] = useState<any | null>(null);
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    const [selectedPortId, setSelectedPortId] = useState<number | null>(null);
    const [hasBypassedWarning, setHasBypassedWarning] = useState(false);

    // Warning Modal State
    const [showWarningModal, setShowWarningModal] = useState(false);
    const [pendingPortId, setPendingPortId] = useState<number | null>(null);
    const [pendingPortDetails, setPendingPortDetails] = useState<{
        portNumber: number;
        chargerName: string;
        connectorType: string;
    } | null>(null);

    useEffect(() => {
        const fetchInitialData = async () => {
            if (!stationId || !vehicleId) return;
            try {
                setLoading(true);
                setErrorMsg(null);

                const [chargersData, vehiclesRes] = await Promise.all([
                    getChargersByStationId(Number(stationId)),
                    getAllVehicleApi(),
                ]);

                setChargers(chargersData);

                const vehiclesList = vehiclesRes.data || vehiclesRes;
                const matchedVehicle = vehiclesList.find((v: any) => v.id.toString() === vehicleId);
                setVehicle(matchedVehicle);

                const allPortsList = chargersData.flatMap(charger =>
                    charger.ports.map(port => ({
                        port,
                        charger
                    }))
                );

                const vehicleConnectors = matchedVehicle?.connectorTypes || [];

                // Smart auto-selection: pre-select the first available compatible port
                const compatibleAvailablePort = allPortsList.find(
                    ({ port, charger }) =>
                        port.status === "AVAILABLE" &&
                        vehicleConnectors.includes(charger.connectorType)
                );

                if (compatibleAvailablePort) {
                    setSelectedPortId(compatibleAvailablePort.port.id);
                    setHasBypassedWarning(false);
                } else {
                    // Fallback: select any available port
                    const anyAvailablePort = allPortsList.find(({ port }) => port.status === "AVAILABLE");
                    if (anyAvailablePort) {
                        setSelectedPortId(anyAvailablePort.port.id);
                        setHasBypassedWarning(false);
                    }
                }
            } catch (err) {
                console.error("Failed to fetch charger/vehicle details:", err);
                setErrorMsg("Failed to load charger or vehicle details.");
            } finally {
                setLoading(false);
            }
        };

        fetchInitialData();
    }, [stationId, vehicleId]);

    const allPorts = chargers.flatMap(charger =>
        charger.ports.map(port => ({
            port,
            charger
        }))
    );

    const handleSelectPort = (portId: number) => {
        const found = allPorts.find(p => p.port.id === portId);
        if (!found) return;

        const { port, charger } = found;
        const vehicleConnectors = vehicle?.connectorTypes || [];
        const isCompatible = vehicleConnectors.includes(charger.connectorType);

        if (vehicle && vehicleConnectors.length > 0 && !isCompatible) {
            setPendingPortId(portId);
            setPendingPortDetails({
                portNumber: port.portNumber,
                chargerName: charger.name,
                connectorType: charger.connectorType
            });
            setShowWarningModal(true);
        } else {
            setSelectedPortId(portId);
            setHasBypassedWarning(false);
        }
    };

    const handleContinue = () => {
        if (!selectedPortId) return;

        // Perform validation just in case
        const found = allPorts.find(p => p.port.id === selectedPortId);
        if (found) {
            const { port, charger } = found;
            const vehicleConnectors = vehicle?.connectorTypes || [];
            const isCompatible = vehicleConnectors.includes(charger.connectorType);

            if (vehicle && vehicleConnectors.length > 0 && !isCompatible && !hasBypassedWarning) {
                setPendingPortId(selectedPortId);
                setPendingPortDetails({
                    portNumber: port.portNumber,
                    chargerName: charger.name,
                    connectorType: charger.connectorType
                });
                setShowWarningModal(true);
                return;
            }
        }

        router.push(
            `/booking/selectTime?stationId=${stationId}&vehicleId=${vehicleId}&portId=${selectedPortId}`
        );
    };

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView className="flex-1">
                <AppHeader title="Select Charger" />

                <ScrollView className="flex-1 px-6 mt-4">
                    {loading ? (
                        <ActivityIndicator size="large" color="#00A452" className="mt-10" />
                    ) : errorMsg ? (
                        <Text className="text-red-500 text-center mt-10">{errorMsg}</Text>
                    ) : allPorts.length === 0 ? (
                        <View className="bg-surface-dark border border-[#33404F] p-8 rounded-2xl items-center justify-center mt-4">
                            <View className="w-16 h-16 rounded-full bg-[#1A2530] items-center justify-center mb-4">
                                <Ionicons name="battery-charging-outline" size={32} color="#64748B" />
                            </View>
                            <Text className="text-white font-semibold text-lg text-center">
                                No Chargers Available
                            </Text>
                            <Text className="text-gray-400 text-sm mt-2 text-center leading-5">
                                There are no compatible chargers or ports available at this station right now.
                            </Text>
                        </View>
                    ) : (
                        allPorts.map(({ port, charger }) => (
                            <SelectChargerCard
                                key={port.id}
                                charger={charger as any}
                                port={port as any}
                                isSelected={selectedPortId === port.id}
                                onSelect={handleSelectPort}
                            />
                        ))
                    )}
                    <View className="h-6" />
                </ScrollView>

                {/* Continue Button */}
                <View className="px-6 pt-6 pb-2">
                    <Button
                        variant="primary"
                        size="lg"
                        fullWidth
                        disabled={!selectedPortId}
                        onPress={handleContinue}
                    >
                        Continue
                    </Button>
                </View>

                {/* Warning Modal */}
                <Modal
                    visible={showWarningModal}
                    onClose={() => {
                        setShowWarningModal(false);
                        setPendingPortId(null);
                        setPendingPortDetails(null);
                    }}
                    variant="bottom-sheet"
                    title="Incompatible Connector"
                >
                    <View className="items-center pb-4">
                        <View className="w-16 h-16 rounded-full bg-red-500/20 items-center justify-center mb-4">
                            <Ionicons name="warning-outline" size={36} color="#EF4444" />
                        </View>

                        <Text className="text-white text-base text-center leading-6 mb-6">
                            The selected port (<Text className="font-semibold text-secondary">{pendingPortDetails?.chargerName} - Port {pendingPortDetails?.portNumber}</Text>) uses the <Text className="font-semibold text-secondary">{pendingPortDetails?.connectorType.replace("_", " ")}</Text> connector type.{"\n\n"}
                            However, your selected vehicle (<Text className="font-semibold text-secondary">{vehicle?.brand} {vehicle?.modelName}</Text>) only supports:{" "}
                            <Text className="font-semibold text-secondary">
                                {vehicle?.connectorTypes && vehicle.connectorTypes.length > 0
                                    ? vehicle.connectorTypes.join(", ")
                                    : "N/A"}
                            </Text>.{"\n\n"}
                            Do you still want to choose this port?
                        </Text>

                        <View className="w-full flex-row gap-4">
                            <View className="flex-1">
                                <Button
                                    variant="outline"
                                    size="md"
                                    onPress={() => {
                                        setShowWarningModal(false);
                                        setPendingPortId(null);
                                        setPendingPortDetails(null);
                                    }}
                                    fullWidth
                                    className="border-border-gray"
                                >
                                    Change Port
                                </Button>
                            </View>
                            <View className="flex-1">
                                <Button
                                    variant="primary"
                                    size="md"
                                    onPress={() => {
                                        if (pendingPortId !== null) {
                                            setSelectedPortId(pendingPortId);
                                            setHasBypassedWarning(true);
                                            // Close modal and navigate immediately
                                            setShowWarningModal(false);
                                            setPendingPortId(null);
                                            setPendingPortDetails(null);
                                            router.push(
                                                `/booking/selectTime?stationId=${stationId}&vehicleId=${vehicleId}&portId=${pendingPortId}`
                                            );
                                        }
                                    }}
                                    fullWidth
                                >
                                    Select Anyway
                                </Button>
                            </View>
                        </View>
                    </View>
                </Modal>
            </SafeAreaView>
        </GradientBackground>
    );
}
