import SelectVehicleCard from "@/components/booking/SelectVehicleCard";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import { getAllVehicleApi } from "@/apis/vehicleApi/vehicleApi";
import { LinearGradient } from "expo-linear-gradient";
import { router, useLocalSearchParams } from "expo-router";
import { useState, useEffect } from "react";
import { ScrollView, View, ActivityIndicator, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";

export default function SelectVehiclePage() {
    const { stationId } = useLocalSearchParams<{ stationId: string }>();

    const [vehicles, setVehicles] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);
    const [selectedId, setSelectedId] = useState<string | null>(null);

    useEffect(() => {
        const fetchVehicles = async () => {
            try {
                setLoading(true);
                const res = await getAllVehicleApi();
                // Depending on the interceptor, res might be the ApiResponse object
                const data = res.data || res; 
                setVehicles(data);
                if (data.length > 0) {
                    setSelectedId(data[0].id.toString());
                }
            } catch (err: any) {
                console.error("Failed to fetch vehicles:", err);
                setErrorMsg("Failed to load your vehicles. Please try again.");
            } finally {
                setLoading(false);
            }
        };
        fetchVehicles();
    }, []);

    const handleContinue = () => {
        if (!selectedId) return;
        router.push(`/booking/selectCharger?stationId=${stationId}&vehicleId=${selectedId}`);
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1"
        >
            <SafeAreaView className="flex-1">
                <AppHeader title="Select Your Vehicle" />

                <ScrollView className="flex-1 px-6 mt-4">
                    {loading ? (
                        <ActivityIndicator size="large" color="#00A452" className="mt-10" />
                    ) : errorMsg ? (
                        <Text className="text-red-500 text-center mt-10">{errorMsg}</Text>
                    ) : vehicles.length === 0 ? (
                        <View className="bg-surface-dark border border-[#33404F] p-8 rounded-2xl items-center justify-center mt-4">
                            <View className="w-16 h-16 rounded-full bg-[#1A2530] items-center justify-center mb-4">
                                <Ionicons name="car-outline" size={32} color="#64748B" />
                            </View>
                            <Text className="text-white font-semibold text-lg text-center">
                                No Vehicles Found
                            </Text>
                            <Text className="text-gray-400 text-sm mt-2 text-center leading-5">
                                You haven't added any vehicles yet. Please add a vehicle in your profile first.
                            </Text>
                        </View>
                    ) : (
                        vehicles.map((vehicle) => (
                            <SelectVehicleCard
                                key={vehicle.id}
                                vehicle={{...vehicle, id: vehicle.id.toString()}} // normalize ID string
                                isSelected={selectedId === vehicle.id.toString()}
                                onSelect={(id) => setSelectedId(id.toString())}
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
                        disabled={!selectedId}
                        onPress={handleContinue}
                    >
                        Continue
                    </Button>
                </View>
            </SafeAreaView>
        </LinearGradient>
    );
}
