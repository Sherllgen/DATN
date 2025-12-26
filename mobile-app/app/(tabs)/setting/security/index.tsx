import { ScrollView, View } from "react-native";

import MenuItem from "@/components/setting_page/MenuItem";
import AppHeader from "@/components/ui/AppHeader";
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
            className="flex-1 pb-[80px]"
        >
            <SafeAreaView className="flex-1">
                {/* Header */}
                <AppHeader title="Security" />

                <ScrollView className="px-6">
                    {/* Menu Items */}
                    <View>
                        <MenuItem
                            title="Change Password"
                            onPress={() => {
                                router.push("/setting/security/changePassword");
                            }}
                        />
                    </View>

                    <View className="h-10" />
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
