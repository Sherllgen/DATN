import BookingCalendar from "@/components/booking/BookingCalendar";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import Dropdown from "@/components/ui/Dropdown";
import { arrivalTimes, bookingDurations } from "@/data/booking";
import { Ionicons } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { router, useLocalSearchParams } from "expo-router";
import { useState } from "react";
import { ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SelectTimePage() {
    const { stationId, vehicleId, portId } = useLocalSearchParams<{
        stationId: string;
        vehicleId: string;
        portId: string;
    }>();

    const [selectedDate, setSelectedDate] = useState("2024-12-17");
    const [arrivalTime, setArrivalTime] = useState(arrivalTimes[4]); // 10:00
    const [duration, setDuration] = useState(bookingDurations[1].value); // 1 Hour

    const arrivalTimeItems = arrivalTimes.map((time) => ({
        label: time,
        value: time,
    }));

    const durationItems = bookingDurations.map((d) => ({
        label: d.label,
        value: d.value,
    }));

    const handleContinue = () => {
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
                    <Text className="text-white font-semibold text-base mb-2">
                        Select Date
                    </Text>
                    <BookingCalendar
                        selectedDate={selectedDate}
                        onDateSelect={setSelectedDate}
                    />

                    <View className="mt-8">
                        <Text className="text-white font-semibold text-base mb-2">
                            Select Time
                        </Text>
                        <Dropdown
                            label="Arrival Time"
                            items={arrivalTimeItems}
                            value={arrivalTime}
                            onValueChange={setArrivalTime}
                        />
                    </View>

                    <View className="mt-6">
                        <Dropdown
                            label="Charging Duration"
                            items={durationItems}
                            value={duration}
                            onValueChange={setDuration}
                        />
                    </View>

                    {/* Info Alert Box */}
                    <View className="bg-[#051F1A] border border-[#00A452]/30 rounded-2xl p-4 mt-8 flex-row items-start">
                        <Ionicons
                            name="information-circle"
                            size={20}
                            color="#00A452"
                            style={{ marginTop: 2 }}
                        />
                        <Text className="flex-1 ml-3 text-secondary text-sm leading-5">
                            You can only book available times. Unavailable time
                            means someone else has booked it.
                        </Text>
                    </View>
                </ScrollView>

                {/* Continue Button */}
                <View className="px-6 pt-6 pb-2">
                    <Button
                        variant="primary"
                        size="lg"
                        fullWidth
                        onPress={handleContinue}
                    >
                        Continue
                    </Button>
                </View>
            </SafeAreaView>
        </LinearGradient>
    );
}
