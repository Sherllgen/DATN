import { changePasswordApi } from "@/apis/authApi/authApi";
import AppHeader from "@/components/ui/AppHeader";
import { useAuthStore } from "@/contexts/auth.store";
import { logAxiosError } from "@/utils/errorLogger";
import { validatePassword } from "@/utils/validators";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useRef, useState } from "react";
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

export default function PasswordScreen() {
    const [oldPassword, setOldPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const router = useRouter();
    const logout = useAuthStore((state) => state.logout);

    const oldPWInputRef = useRef<TextInput>(null);
    const newPWInputRef = useRef<TextInput>(null);
    const confirmPWInputRef = useRef<TextInput>(null);

    const handleDone = async () => {
        setError("");

        // Validation
        if (!oldPassword || !newPassword || !confirmPassword) {
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

        if (oldPassword === newPassword) {
            setError("New password must be different from old password");
            return;
        }

        try {
            setLoading(true);
            await changePasswordApi(oldPassword, newPassword, confirmPassword);

            Alert.alert("Success", "Password changed successfully", [
                {
                    text: "OK",
                    onPress: () => {
                        logout();
                        router.replace("/auth/login");
                    },
                },
            ]);
        } catch (err: any) {
            const errorMessage =
                err?.response?.data?.message ||
                "Failed to change password. Please try again.";
            setError(errorMessage);
            logAxiosError(err);
        } finally {
            setLoading(false);
        }
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1"
        >
            <SafeAreaView className="flex-1">
                {/* Header */}
                <AppHeader title="Change Password" />

                <View className="items-end -mt-14 mb-6 pr-6">
                    <TouchableOpacity
                        onPress={handleDone}
                        disabled={loading}
                        className={loading ? "opacity-50" : ""}
                    >
                        <View className="items-center bg-green-600 px-4 py-2 rounded-lg w-20">
                            {loading ? (
                                <ActivityIndicator size="small" color="#fff" />
                            ) : (
                                <Text className="font-medium text-white text-base text-center">
                                    Done
                                </Text>
                            )}
                        </View>
                    </TouchableOpacity>
                </View>

                <ScrollView className="flex-1 px-6">
                    {/* Description */}
                    <Text className="mb-8 text-gray-500 text-sm leading-6">
                        Set EVGo password, so that you can rapidly log in to
                        EVGo with the account linked with EVGo + EVGo password
                        (such as your email + EVGo password).
                    </Text>

                    {/* Error Message */}
                    {error ? (
                        <View className="bg-red-500/20 mb-4 p-3 border border-red-500 rounded-lg">
                            <Text className="text-red-400 text-sm">
                                {error}
                            </Text>
                        </View>
                    ) : null}

                    {/* Old Password */}
                    <View className="flex-row justify-start items-center gap-4 mb-6">
                        <Text className="w-44 text-gray-200 text-base">
                            Old password
                        </Text>
                        <TextInput
                            ref={oldPWInputRef}
                            value={oldPassword}
                            onChangeText={setOldPassword}
                            placeholder="Enter old password"
                            placeholderTextColor="#4B5563"
                            secureTextEntry
                            className="mr-6 pb-2 border-gray-700 border-b w-full text-white"
                            returnKeyType="next"
                            onSubmitEditing={() =>
                                newPWInputRef.current?.focus()
                            }
                        />
                    </View>

                    {/* New Password */}
                    <View className="flex-row justify-start items-center gap-4 mb-6">
                        <Text className="w-44 text-gray-200 text-base">
                            New password
                        </Text>
                        <TextInput
                            ref={newPWInputRef}
                            value={newPassword}
                            onChangeText={setNewPassword}
                            placeholder="Enter new password"
                            placeholderTextColor="#4B5563"
                            secureTextEntry
                            className="mr-6 pb-2 border-gray-700 border-b w-full text-white"
                            returnKeyType="next"
                            onSubmitEditing={() =>
                                confirmPWInputRef.current?.focus()
                            }
                        />
                    </View>
                    {/* Confirm Password */}
                    <View className="flex-row justify-start items-center gap-4 mb-6">
                        <Text className="w-44 text-gray-200 text-base">
                            Confirm password
                        </Text>
                        <TextInput
                            ref={confirmPWInputRef}
                            value={confirmPassword}
                            onChangeText={setConfirmPassword}
                            placeholder="Confirm password"
                            placeholderTextColor="#4B5563"
                            secureTextEntry
                            className="mr-6 pb-2 border-gray-700 border-b w-full text-white"
                            returnKeyType="done"
                            onSubmitEditing={handleDone}
                        />
                    </View>

                    {/* Password Requirements */}
                    <Text className="mb-2 text-gray-400 text-sm leading-5">
                        Password must be 8-16 characters and contain both
                        numbers and letters/special characters.
                    </Text>

                    {/* Forgot Password Link */}
                    <TouchableOpacity
                        onPress={() =>
                            router.push(
                                "/(tabs)/setting/security/forgotPassword"
                            )
                        }
                    >
                        <Text className="text-green-700 text-sm">
                            Forgot old password?
                        </Text>
                    </TouchableOpacity>
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
