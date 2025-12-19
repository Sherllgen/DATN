import { Ionicons } from "@expo/vector-icons";
import React, { useEffect, useRef, useState } from "react";
import { Text, TextInput, TouchableOpacity, View } from "react-native";

interface OTPVerificationStepProps {
    phoneNumber: string;
    otp: string[];
    onOTPChange: (index: number, value: string) => void;
    onOTPKeyPress: (index: number, key: string) => number | null;
    onVerify: () => void;
    onResend: () => void;
    onBack: () => void;
}

export default function OTPVerificationStep({
    phoneNumber,
    otp,
    onOTPChange,
    onOTPKeyPress,
    onVerify,
    onResend,
    onBack,
}: OTPVerificationStepProps) {
    const otpInputs = useRef<(TextInput | null)[]>([]);
    const [timeRemaining, setTimeRemaining] = useState<number>(60);

    const handleOTPChange = (index: number, value: string) => {
        onOTPChange(index, value);
        if (value && index < 5) {
            otpInputs.current[index + 1]?.focus();
        }
    };

    const handleResend = () => {
        setTimeRemaining(60);
        onResend();
    };

    const formatTime = (seconds: number) => {
        const mins = Math.floor(seconds / 60);
        const secs = seconds % 60;
        return `${mins}:${secs.toString().padStart(2, "0")}`;
    };

    useEffect(() => {
        if (timeRemaining === 0) return;

        const timer = setTimeout(() => {
            setTimeRemaining((prev) => prev - 1);
        }, 1000);

        return () => clearTimeout(timer);
    }, [timeRemaining]);

    return (
        <>
            {/* Back Button */}
            <TouchableOpacity
                onPress={onBack}
                className="-mt-12 mb-16 p-3 pl-0 w-12"
            >
                <Ionicons name="arrow-back" size={24} color="#fff" />
            </TouchableOpacity>

            {/* Title */}
            <Text className="mb-3 font-bold text-white text-4xl text-center">
                Verify Code
            </Text>
            <Text className="mb-10 text-gray-400 text-sm text-center leading-5">
                Please enter the verification code we sent to {phoneNumber}
            </Text>

            {/* OTP Input */}
            <View className="flex-row justify-center gap-3 mb-8">
                {otp.map((digit, index) => (
                    <View
                        key={index}
                        className="bg-gray-100 rounded-lg w-12 h-14"
                    >
                        <TextInput
                            ref={(ref) => {
                                otpInputs.current[index] = ref;
                            }}
                            className="flex-1 text-black text-xl text-center"
                            value={digit}
                            onChangeText={(value) =>
                                handleOTPChange(index, value)
                            }
                            onKeyPress={({ nativeEvent }) => {
                                const preIndex = onOTPKeyPress(
                                    index,
                                    nativeEvent.key
                                );

                                if (preIndex !== null) {
                                    // Use setTimeout to ensure state update completes before focusing
                                    setTimeout(() => {
                                        otpInputs.current[preIndex]?.focus();
                                    }, 0);
                                }
                            }}
                            keyboardType="numeric"
                            maxLength={1}
                            selectTextOnFocus
                            caretHidden={true}
                        />
                    </View>
                ))}
            </View>

            {/* Countdown Timer */}
            <View className="mb-4">
                <Text className="text-gray-400 text-sm text-start">
                    {timeRemaining > 0 ? (
                        <>
                            Code will expire in{" "}
                            <Text className="font-semibold text-yellow-500">
                                {formatTime(timeRemaining)}
                            </Text>
                        </>
                    ) : (
                        <Text className="text-red-500">Mã đã hết hạn</Text>
                    )}
                </Text>
            </View>

            {/* Resend OTP */}
            <View className="flex-row justify-start mb-6">
                <Text className="text-gray-400 text-sm">
                    Didn't receive code?{" "}
                </Text>
                <TouchableOpacity onPress={handleResend} activeOpacity={0.7}>
                    <Text className="font-semibold text-green-700 text-sm">
                        Resend
                    </Text>
                </TouchableOpacity>
            </View>

            {/* Verify Button */}
            <TouchableOpacity
                className="items-center bg-secondary mb-6 py-4 rounded-full"
                onPress={onVerify}
                activeOpacity={0.8}
                disabled={otp.some((digit) => !digit)}
                style={{
                    opacity: otp.some((digit) => !digit) ? 0.5 : 1,
                }}
            >
                <Text className="font-semibold text-white text-base">
                    Verify & Continue
                </Text>
            </TouchableOpacity>
        </>
    );
}
