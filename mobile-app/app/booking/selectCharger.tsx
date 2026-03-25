import SelectChargerCard from "@/components/booking/SelectChargerCard";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import { getChargersByStationId } from "@/apis/chargerApi";
import { ChargerResponse } from "@/types/charger.types";
import { LinearGradient } from "expo-linear-gradient";
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
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    const [selectedPortId, setSelectedPortId] = useState<number | null>(null);

    useEffect(() => {
        const fetchChargers = async () => {
            if (!stationId) return;
            try {
                setLoading(true);
                const data = await getChargersByStationId(Number(stationId));
                setChargers(data);
                
                const availablePort = data.flatMap(c => c.ports).find(p => p.status === "AVAILABLE");
                if (availablePort) {
                    setSelectedPortId(availablePort.id);
                }
            } catch (err) {
                console.error("Failed to fetch chargers:", err);
                setErrorMsg("Failed to load chargers for this station.");
            } finally {
                setLoading(false);
            }
        };
        fetchChargers();
    }, [stationId]);

    const allPorts = chargers.flatMap(charger =>
        charger.ports.map(port => ({
            port,
            charger
        }))
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
                                onSelect={setSelectedPortId}
                            />
                        ))
                    )}
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
