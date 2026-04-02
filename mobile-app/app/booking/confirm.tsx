import React, { useState, useEffect } from "react";
import { View, Text, ScrollView, Image, ActivityIndicator, Linking } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useLocalSearchParams, useRouter } from "expo-router";
import { MaterialCommunityIcons, MaterialIcons, Ionicons } from "@expo/vector-icons";
import AppHeader from "@/components/ui/AppHeader";
import GradientBackground from "@/components/ui/GradientBackground";
import ListItemCard from "@/components/ui/ListItemCard";
import Button from "@/components/ui/Button";
import { AppColors } from "@/constants/theme";
import { useStationCache } from "@/stores/stationCacheStore";
import { bookingDurations } from "@/data/booking";
import { getStationById } from "@/apis/stationApi/stationApi";
import { getChargersByStationId } from "@/apis/chargerApi";
import { getAllVehicleApi } from "@/apis/vehicleApi/vehicleApi";
import { checkAvailability, createBooking } from "@/apis/bookingApi";
import { getInvoiceByBookingId, createZaloPayOrder } from "@/apis/paymentApi";
import { Station } from "@/types/station.types";
import { ChargerResponse, PortResponse } from "@/types/charger.types";

export default function ConfirmBookingScreen() {
    const router = useRouter();
    const { stationId, vehicleId, portId, date, time, duration } = useLocalSearchParams<{
        stationId: string;
        vehicleId: string;
        portId: string;
        date: string;
        time: string;
        duration: string;
    }>();

    const getCachedStation = useStationCache(state => state.getStation);
    const [station, setStation] = useState<Station | null>(() => {
        return stationId ? getCachedStation(Number(stationId)) : null;
    });
    const [allChargers, setAllChargers] = useState<ChargerResponse[]>([]);
    const [vehicle, setVehicle] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    // Locking Phase State
    const [lockStatus, setLockStatus] = useState<'idle' | 'loading' | 'success' | 'failed'>('idle');
    const [errorMessage, setErrorMessage] = useState<string | null>(null);
    const [timeLeft, setTimeLeft] = useState<number>(300); // 5 minutes (300 seconds)
    const [isAcquiringLock, setIsAcquiringLock] = useState(false);
    const [isProcessingBooking, setIsProcessingBooking] = useState(false);

    useEffect(() => {
        const fetchInitialData = async () => {
            if (!stationId) return;
            setLoading(true);
            try {
                if (!station) {
                    const st = await getStationById(Number(stationId));
                    setStation(st);
                    useStationCache.getState().setStation(st);
                }

                const [chargersList, vehiclesList] = await Promise.all([
                    getChargersByStationId(Number(stationId)),
                    getAllVehicleApi().then(res => res.data || res)
                ]);

                setAllChargers(chargersList);

                const matchedVehicle = vehiclesList.find((v: any) => v.id.toString() === vehicleId) || vehiclesList[0];
                setVehicle(matchedVehicle);

            } catch (err) {
                console.error("Failed to load initial data:", err);
            } finally {
                setLoading(false);
            }
        };
        fetchInitialData();
    }, [stationId, vehicleId]);

    let selectedCharger: ChargerResponse | undefined;
    let selectedPort: PortResponse | undefined;

    for (const charger of allChargers) {
        const port = charger.ports.find((p) => p.id === Number(portId));
        if (port) {
            selectedCharger = charger;
            selectedPort = port;
            break;
        }
    }

    if (!selectedCharger && allChargers.length > 0) {
        selectedCharger = allChargers[0];
        selectedPort = selectedCharger.ports[0];
    }

    const durationLabel = bookingDurations.find(d => d.value === duration)?.label || "1 Hour";
    const priceEstimation = 30000 * parseFloat(duration || "1.0");

    // Acquire lock once station details are ready
    useEffect(() => {
        const acquireLock = async () => {
            if (!station || !selectedCharger || !selectedPort || isAcquiringLock || lockStatus !== 'idle') return;

            try {
                setLockStatus('loading');
                setIsAcquiringLock(true);

                // Construct Date strings in local time -> match "YYYY-MM-DDTHH:mm:ss"
                const timeParts = time.split(':');
                const formattedTime = timeParts.length === 2 ? `${time}:00` : time;
                const startStr = `${date}T${formattedTime}`;
                const startTime = new Date(startStr);
                const durationHours = parseFloat(duration || "1.0");
                const endTime = new Date(startTime.getTime() + durationHours * 60 * 60 * 1000);

                const pad = (num: number) => num.toString().padStart(2, '0');
                const formatISO = (d: Date) => {
                    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
                };

                await checkAvailability({
                    stationId: Number(stationId),
                    chargerId: selectedCharger.id,
                    portNumber: selectedPort.portNumber,
                    startTime: formatISO(startTime),
                    endTime: formatISO(endTime)
                });

                setLockStatus('success');
            } catch (err: any) {
                setLockStatus('failed');
                setErrorMessage(err.response?.data?.message || "Slot unavailable. Please select another time.");
            }
        };

        if (station && lockStatus === 'idle') {
            acquireLock();
        }
    }, [station, selectedCharger, selectedPort, lockStatus]);

    // Timer Logic
    useEffect(() => {
        if (lockStatus !== 'success') return;

        if (timeLeft <= 0) {
            setLockStatus('failed');
            setErrorMessage("Session expired. The holding lock has been released.");
            return;
        }

        const timerId = setInterval(() => {
            setTimeLeft(prev => prev - 1);
        }, 1000);

        return () => clearInterval(timerId);
    }, [lockStatus, timeLeft]);

    const timerMins = Math.floor(timeLeft / 60);
    const timerSecs = timeLeft % 60;
    const timerDisplay = `0${timerMins}:${timerSecs < 10 ? '0' : ''}${timerSecs}`;

    const handleConfirmBooking = async () => {
        if (!station || !selectedCharger || !selectedPort) return;

        try {
            setIsProcessingBooking(true);
            const timeParts = time.split(':');
            const formattedTime = timeParts.length === 2 ? `${time}:00` : time;
            const startStr = `${date}T${formattedTime}`;
            const startTime = new Date(startStr);
            const durationHours = parseFloat(duration || "1.0");
            const endTime = new Date(startTime.getTime() + durationHours * 60 * 60 * 1000);

            const pad = (num: number) => num.toString().padStart(2, '0');
            const formatISO = (d: Date) => {
                return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
            };

            // 1. Create Booking
            const booking = await createBooking({
                stationId: Number(stationId),
                chargerId: selectedCharger.id,
                portNumber: selectedPort.portNumber,
                vehicleId: Number(vehicleId),
                startTime: formatISO(startTime),
                endTime: formatISO(endTime)
            });

            // 2. Fetch Invoice generated by backend
            const invoice = await getInvoiceByBookingId(booking.id);

            // 3. Create ZaloPay Order
            const order = await createZaloPayOrder({
                invoiceId: invoice.id,
                userId: booking.userId,
                amount: booking.totalPrice || priceEstimation,
                description: `EV-Go Booking ${booking.id}`
            });

            // 4. Open ZaloPay URL
            if (order.orderUrl) {
                await Linking.openURL(order.orderUrl);
                // Optionally push to a 'Waiting for payment' or 'Home' screen
                router.replace('/');
            }
        } catch (err: any) {
            setErrorMessage(err.response?.data?.message || "Failed to process booking or payment.");
            setLockStatus('failed'); // Reuse the failed screen
        } finally {
            setIsProcessingBooking(false);
        }
    };

    if (loading) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <ActivityIndicator size="large" color="#00A452" />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (!station || !selectedCharger || !selectedPort || !vehicle) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 items-center justify-center">
                    <Text className="text-white mb-4">Required data missing or not found.</Text>
                    <Button onPress={() => router.back()} variant="primary">
                        Go Back
                    </Button>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (lockStatus === 'loading') {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 items-center justify-center px-6">
                    <ActivityIndicator size="large" color={AppColors.secondary} />
                    <Text className="text-white mt-6 text-center text-lg font-semibold">
                        Securing your time block...
                    </Text>
                    <Text className="text-text-secondary mt-2 text-center">
                        Please wait while we reserve this port on the hardware.
                    </Text>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (lockStatus === 'failed') {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 items-center justify-center px-6">
                    <Ionicons name="alert-circle" size={64} color="#EF4444" />
                    <Text className="text-white mt-6 text-center text-lg font-semibold">
                        Booking Failed
                    </Text>
                    <Text className="text-text-secondary mt-2 text-center mb-8">
                        {errorMessage}
                    </Text>
                    <Button onPress={() => router.back()} variant="primary" fullWidth>
                        Go Back
                    </Button>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }}>
                <AppHeader title="Review Summary" showBack />

                {/* Countdown Timer Banner */}
                {lockStatus === 'success' && (
                    <View className="bg-[#4CAF50]/10 border-y border-[#4CAF50]/30 py-3 px-4 flex-row justify-between items-center z-10">
                        <View className="flex-row items-center">
                            <Ionicons name="time-outline" size={20} color="#4CAF50" />
                            <Text className="text-white ml-2 text-sm">Please pay to secure your slot</Text>
                        </View>
                        <Text className="text-[#4CAF50] font-bold text-lg tracking-wider">
                            {timerDisplay}
                        </Text>
                    </View>
                )}

                <ScrollView
                    style={{ flex: 1 }}
                    className="px-4"
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ paddingBottom: 0 }}
                    keyboardShouldPersistTaps="handled"
                >
                    {/* Vehicle Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Vehicle</Text>
                    <ListItemCard
                        icon={
                            <MaterialIcons name="electric-bike" size={22} color={AppColors.secondary} />
                        }
                        title={vehicle.brand}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Model: {vehicle.modelName}
                        </Text>
                        <Text className="mt-1 text-text-secondary text-sm">
                            Connectors: {selectedCharger.connectorType}
                        </Text>
                    </ListItemCard>

                    {/* Charging Station Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charging Station</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-station" size={22} color={AppColors.secondary} />
                        }
                        title={station.name}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <Text className="text-text-secondary text-sm">
                            Address: {station.address}
                        </Text>
                    </ListItemCard>

                    {/* Charger Section */}
                    <Text className="text-white font-semibold text-base mb-2 mt-4">Charger</Text>
                    <ListItemCard
                        icon={
                            <MaterialCommunityIcons name="ev-plug-type2" size={30} color={AppColors.secondary} />
                        }
                        title={`${selectedCharger.name} - Port ${selectedPort.portNumber}`}
                        titleClassName="text-secondary font-semibold text-base"
                    >
                        <View className="flex-row items-center flex-wrap gap-x-2 mt-2">
                            <Text className="text-text-secondary text-sm">
                                {selectedCharger.connectorType}
                            </Text>
                            <View className="w-[1px] h-8 bg-border-gray mx-1" />
                            <View>
                                <Text className="text-text-secondary text-xs">Max. power</Text>
                                <Text className="text-white font-semibold text-base">
                                    {selectedCharger.maxPower} kW
                                </Text>
                            </View>
                        </View>
                    </ListItemCard>

                    {/* Booking Details Section */}
                    <View className="bg-border/20 border border-border p-4 rounded-lg mt-4 mb-4">
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Booking Date</Text>
                            <Text className="text-white font-semibold text-base">{date}</Text>
                        </View>
                        <View className="flex-row justify-between mb-3">
                            <Text className="text-text-secondary text-base">Time of Arrival</Text>
                            <Text className="text-white font-semibold text-base">{time}</Text>
                        </View>
                        <View className="flex-row justify-between">
                            <Text className="text-text-secondary text-base">Charging Duration</Text>
                            <Text className="text-white font-semibold text-base">{durationLabel}</Text>
                        </View>
                    </View>

                    <View className="bg-border/20 border border-border p-4 rounded-lg mb-4">
                        <View className="flex-row justify-between mb-4">
                            <Text className="text-text-secondary text-base">Amount Estimation</Text>
                            <Text className="text-white font-semibold text-base">{priceEstimation.toLocaleString()} VND</Text>
                        </View>
                        <View className="flex-row justify-between pb-4 border-b border-border/50 mb-4">
                            <Text className="text-text-secondary text-base">Tax</Text>
                            <Text className="text-white font-semibold text-base">Free</Text>
                        </View>
                        <View className="flex-row justify-between items-center">
                            <Text className="text-text-secondary text-base">Total Amount</Text>
                            <Text className="text-white font-semibold text-base">{priceEstimation.toLocaleString()} VND</Text>
                        </View>
                    </View>

                    {/* Payment Method Section */}
                    <Text className="text-white font-semibold text-base mb-2">Selected Payment Method</Text>
                    <ListItemCard
                        icon={
                            <View>
                                <Image
                                    source={require("@/assets/images/zalopay.webp")}
                                    style={{ width: 44, height: 44, borderRadius: 8 }}
                                    resizeMode="contain"
                                />
                            </View>
                        }
                        title="ZaloPay App"
                        titleClassName="text-white font-semibold text-base"
                    />

                    {/* Alert Message */}
                    <View className="bg-[#051F1A] border border-[#00A452]/30 rounded-lg p-4 mt-2 flex-row items-start mb-[100px]">
                        <Ionicons
                            name="information-circle"
                            size={20}
                            color="#00A452"
                            style={{ marginTop: 2 }}
                        />
                        <Text className="flex-1 ml-3 text-secondary text-sm leading-5">
                            You will be redirected to the ZaloPay app to authorize. Your account will only be charged after the charging session is completed.
                        </Text>
                    </View>
                </ScrollView>

                {/* Bottom Fixed Action */}
                <View className="px-6 pt-6 pb-2">
                    <Button
                        onPress={handleConfirmBooking}
                        disabled={isProcessingBooking || lockStatus !== 'success'}
                        className="w-full"
                        textClassName="font-semibold text-base"
                        style={{ height: 56 }}
                        variant="primary"
                        size="lg"
                    >
                        {isProcessingBooking ? "Processing..." : "Confirm Booking"}
                    </Button>
                </View>
            </SafeAreaView>
        </GradientBackground>
    );
}
