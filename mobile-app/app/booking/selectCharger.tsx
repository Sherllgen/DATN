import SelectChargerCard from "@/components/booking/SelectChargerCard";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import { mockChargersResponse } from "@/data/chargers";
import { LinearGradient } from "expo-linear-gradient";
import { router, useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SelectChargerPage() {
    const { stationId, vehicleId } = useLocalSearchParams<{
        stationId: string;
        vehicleId: string;
    }>();

    const chargers = mockChargersResponse.data;

    // Flatten chargers into a list of ports for individual selection
    const allPorts = chargers.flatMap(charger =>
        // We pass the charger info along with the port for display
        charger.ports.map(port => ({
            port,
            charger
        }))
    );

    // Initial select: first available port
    const [selectedPortId, setSelectedPortId] = useState<number | null>(
        allPorts.find(p => p.port.status === "AVAILABLE")?.port.id ?? null
    );

    const handleContinue = () => {
        if (!selectedPortId) return;
        router.push(
            `/booking/selectTime?stationId=${stationId}&vehicleId=${vehicleId}&portId=${selectedPortId}`
        );
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1"
        >
            <SafeAreaView className="flex-1">
                <AppHeader title="Select Charger" />

                <ScrollView className="flex-1 px-6 mt-4">
                    {allPorts.map(({ port, charger }) => (
                        <SelectChargerCard
                            key={port.id}
                            charger={charger}
                            port={port}
                            isSelected={selectedPortId === port.id}
                            onSelect={setSelectedPortId}
                        />
                    ))}
                    <View className="h-6" />
                </ScrollView>

                {/* Continue Button */}
                <View className="px-6 pt-6">
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
            </SafeAreaView>
        </LinearGradient>
    );
}
