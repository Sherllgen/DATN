import PhoneNumberStep from "@/components/auth/PhoneNumberStep";
import { LinearGradient } from "expo-linear-gradient";
import { router } from "expo-router";
import React, { useState } from "react";
import { ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function RegisterScreen() {
    const [phoneNumber, setPhoneNumber] = useState("");

    const handleSendOTP = () => {
        if (phoneNumber.length === 10) {
            // Logic to send OTP
            console.log("Sending OTP to:", phoneNumber);
            // Navigate to OTP verification screen
            router.push({
                pathname: "/auth/otp-verify",
                params: { phoneNumber },
            });
        }
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-12"
        >
            <SafeAreaView>
                <ScrollView
                    contentContainerClassName="flex-grow"
                    className="pt-20"
                    showsVerticalScrollIndicator={false}
                >
                    <PhoneNumberStep
                        phoneNumber={phoneNumber}
                        onPhoneNumberChange={setPhoneNumber}
                        onContinue={handleSendOTP}
                    />
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
