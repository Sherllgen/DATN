import { Tabs } from "expo-router";
import React from "react";
import { Pressable, View } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";

import MaterialCommunityIcons from "@expo/vector-icons/MaterialCommunityIcons";

import { HapticTab } from "@/components/haptic-tab";
import { AppColors } from "@/constants/theme";
import { Ionicons, MaterialIcons } from "@expo/vector-icons";

export default function TabLayout() {
    const TAB_HEIGHT = 80;
    const insets = useSafeAreaInsets();

    const CenterTabButton = ({ children, onPress }: any) => {
        return (
            <View
                style={{
                    flex: 1,
                    alignItems: "center",
                    justifyContent: "center",
                }}
            >
                <Pressable
                    onPress={onPress}
                    style={[
                        {
                            position: "absolute",
                            top: 4,
                            width: 44,
                            height: 44,
                            paddingTop: 7,
                            alignItems: "center",
                            // justifyContent: "center",
                            borderRadius: 30,
                            backgroundColor: AppColors.primary,
                        },
                    ]}
                >
                    {children}
                </Pressable>
            </View>
        );
    };

    return (
        <Tabs
            screenOptions={{
                headerShown: false,
                tabBarButton: HapticTab,
                tabBarStyle: {
                    position: "absolute",
                    bottom: insets.bottom,
                    left: 0,
                    right: 0,
                    backgroundColor: "#131315",
                    height: TAB_HEIGHT,
                    paddingTop: 6,
                    borderColor: "#eee",
                },

                tabBarActiveTintColor: "#fff",
            }}
        >
            <Tabs.Screen
                name="home"
                options={{
                    title: "Home",
                    tabBarIcon: ({ color }) => (
                        <Ionicons name="compass" size={28} color={color} />
                    ),
                }}
            />

            <Tabs.Screen
                name="charging"
                options={{
                    title: "Charging",
                    tabBarIcon: ({ color }) => (
                        <MaterialIcons
                            name="energy-savings-leaf"
                            size={28}
                            color={color}
                        />
                    ),
                }}
            />

            <Tabs.Screen
                name="qr_scan"
                options={{
                    tabBarLabel: "",
                    tabBarIcon: () => (
                        <MaterialCommunityIcons
                            name="qrcode-scan"
                            size={24}
                            color="white"
                        />
                    ),
                    tabBarButton: (props) => <CenterTabButton {...props} />,
                }}
            />

            <Tabs.Screen
                name="notifice"
                options={{
                    title: "Notifice",
                    tabBarIcon: ({ color }) => (
                        <MaterialCommunityIcons
                            name="bell"
                            size={30}
                            color={color}
                        />
                    ),
                }}
            />

            <Tabs.Screen
                name="setting"
                options={{
                    title: "Setting",
                    tabBarIcon: ({ color }) => (
                        <MaterialCommunityIcons
                            name="cog"
                            color={color}
                            size={26}
                        />
                    ),
                    popToTopOnBlur: true,
                }}
            />
        </Tabs>
    );
}
