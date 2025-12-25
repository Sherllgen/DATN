import { Ionicons } from "@expo/vector-icons";
import {
    Image,
    Modal,
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
import { SafeAreaView } from "react-native-safe-area-context";

export default function SettingPage() {
    const [showLogoutModal, setShowLogoutModal] = useState(false);
    const router = useRouter();

    const user = useUserStore((state) => state.user);

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-6 pt-8"
        >
            <SafeAreaView>
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
                                        "https://i.pravatar.cc/150",
                                }}
                                className="bg-[#4A5568] rounded-full w-[60px] h-[60px]"
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
                            <View className="justify-center items-center bg-[#4A5568] rounded-full w-[60px] h-[60px]">
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

                    <View className="bg-[#4A5568]/50 my-4 h-[1px]" />

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
                                <View className="bg-[#4A5568]/50 my-2 h-[1px]" />
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
                        <View className="bg-[#4A5568]/50 my-2 h-[1px]" />
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
                            onPress={() => setShowLogoutModal(true)}
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

            {/* Logout Confirmation Modal */}
            <Modal
                visible={showLogoutModal}
                transparent={true}
                animationType="fade"
                onRequestClose={() => setShowLogoutModal(false)}
            >
                <TouchableOpacity
                    className="flex-1 justify-center bg-black/70 px-6"
                    onPress={() => setShowLogoutModal(false)}
                    activeOpacity={1}
                >
                    <View className="bg-[#292929] px-6 pt-6 pb-8 rounded-3xl">
                        <Text className="mb-2 font-semibold text-white text-lg text-center">
                            Logout
                        </Text>
                        <Text className="mb-8 text-[#9BA1A6] text-sm text-center">
                            Are you sure you want to logout?
                        </Text>

                        <View className="flex flex-row justify-between gap-4">
                            <TouchableOpacity
                                className="flex-1 py-3 border border-gray-400 rounded-full"
                                onPress={() => setShowLogoutModal(false)}
                                activeOpacity={0.4}
                            >
                                <Text className="font-semibold text-white text-base text-center">
                                    Cancel
                                </Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                className="flex-1 bg-secondary py-3 rounded-full"
                                onPress={() => {
                                    useAuthStore.getState().logout();
                                    setShowLogoutModal(false);
                                    router.replace("/auth/login");
                                }}
                                activeOpacity={0.7}
                            >
                                <Text className="font-semibold text-white text-base text-center">
                                    Logout
                                </Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </TouchableOpacity>
            </Modal>
        </LinearGradient>
    );
}
