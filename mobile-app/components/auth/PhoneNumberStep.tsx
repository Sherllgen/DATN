import SvgLogoGoogle from "@/assets/svg/SvgLogoGoogle";
import { Link } from "expo-router";
import React from "react";
import { Text, TextInput, TouchableOpacity, View } from "react-native";

interface PhoneNumberStepProps {
    phoneNumber: string;
    onPhoneNumberChange: (value: string) => void;
    onContinue: () => void;
}

export default function PhoneNumberStep({
    phoneNumber,
    onPhoneNumberChange,
    onContinue,
}: PhoneNumberStepProps) {
    return (
        <>
            {/* Title */}
            <Text className="mb-3 font-bold text-white text-4xl text-center">
                Sign Up
            </Text>
            <Text className="mb-12 text-gray-400 text-sm text-center leading-5">
                Create your account by entering your phone number. We'll send
                you a verification code.
            </Text>

            {/* Email Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Email
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="abc@example.com"
                    placeholderTextColor="#999"
                    value={phoneNumber}
                    onChangeText={onPhoneNumberChange}
                    autoCapitalize="none"
                    keyboardType="email-address"
                />
            </View>

            {/* Password Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Password
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="••••••••"
                    placeholderTextColor="#999"
                    autoCapitalize="none"
                />
            </View>

            {/* Password confirm Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Confirm Password
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="••••••••"
                    placeholderTextColor="#999"
                    autoCapitalize="none"
                />
            </View>

            {/* Continue Button */}
            <TouchableOpacity
                className="items-center bg-secondary mt-10 mb-6 ml-auto py-4 rounded-full w-full"
                onPress={onContinue}
                activeOpacity={0.8}
                disabled={phoneNumber.length !== 10}
                style={{
                    opacity: phoneNumber.length !== 10 ? 0.5 : 1,
                }}
            >
                <Text className="font-semibold text-white text-base">
                    Continue
                </Text>
            </TouchableOpacity>

            {/* Divider */}
            <View className="flex-row items-center mb-6">
                <View className="flex-1 bg-gray-100/30 h-px" />
                <Text className="mx-4 text-gray-400 text-sm">or</Text>
                <View className="flex-1 bg-gray-100/30 h-px" />
            </View>

            {/* Google Sign Up */}
            <TouchableOpacity
                className="bg-white mb-4 py-3 border rounded-full"
                onPress={onContinue}
                activeOpacity={0.8}
            >
                <View className="flex-row justify-evenly items-center">
                    <SvgLogoGoogle />
                    <Text className="text-black text-base">
                        Sign up with Google
                    </Text>
                </View>
            </TouchableOpacity>

            {/* Sign In Link */}
            <View className="flex-row justify-center mt-6">
                <Text className="text-gray-400 text-sm">
                    Already have an account?{" "}
                </Text>
                <Link href="/auth/login" asChild>
                    <TouchableOpacity activeOpacity={0.7}>
                        <Text className="font-semibold text-white text-sm">
                            Sign In
                        </Text>
                    </TouchableOpacity>
                </Link>
            </View>
        </>
    );
}
