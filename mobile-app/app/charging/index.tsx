import React from "react";
import { View, ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";

import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import Button from "@/components/ui/Button";
import ChargingIndicator from "@/components/charging/ChargingIndicator";
import StatsGrid from "@/components/charging/StatsGrid";
import { mockChargingSession } from "@/data/chargingData";

export default function ChargingPage() {
    const session = mockChargingSession;

    const handleStop = () => {
        console.log("Stop charging");
    };

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }}>
                <AppHeader
                    title="Charging"
                    showBack
                />

                <ScrollView
                    style={{ flex: 1 }}
                    className="px-4"
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ paddingBottom: 0 }}
                    keyboardShouldPersistTaps="handled"
                >
                    {/* Central Indicator */}
                    <ChargingIndicator
                        kwh={session.kwh}
                        percentage={session.percentage}
                    />

                    {/* Stats Grid */}
                    <StatsGrid
                        time={session.time}
                        percentage={session.percentage}
                        current={session.current}
                        fees={session.fees}
                    />
                </ScrollView>

                {/* Bottom Action Button */}
                <View className="px-6 pt-6 pb-2">
                    <Button
                        variant="primary"
                        onPress={handleStop}
                        className="w-full"
                        style={{ height: 56 }}
                        textClassName="font-semibold text-base"
                    >
                        Stop Charging
                    </Button>
                </View>
            </SafeAreaView>
        </GradientBackground>
    );
}
