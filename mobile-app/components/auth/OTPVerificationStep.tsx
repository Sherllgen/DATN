import { Ionicons } from "@expo/vector-icons";
import React, { useRef, useState } from "react";
import { Text, TextInput, TouchableOpacity, View } from "react-native";

interface OTPVerificationStepProps {
    email: string;
    otp: string[];
    onOTPChange: (index: number, value: string) => void;
    onOTPKeyPress: (index: number, key: string) => number | null;
    onVerify: () => void;
    onResend: () => void;
    onBack: () => void;
}

export default function OTPVerificationStep({
    email,
    otp,
    onOTPChange,
    onOTPKeyPress,
    onVerify,
    onResend,
    onBack,
}: OTPVerificationStepProps) {
    const otpInputs = useRef<(TextInput | null)[]>([]);
    const [focusedIndex, setFocusedIndex] = useState<number | null>(null);

    const handleOTPChange = (index: number, value: string) => {
        onOTPChange(index, value);
        if (value && index < 5) {
            otpInputs.current[index + 1]?.focus();
        }
    };

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
                Please enter the verification code we sent to {email}
            </Text>

            {/* OTP Input */}
            <View className="flex-row justify-center gap-3 mb-8">
                {otp.map((digit, index) => (
                    <View
                        key={index}
                        className={`rounded-lg w-12 h-14 ${
                            focusedIndex === index
                                ? "bg-white border-2 border-secondary"
                                : "bg-gray-100"
                        }`}
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
                            onFocus={() => setFocusedIndex(index)}
                            onBlur={() => setFocusedIndex(null)}
                            keyboardType="numeric"
                            maxLength={1}
                            selectTextOnFocus
                            caretHidden={true}
                        />
                    </View>
                ))}
            </View>

            {/* Resend OTP */}
            <View className="flex-row justify-start mb-6">
                <Text className="text-gray-400 text-sm">
                    Didn't receive code?{" "}
                </Text>
                <TouchableOpacity onPress={onResend} activeOpacity={0.7}>
                    <Text className="font-semibold text-green-600 text-sm">
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
