import { resendEmailOtpApi, verifyEmailOtpApi } from "@/apis/authApi/authApi";
import OTPVerificationStep from "@/components/auth/OTPVerificationStep";
import { logAxiosError } from "@/utils/errorLogger";
import { LinearGradient } from "expo-linear-gradient";
import { router, useLocalSearchParams } from "expo-router";
import React, { useEffect, useState } from "react";
import { ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Toast } from "toastify-react-native";

export default function OTPVerifyScreen() {
    const { email, isResent } = useLocalSearchParams<{
        email: string;
        isResent: string;
    }>();
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

    const handleVerifyOTP = async () => {
        const otpCode = otp.join("");
        console.log("Verifying OTP:", otpCode);
        try {
            const res = await verifyEmailOtpApi(otpCode, email);
            if (res.status === 200 || res.status === 201) {
                router.replace("/auth/login");
            }
        } catch (error) {
            logAxiosError(error);
        }
    };

    const handleResendOTP = async () => {
        try {
            const res = await resendEmailOtpApi(email);

            if (res.status === 200 || res.status === 201) {
                Toast.success(`OTP has been sent to ${email}`);
            }
        } catch (error) {
            logAxiosError(error);
            Toast.error("Failed to resend OTP. Please try again.");
        } finally {
            setOtp(["", "", "", "", "", ""]);
        }
    };

    const handleBackToPhone = () => {
        router.back();
    };

    useEffect(() => {
        if (isResent === "true") {
            handleResendOTP();
        }
    }, [isResent]);

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
                        email={email || ""}
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
