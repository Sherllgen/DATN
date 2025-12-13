import OTPVerificationStep from "@/components/auth/OTPVerificationStep";
import PhoneNumberStep from "@/components/auth/PhoneNumberStep";
import { LinearGradient } from "expo-linear-gradient";
import React, { useState } from "react";
import { ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function RegisterScreen() {
    const [step, setStep] = useState<"phone" | "otp">("phone");
    const [phoneNumber, setPhoneNumber] = useState("");
    const [otp, setOtp] = useState(["", "", "", "", "", ""]);

    const handleSendOTP = () => {
        if (phoneNumber.length === 10) {
            // Logic to send OTP
            console.log("Sending OTP to:", phoneNumber);
            setStep("otp");
        }
    };

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
    };

    const handleResendOTP = () => {
        console.log("Resending OTP to:", phoneNumber);
        // Logic to resend OTP
        setOtp(["", "", "", "", "", ""]);
    };

    const handleBackToPhone = () => {
        setStep("phone");
        setOtp(["", "", "", "", "", ""]);
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
                >
                    {step === "phone" ? (
                        <PhoneNumberStep
                            phoneNumber={phoneNumber}
                            onPhoneNumberChange={setPhoneNumber}
                            onContinue={handleSendOTP}
                        />
                    ) : (
                        <OTPVerificationStep
                            phoneNumber={phoneNumber}
                            otp={otp}
                            onOTPChange={handleOTPChange}
                            onOTPKeyPress={handleOTPKeyPress}
                            onVerify={handleVerifyOTP}
                            onResend={handleResendOTP}
                            onBack={handleBackToPhone}
                        />
                    )}
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
