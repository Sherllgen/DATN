import { ScrollView, View } from "react-native";

import MenuItem from "@/components/setting_page/MenuItem";
import { useUserStore } from "@/contexts/user.store";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useState } from "react";
import { SafeAreaView } from "react-native-safe-area-context";

export default function SecurityScreen() {
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
                    {/* Menu Items */}
                    <View>
                        <MenuItem
                            title="Password"
                            onPress={() => {
                                router.push("/setting/security/password");
                            }}
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

                    <View className="h-10" />
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
