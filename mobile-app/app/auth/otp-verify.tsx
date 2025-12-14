import OTPVerificationStep from "@/components/auth/OTPVerificationStep";
import { LinearGradient } from "expo-linear-gradient";
import { router, useLocalSearchParams } from "expo-router";
import React, { useState } from "react";
import { ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function OTPVerifyScreen() {
    const { phoneNumber } = useLocalSearchParams<{ phoneNumber: string }>();
    const [otp, setOtp] = useState(["", "", "", "", "", ""]);

    const handleOTPChange = (index: number, value: string) => {
        if (value.length > 1) {
            value = value.charAt(0);
        }

        const newOtp = [...otp];
        newOtp[index] = value;
        setOtp(newOtp);
    };

    const handleOTPKeyPress = (index: number, key: string) => {
        if (key === "Backspace" && !otp[index] && index > 0) {
            const newOtp = [...otp];
            newOtp[index - 1] = "";
            setOtp(newOtp);
            return index - 1; // return index to focus previous input
        }
        return null;
    };

    const handleVerifyOTP = () => {
        const otpCode = otp.join("");
        console.log("Verifying OTP:", otpCode);
        // Logic to verify OTP
        // After successful verification, navigate to next screen
        // router.replace("/(tabs)/home");
    };

    const handleResendOTP = () => {
        console.log("Resending OTP to:", phoneNumber);
        // Logic to resend OTP
        setOtp(["", "", "", "", "", ""]);
    };

    const handleBackToPhone = () => {
        router.back();
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
                    <OTPVerificationStep
                        phoneNumber={phoneNumber || ""}
                        otp={otp}
                        onOTPChange={handleOTPChange}
                        onOTPKeyPress={handleOTPKeyPress}
                        onVerify={handleVerifyOTP}
                        onResend={handleResendOTP}
                        onBack={handleBackToPhone}
                    />
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
