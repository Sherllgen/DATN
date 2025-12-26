import SvgLogoGoogle from "@/assets/svg/SvgLogoGoogle";
import { Ionicons } from "@expo/vector-icons";
import { Link } from "expo-router";
import React, { useRef, useState } from "react";
import { Text, TextInput, TouchableOpacity, View } from "react-native";

interface PhoneNumberStepProps {
    registerData: {
        fullName: string;
        email: string;
        password: string;
        confirmPassword: string;
    };
    onRegisterDataChange: (
        value: React.SetStateAction<{
            fullName: string;
            email: string;
            password: string;
            confirmPassword: string;
        }>
    ) => void;
    onContinue: () => void;
}

export default function PhoneNumberStep({
    registerData,
    onRegisterDataChange,
    onContinue,
}: PhoneNumberStepProps) {
    const [showPassword, setShowPassword] = useState(false);

    const emailInputRef = useRef<TextInput>(null);
    const passwordInputRef = useRef<TextInput>(null);
    const confirmPasswordInputRef = useRef<TextInput>(null);

    return (
        <>
            {/* Title */}
            <Text className="mb-12 font-bold text-white text-4xl text-center">
                Sign Up
            </Text>

            {/* Fullname Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Full name
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="Nguyen Van A"
                    placeholderTextColor="#999"
                    value={registerData.fullName}
                    onChangeText={(value) =>
                        onRegisterDataChange((prev) => ({
                            ...prev,
                            fullName: value,
                        }))
                    }
                    autoCapitalize="none"
                    returnKeyType="next"
                    onSubmitEditing={() => emailInputRef.current?.focus()}
                />
            </View>

            {/* Email Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Email
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    ref={emailInputRef}
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="abc@example.com"
                    placeholderTextColor="#999"
                    value={registerData.email}
                    onChangeText={(value) =>
                        onRegisterDataChange((prev) => ({
                            ...prev,
                            email: value,
                        }))
                    }
                    autoCapitalize="none"
                    keyboardType="email-address"
                    returnKeyType="next"
                    onSubmitEditing={() => passwordInputRef.current?.focus()}
                />
            </View>

            {/* Password Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Password
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    ref={passwordInputRef}
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="••••••••"
                    placeholderTextColor="#999"
                    value={registerData.password}
                    onChangeText={(value) =>
                        onRegisterDataChange((prev) => ({
                            ...prev,
                            password: value,
                        }))
                    }
                    secureTextEntry={!showPassword}
                    autoCapitalize="none"
                    returnKeyType="next"
                    onSubmitEditing={() =>
                        confirmPasswordInputRef.current?.focus()
                    }
                />
                <TouchableOpacity
                    onPress={() => setShowPassword(!showPassword)}
                    className="mr-3"
                    activeOpacity={0.7}
                >
                    <Ionicons
                        name={showPassword ? "eye-off" : "eye"}
                        size={20}
                        color="#999"
                    />
                </TouchableOpacity>
            </View>

            {/* Password confirm Input */}
            <Text className="mb-2 ml-4 font-medium text-white text-sm">
                Confirm Password
            </Text>
            <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                <TextInput
                    ref={confirmPasswordInputRef}
                    className="flex-1 ml-3 text-white text-base"
                    placeholder="••••••••"
                    placeholderTextColor="#999"
                    value={registerData.confirmPassword}
                    onChangeText={(value) =>
                        onRegisterDataChange((prev) => ({
                            ...prev,
                            confirmPassword: value,
                        }))
                    }
                    autoCapitalize="none"
                    returnKeyType="done"
                    secureTextEntry={true}
                    onSubmitEditing={onContinue}
                />
            </View>

            {/* Continue Button */}
            <TouchableOpacity
                className="items-center bg-secondary mt-10 mb-6 ml-auto py-4 rounded-full w-full"
                onPress={onContinue}
                activeOpacity={0.8}
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
