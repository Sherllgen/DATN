import { forgotPasswordApi, resetPasswordApi } from "@/apis/authApi/authApi";
import AppHeader from "@/components/ui/AppHeader";
import { useAuthStore } from "@/contexts/auth.store";
import { useUserStore } from "@/contexts/user.store";
import { logAxiosError } from "@/utils/errorLogger";
import { validatePassword } from "@/utils/validators";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useEffect, useRef, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    ScrollView,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function ForgotPasswordScreen() {
    const [token, setToken] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [sendingToken, setSendingToken] = useState(true);
    const [error, setError] = useState("");

    const router = useRouter();
    const user = useUserStore((state) => state.user);
    const logout = useAuthStore((state) => state.logout);

    const tokenInputRef = useRef<TextInput>(null);
    const newPWInputRef = useRef<TextInput>(null);
    const confirmPWInputRef = useRef<TextInput>(null);

    const handleResendToken = async () => {
        setError("");

        if (!user?.email) {
            setError("Unable to retrieve your email");
            return;
        }

        try {
            setSendingToken(true);
            await forgotPasswordApi(user.email);

            Alert.alert(
                "Success",
                "Reset token has been sent to your email. Please check your inbox."
            );
        } catch (err: any) {
            const errorMessage =
                err?.response?.data?.message ||
                "Failed to send reset token. Please try again.";
            setError(errorMessage);
            logAxiosError(err);
        } finally {
            setSendingToken(false);
        }
    };

    const handleResetPassword = async () => {
        setError("");

        // Validation
        if (!token || !newPassword || !confirmPassword) {
            setError("Please fill in all fields");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("New password and confirm password do not match");
            return;
        }

        if (!validatePassword(newPassword)) {
            setError(
                "Password must be 8-16 characters and contain both numbers and letters/special characters"
            );
            return;
        }

        try {
            setLoading(true);
            await resetPasswordApi(token, newPassword, confirmPassword);

            Alert.alert(
                "Success",
                "Password has been reset successfully. Please login with your new password.",
                [
                    {
                        text: "OK",
                        onPress: () => {
                            logout();
                            router.replace("/auth/login");
                        },
                    },
                ]
            );
        } catch (err: any) {
            const errorMessage =
                err?.response?.data?.message ||
                "Failed to reset password. Please check your token and try again.";
            setError(errorMessage);
            logAxiosError(err);
        } finally {
            setLoading(false);
        }
    };

    // Auto send token when component mounts
    useEffect(() => {
        const sendToken = async () => {
            if (!user?.email) {
                Alert.alert(
                    "Error",
                    "Unable to retrieve your email. Please login again.",
                    [
                        {
                            text: "OK",
                            onPress: () => router.back(),
                        },
                    ]
                );
                return;
            }

            try {
                setSendingToken(true);
                await forgotPasswordApi(user.email);
            } catch (err: any) {
                const errorMessage =
                    err?.response?.data?.message ||
                    "Failed to send reset token. Please try again.";
                setError(errorMessage);
                logAxiosError(err);
            } finally {
                setSendingToken(false);
            }
        };

        sendToken();
    }, [user]);

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 pb-[90px]"
        >
            <SafeAreaView className="flex-1">
                {/* Header */}
                <AppHeader title="Forgot Password" />

                <ScrollView className="flex-1 mt-6 px-6">
                    {/* Sending Token Indicator */}
                    {sendingToken ? (
                        <View className="justify-center items-center py-10">
                            <ActivityIndicator size="large" color="#16a34a" />
                            <Text className="mt-4 text-gray-200 text-base">
                                Sending reset token to your email...
                            </Text>
                        </View>
                    ) : (
                        <View>
                            {/* Info Message */}
                            <View className="bg-green-600/20 mb-6 p-4 border border-green-600 rounded-lg">
                                <Text className="text-green-400 text-sm">
                                    Reset token has been sent to {user?.email}
                                </Text>
                            </View>

                            {/* Error Message */}
                            {error ? (
                                <View className="bg-red-500/20 mb-4 p-3 border border-red-500 rounded-lg">
                                    <Text className="text-red-400 text-sm">
                                        {error}
                                    </Text>
                                </View>
                            ) : null}

                            <View>
                                <Text className="mb-6 text-gray-200 text-base leading-6">
                                    Check your email for the reset token, then
                                    enter it below along with your new password.
                                </Text>

                                {/* Token Input */}
                                <View className="mb-6">
                                    <Text className="mb-2 text-gray-200 text-base">
                                        Reset Token
                                    </Text>
                                    <TextInput
                                        ref={tokenInputRef}
                                        value={token}
                                        onChangeText={setToken}
                                        placeholder="Enter reset token from email"
                                        placeholderTextColor="#4B5563"
                                        autoCapitalize="none"
                                        className="bg-gray-800/30 p-3 border border-gray-700 rounded-lg text-white"
                                        returnKeyType="next"
                                        onSubmitEditing={() =>
                                            newPWInputRef.current?.focus()
                                        }
                                    />
                                </View>

                                {/* New Password Input */}
                                <View className="mb-6">
                                    <Text className="mb-2 text-gray-200 text-base">
                                        New Password
                                    </Text>
                                    <TextInput
                                        ref={newPWInputRef}
                                        value={newPassword}
                                        onChangeText={setNewPassword}
                                        placeholder="Enter new password"
                                        placeholderTextColor="#4B5563"
                                        secureTextEntry
                                        className="bg-gray-800/30 p-3 border border-gray-700 rounded-lg text-white"
                                        returnKeyType="next"
                                        onSubmitEditing={() =>
                                            confirmPWInputRef.current?.focus()
                                        }
                                    />
                                </View>

                                {/* Confirm Password Input */}
                                <View className="mb-6">
                                    <Text className="mb-2 text-gray-200 text-base">
                                        Confirm Password
                                    </Text>
                                    <TextInput
                                        ref={confirmPWInputRef}
                                        value={confirmPassword}
                                        onChangeText={setConfirmPassword}
                                        placeholder="Confirm new password"
                                        placeholderTextColor="#4B5563"
                                        secureTextEntry
                                        className="bg-gray-800/30 p-3 border border-gray-700 rounded-lg text-white"
                                        returnKeyType="done"
                                        onSubmitEditing={handleResetPassword}
                                    />
                                </View>

                                {/* Password Requirements */}
                                <Text className="mb-6 text-gray-400 text-sm leading-5">
                                    Password must be 8-16 characters and contain
                                    both numbers and letters/special characters.
                                </Text>

                                {/* Reset Password Button */}
                                <TouchableOpacity
                                    onPress={handleResetPassword}
                                    disabled={loading}
                                    className={`bg-green-600 p-4 rounded-lg items-center mb-4 ${
                                        loading ? "opacity-50" : ""
                                    }`}
                                >
                                    {loading ? (
                                        <ActivityIndicator
                                            size="small"
                                            color="#fff"
                                        />
                                    ) : (
                                        <Text className="font-semibold text-white text-base">
                                            Reset Password
                                        </Text>
                                    )}
                                </TouchableOpacity>

                                {/* Resend Token */}
                                <TouchableOpacity
                                    onPress={handleResendToken}
                                    disabled={sendingToken}
                                    className="items-center"
                                >
                                    <Text className="text-gray-400 text-sm">
                                        Didn't receive the token?{" "}
                                        <Text className="text-green-600 underline">
                                            Send again
                                        </Text>
                                    </Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    )}
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
