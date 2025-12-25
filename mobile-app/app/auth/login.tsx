import { loginApi } from "@/apis/authApi/authApi";
import SvgLogoGoogle from "@/assets/svg/SvgLogoGoogle";
import { useAuthStore } from "@/contexts/auth.store";
import { useUserStore } from "@/contexts/user.store";
import { logAxiosError } from "@/utils/errorLogger";
import { isValidEmail } from "@/utils/validators";
import { FontAwesome5 } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import { Link, useRouter } from "expo-router";
import React, { useRef, useState } from "react";
import {
    ActivityIndicator,
    ScrollView,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function LoginScreen() {
    const [username, setUsername] = useState<string>("");
    const [password, setPassword] = useState<string>("");
    const [showPassword, setShowPassword] = useState<boolean>(false);
    const [showError, setShowError] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);

    const passwordInputRef = useRef<TextInput>(null);

    const router = useRouter();

    const setUser = useUserStore((s) => s.setUser);
    const setAccessToken = useAuthStore((s) => s.setAccessToken);

    const handleSignIn = async () => {
        const isUserNameValid = isValidEmail(username);

        if (!isUserNameValid) {
            setShowError("Email không hợp lệ. Vui lòng thử lại.");
            return;
        }

        setLoading(true);
        setShowError("");

        try {
            const res = await loginApi(username, password);
            if (res.status === 200) {
                setAccessToken(res.data.accessToken);
                setUser(res.data.user);
                router.replace("/(tabs)/home");
            }
        } catch (error: any) {
            logAxiosError(error);

            if (error.status === 409) {
                setShowError("Email đã được sử dụng. Vui lòng thử lại.");
                return;
            }

            if (error.status === 401) {
                setShowError(
                    "Email hoặc mật khẩu không đúng. Vui lòng thử lại."
                );
                return;
            }

            // Xử lý lỗi tài khoản chưa xác thực - chuyển đến màn hình verify
            if (
                error.status === 403 &&
                (error.response?.data?.status === 1006 ||
                    error.response?.data?.message === "Account not verified")
            ) {
                router.push({
                    pathname: "/auth/otp-verify",
                    params: { email: username },
                });
                return;
            }

            setShowError("Đã có lỗi xảy ra. Vui lòng thử lại.");
        } finally {
            setLoading(false);
        }
    };

    const handleGoogleSignIn = () => {
        // Handle Google sign in
        console.log("Sign in with Google");
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-12 pt-20"
        >
            <SafeAreaView>
                <ScrollView contentContainerClassName="flex-grow  ">
                    {/* Title */}
                    <Text className="mb-3 font-bold text-white text-4xl text-center">
                        Sign In
                    </Text>
                    <Text className="mb-12 text-gray-400 text-sm text-center leading-5">
                        Welcome back! Please enter your details to sign in to
                        your account.
                    </Text>

                    {/* Username Input */}
                    <Text className="mb-2 font-medium text-white text-sm">
                        Email
                    </Text>
                    <View className="flex-row items-center bg-primary/90 mb-6 px-2 py-[2px] rounded-full">
                        <TextInput
                            className="flex-1 ml-3 text-white text-base"
                            placeholder="abc@example.com"
                            placeholderTextColor="#999"
                            value={username}
                            onChangeText={setUsername}
                            autoCapitalize="none"
                            keyboardType="email-address"
                            returnKeyType="next"
                            onSubmitEditing={() =>
                                passwordInputRef.current?.focus()
                            }
                        />
                    </View>

                    {/* Password Input */}
                    <Text className="mb-2 font-medium text-white text-sm">
                        Password
                    </Text>
                    <View className="flex-row items-center bg-primary/90 mb-4 px-2 py-[2px] rounded-full">
                        <TextInput
                            ref={passwordInputRef}
                            className="flex-1 ml-3 text-white text-base"
                            placeholder="••••••••"
                            placeholderTextColor="#999"
                            value={password}
                            onChangeText={setPassword}
                            secureTextEntry={!showPassword}
                            autoCapitalize="none"
                            returnKeyType="done"
                            onSubmitEditing={handleSignIn}
                        />
                        <TouchableOpacity
                            onPress={() => setShowPassword(!showPassword)}
                            className="p-1 pr-4"
                        >
                            <FontAwesome5
                                name={showPassword ? "eye-slash" : "eye"}
                                size={20}
                                color="#999"
                            />
                        </TouchableOpacity>
                    </View>

                    {/* Forget Password */}
                    <TouchableOpacity className="items-end mb-10">
                        <Text className="text-gray-400 text-sm">
                            Forget Password?
                        </Text>
                    </TouchableOpacity>

                    {showError ? (
                        <Text className="mb-4 text-red-500 text-center">
                            {showError}
                        </Text>
                    ) : null}

                    {/* Sign In Button */}
                    <TouchableOpacity
                        className="items-center bg-secondary mb-6 py-4 rounded-full"
                        onPress={handleSignIn}
                        activeOpacity={0.8}
                        disabled={loading}
                    >
                        {loading ? (
                            <ActivityIndicator color="#FFFFFF" size="small" />
                        ) : (
                            <Text className="font-semibold text-white text-base">
                                Sign In
                            </Text>
                        )}
                    </TouchableOpacity>

                    {/* Divider */}
                    <View className="flex-row items-center mb-6">
                        <View className="flex-1 bg-gray-100/30 h-px" />
                        <Text className="mx-4 text-gray-400 text-sm">or</Text>
                        <View className="flex-1 bg-gray-100/30 h-px" />
                    </View>

                    {/* Google Sign In */}
                    <TouchableOpacity
                        className="bg-white mb-4 py-3 border rounded-full"
                        onPress={handleGoogleSignIn}
                        activeOpacity={0.8}
                    >
                        <View className="flex-row justify-evenly items-center">
                            <SvgLogoGoogle />
                            <Text className="text-black text-base">
                                Sign In with Google
                            </Text>
                        </View>
                    </TouchableOpacity>

                    {/* Sign Up Link */}
                    <View className="flex-row justify-center mt-6">
                        <Text className="text-gray-400 text-sm">
                            Don't have account?{" "}
                        </Text>
                        <Link href="/auth/register" asChild>
                            <TouchableOpacity activeOpacity={0.7}>
                                <Text className="font-semibold text-white text-sm">
                                    Sign Up
                                </Text>
                            </TouchableOpacity>
                        </Link>
                    </View>
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
