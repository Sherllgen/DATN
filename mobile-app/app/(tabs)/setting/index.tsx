import { Ionicons } from "@expo/vector-icons";
import {
    Alert,
    Image,
    ScrollView,
    Text,
    TouchableOpacity,
    View,
} from "react-native";

import MenuItem from "@/components/setting_page/MenuItem";
import { useAuthStore } from "@/contexts/auth.store";
import { useUserStore } from "@/contexts/user.store";
import { LinearGradient } from "expo-linear-gradient";
import { Href, useRouter } from "expo-router";
import { useState } from "react";
import { logoutApi } from "@/apis/authApi/authApi";
import { SafeAreaView } from "react-native-safe-area-context";
import GradientBackground from "@/components/ui/GradientBackground";

export default function SettingPage() {
    const router = useRouter();

    const user = useUserStore((state) => state.user);

    return (
        <GradientBackground className="flex-1 px-6">
            <SafeAreaView className="flex-1" edges={["top", "left", "right"]}>
                <ScrollView>
                    {/* Profile Section */}
                    {user ? (
                        <TouchableOpacity
                            className="flex-row items-center"
                            activeOpacity={0.7}
                            onPress={() => router.push("/setting/profile")}
                        >
                            <Image
                                source={{
                                    uri:
                                        user.avatarUrl ||
                                        "https://i.pravatar.cc/150?img=12",
                                }}
                                className="bg-border-gray rounded-full w-[60px] h-[60px]"
                            />
                            <View className="flex-1 ml-4">
                                <Text className="mb-1 font-semibold text-white text-lg">
                                    {user.fullName || "User"}
                                </Text>
                                <Text className="text-[#9BA1A6] text-sm">
                                    {user.email || ""}
                                </Text>
                            </View>
                            <Ionicons
                                name="chevron-forward"
                                size={20}
                                color="#9BA1A6"
                            />
                        </TouchableOpacity>
                    ) : (
                        <TouchableOpacity
                            className="flex-row items-center py-4"
                            activeOpacity={0.7}
                            onPress={() => router.push("/auth/login")}
                        >
                            <View className="justify-center items-center bg-border-gray rounded-full w-[60px] h-[60px]">
                                <Ionicons
                                    name="person-outline"
                                    size={32}
                                    color="#9BA1A6"
                                />
                            </View>
                            <View className="flex-1 ml-6">
                                <Text className="mb-1 font-semibold text-white">
                                    Welcome back
                                </Text>
                                <Text className="text-[#9BA1A6] text-sm">
                                    Tap to login or register
                                </Text>
                            </View>
                            <Ionicons
                                name="chevron-forward"
                                size={20}
                                color="#9BA1A6"
                            />
                        </TouchableOpacity>
                    )}

                    <View className="bg-border-gray/50 my-4 h-[1px]" />

                    {/* Menu Items */}
                    <View>
                        {user && (
                            <>
                                <MenuItem
                                    title="Personal Info"
                                    onPress={() =>
                                        router.push("/setting/profile")
                                    }
                                />
                                <MenuItem
                                    title="My Vehicle"
                                    onPress={() =>
                                        router.push("/setting/myVehicle")
                                    }
                                />
                                <View className="bg-border-gray/50 my-2 h-[1px]" />
                                <MenuItem
                                    title="Payment Methods"
                                    onPress={() =>
                                        console.log("Payment Methods")
                                    }
                                />
                            </>
                        )}
                        <MenuItem
                            title="Security"
                            onPress={() =>
                                router.push("/setting/security" as Href)
                            }
                        />
                        <MenuItem
                            title="Privacy Policy"
                            onPress={() => console.log("Privacy Policy")}
                        />
                        <View className="bg-border-gray/50 my-2 h-[1px]" />
                        <MenuItem
                            title="Help Center"
                            onPress={() => console.log("Help Center")}
                        />
                        <MenuItem
                            title="Language"
                            rightText="English (US)"
                            onPress={() => console.log("Language")}
                        />
                        <MenuItem
                            title="About EVPoint"
                            onPress={() => console.log("About EVPoint")}
                        />
                    </View>

                    {/* Logout Button */}
                    {user && (
                        <TouchableOpacity
                            className="mt-8 py-5"
                            onPress={() => {
                                Alert.alert(
                                    "Logout",
                                    "Are you sure you want to logout?",
                                    [
                                        {
                                            text: "Cancel",
                                            style: "cancel"
                                        },
                                        {
                                            text: "Logout",
                                            style: "destructive",
                                            onPress: async () => {
                                                try {
                                                    await logoutApi();
                                                } catch (error) {
                                                    console.error("Logout API failed:", error);
                                                } finally {
                                                    useAuthStore.getState().logout();
                                                    router.replace("/auth/login");
                                                }
                                            }
                                        }
                                    ]
                                );
                            }}
                            activeOpacity={0.7}
                        >
                            <Text className="font-semibold text-[#FF3B30]">
                                Logout
                            </Text>
                        </TouchableOpacity>
                    )}

                    <View className="h-10" />
                </ScrollView>
            </SafeAreaView>
        </GradientBackground>
    );
}
