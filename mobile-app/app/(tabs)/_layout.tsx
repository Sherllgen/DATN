import { Tabs } from "expo-router";
import React from "react";
import { Pressable, View } from "react-native";

import MaterialCommunityIcons from "@expo/vector-icons/MaterialCommunityIcons";

import { HapticTab } from "@/components/haptic-tab";
import { AppColors } from "@/constants/theme";
import { MaterialIcons } from "@expo/vector-icons";

export default function TabLayout() {
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
                    bottom: 0,
                    left: 0,
                    right: 0,
                    backgroundColor: "#131315",
                    height: 80,
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
                        <MaterialCommunityIcons
                            name="home"
                            size={30}
                            color={color}
                        />
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
                    title: "Profile",
                    tabBarIcon: ({ color }) => (
                        <MaterialCommunityIcons
                            name="cog"
                            color={color}
                            size={26}
                        />
                    ),
                }}
            />
        </Tabs>
    );
}
