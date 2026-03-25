import { getAvailableSlots, getCalendarStatus, getDurationsConfig } from "@/apis/bookingApi";
import BookingCalendar from "@/components/booking/BookingCalendar";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import Dropdown from "@/components/ui/Dropdown";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { router, useLocalSearchParams } from "expo-router";
import { useEffect, useState } from "react";
import { ActivityIndicator, ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { DayStatus } from "@/types/booking.types";

export default function SelectTimePage() {
    const { stationId, vehicleId, portId } = useLocalSearchParams<{
        stationId: string;
        vehicleId: string;
        portId: string;
    }>();

    const [selectedDate, setSelectedDate] = useState(new Date().toLocaleDateString('en-CA'));
    const [arrivalTime, setArrivalTime] = useState<string | undefined>(undefined);
    const [duration, setDuration] = useState<string | undefined>(undefined);

    const [durationsList, setDurationsList] = useState<{ label: string; value: string }[]>([]);
    const [calendarStatus, setCalendarStatus] = useState<Record<string, 'AVAILABLE' | 'FULL' | 'UNAVAILABLE'>>({});
    const [availableTimeSlots, setAvailableTimeSlots] = useState<{ label: string; value: string }[]>([]);

    const [isLoadingInit, setIsLoadingInit] = useState(true);
    const [isLoadingSlots, setIsLoadingSlots] = useState(false);
    const [errorMsg, setErrorMsg] = useState<string | null>(null);

    useEffect(() => {
        const initData = async () => {
            try {
                setIsLoadingInit(true);
                setErrorMsg(null);
                const today = new Date();
                const [durationRes, calendarRes] = await Promise.all([
                    getDurationsConfig(),
                    getCalendarStatus(Number(stationId), today.getFullYear(), today.getMonth() + 1)
                ]);
                
                const mappedDurations = durationRes.durations.map((d: number) => ({
                    label: d === 0.5 ? "30 Minutes" : `${d} Hour${d > 1 ? 's' : ''}`,
                    value: d.toString()
                }));
                setDurationsList(mappedDurations);
                if (mappedDurations.length > 0) setDuration(mappedDurations[1]?.value || mappedDurations[0].value);
                
                const statusMap: Record<string, 'AVAILABLE' | 'FULL' | 'UNAVAILABLE'> = {};
                calendarRes.forEach((item: any) => {
                    statusMap[item.date] = item.status;
                });
                setCalendarStatus(statusMap);
            } catch (err) {
                setErrorMsg('Failed to load initially. Please go back and try again.');
            } finally {
                setIsLoadingInit(false);
            }
        };
        initData();
    }, [stationId]);

    useEffect(() => {
        const fetchSlots = async () => {
            if (!selectedDate || !duration) return;
            try {
                setIsLoadingSlots(true);
                setArrivalTime(undefined);
                const slotsRes = await getAvailableSlots(Number(stationId), selectedDate, Number(duration));
                const mappedSlots = slotsRes.map(slot => ({
                    label: slot.startTime.substring(0, 5), // 'HH:mm'
                    value: slot.startTime
                }));
                setAvailableTimeSlots(mappedSlots);
            } catch (err) {
                setAvailableTimeSlots([]);
            } finally {
                setIsLoadingSlots(false);
            }
        };
        fetchSlots();
    }, [selectedDate, duration, stationId]);

    const handleContinue = () => {
        if (!arrivalTime || !duration) return;
        router.push(`/booking/confirm?stationId=${stationId}&vehicleId=${vehicleId}&portId=${portId}&date=${selectedDate}&time=${arrivalTime}&duration=${duration}`);
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1"
        >
            <SafeAreaView className="flex-1">
                <AppHeader title="Booking" />

                <ScrollView className="flex-1 px-6 mt-4 pb-10">
                    {isLoadingInit ? (
                        <View className="flex-1 justify-center items-center py-20">
                            <ActivityIndicator size="large" color="#00A452" />
                            <Text className="text-white mt-4">Loading availability...</Text>
                        </View>
                    ) : errorMsg ? (
                        <View className="bg-red-500/20 p-4 rounded-xl mt-4">
                            <Text className="text-red-500 text-center">{errorMsg}</Text>
                        </View>
                    ) : (
                        <>
                            <Text className="text-white font-semibold text-base mb-2">
                                Select Date
                            </Text>
                            <BookingCalendar
                                selectedDate={selectedDate}
                                onDateSelect={setSelectedDate}
                                statusMap={calendarStatus}
                            />

                            <View className="mt-6">
                                <Dropdown
                                    label="Charging Duration"
                                    items={durationsList}
                                    value={duration}
                                    onValueChange={setDuration}
                                />
                            </View>

                            <View className="mt-8">
                                <Text className="text-white font-semibold text-base mb-2">
                                    Select Time {isLoadingSlots && <ActivityIndicator size="small" color="#00A452" style={{ marginLeft: 8 }} />}
                                </Text>
                                {availableTimeSlots.length > 0 ? (
                                    <Dropdown
                                        label="Arrival Time"
                                        items={availableTimeSlots}
                                        value={arrivalTime}
                                        onValueChange={setArrivalTime}
                                    />
                                ) : (
                                    <View className="bg-surface-dark border border-[#33404F] p-8 rounded-2xl items-center justify-center">
                                        {isLoadingSlots ? (
                                            <>
                                                <ActivityIndicator size="large" color="#00A452" />
                                                <Text className="text-white font-medium mt-4">Checking slots...</Text>
                                            </>
                                        ) : (
                                            <>
                                                <View className="w-16 h-16 rounded-full bg-[#1A2530] items-center justify-center mb-4">
                                                    <Ionicons name="time-outline" size={32} color="#64748B" />
                                                </View>
                                                <Text className="text-white font-semibold text-lg text-center">
                                                    No Times Available
                                                </Text>
                                                <Text className="text-gray-400 text-sm mt-2 text-center leading-5">
                                                    All charging slots are fully booked for this date and duration. Please select another date or reduce the charging duration.
                                                </Text>
                                            </>
                                        )}
                                    </View>
                                )}
                            </View>

                    {/* Info Alert Box */}
                    <View className="bg-[#051F1A] border border-[#00A452]/30 rounded-2xl p-4 mt-8 flex-row items-start">
                        <Ionicons
                            name="information-circle"
                            size={20}
                            color="#00A452"
                            style={{ flexShrink: 0, marginTop: 2 }}
                        />
                        <Text className="flex-1 ml-3 text-secondary text-sm leading-5">
                            You can only book available times. Unavailable time
                            means someone else has booked it.
                        </Text>
                    </View>
                        </>
                    )}
                </ScrollView>

                {/* Continue Button */}
                <View className="px-6 pt-6 pb-2">
                    <Button
                        variant="primary"
                        size="lg"
                        fullWidth
                        disabled={!arrivalTime || isLoadingSlots || isLoadingInit}
                        onPress={handleContinue}
                    >
                        Continue
                    </Button>
                </View>
            </SafeAreaView>
        </LinearGradient>
    );
}
