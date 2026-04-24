import { withLayoutContext } from "expo-router";
import {
    createMaterialTopTabNavigator,
    MaterialTopTabNavigationOptions,
    MaterialTopTabBarProps,
} from "@react-navigation/material-top-tabs";
import React from "react";
import { Pressable, View, Text } from "react-native";
import { useSafeAreaInsets } from "react-native-safe-area-context";
import MaterialCommunityIcons from "@expo/vector-icons/MaterialCommunityIcons";
import { AppColors } from "@/constants/theme";
import { Ionicons, MaterialIcons } from "@expo/vector-icons";
import { useUserStore } from "@/contexts/user.store";
import { Alert } from "react-native";

// Create the custom navigator
const { Navigator } = createMaterialTopTabNavigator();

const MaterialTopTabs = withLayoutContext(Navigator);

export default function TabLayout() {
    const TAB_HEIGHT = 50;
    const insets = useSafeAreaInsets();

    const CenterTabButton = ({ onPress, isFocused }: { onPress?: () => void; isFocused: boolean }) => {
        const unpaidCount = useUserStore((state) => state.unpaidCount) || 0;

        const handlePress = () => {
            if (unpaidCount > 0) {
                Alert.alert("Action Blocked", "Please settle your unpaid invoices before starting a new charging session.");
                return;
            }
            if (onPress) onPress();
        };

        return (
            <View
                style={{
                    flex: 1,
                    alignItems: "center",
                    justifyContent: "center",
                }}
            >
                <Pressable
                    onPress={handlePress}
                    style={[
                        {
                            width: 50,
                            height: 50,
                            marginTop: 4,
                            alignItems: "center",
                            justifyContent: "center",
                            borderRadius: 30,
                            backgroundColor: AppColors.primary,
                            shadowColor: "#000",
                            shadowOffset: { width: 0, height: 2 },
                            shadowOpacity: 0.25,
                            shadowRadius: 3.84,
                            elevation: 5,
                        },
                    ]}
                >
                    <MaterialCommunityIcons
                        name="qrcode-scan"
                        size={24}
                        color="white"
                    />
                </Pressable>
            </View>
        );
    };

    // Custom Tab Bar to position Top Tabs at the bottom
    const CustomTabBar = ({ state, descriptors, navigation }: MaterialTopTabBarProps) => {
        return (
            <View
                style={{
                    flexDirection: 'row',
                    backgroundColor: "#131315",
                    height: TAB_HEIGHT + insets.bottom,
                    paddingTop: 6,
                    paddingBottom: insets.bottom,
                    borderTopWidth: 0.5,
                    borderTopColor: "rgba(255,255,255,0.1)",
                }}
            >
                {state.routes.map((route, index) => {
                    const { options } = descriptors[route.key];
                    const label = options.tabBarLabel !== undefined
                        ? options.tabBarLabel
                        : options.title !== undefined
                            ? options.title
                            : route.name;

                    const isFocused = state.index === index;

                    const onPress = () => {
                        const event = navigation.emit({
                            type: 'tabPress',
                            target: route.key,
                            canPreventDefault: true,
                        });

                        if (!isFocused && !event.defaultPrevented) {
                            navigation.navigate(route.name);
                        }
                    };

                    const onLongPress = () => {
                        navigation.emit({
                            type: 'tabLongPress',
                            target: route.key,
                        });
                    };

                    const color = isFocused ? "#fff" : "#9BA1A6";

                    // Handle the special QR Scan button
                    if (route.name === "qr_scan") {
                        return (
                            <CenterTabButton
                                key={route.key}
                                onPress={onPress}
                                isFocused={isFocused}
                            />
                        );
                    }

                    // Hide specific routes (like ghost 'charging' or Gallery)
                    // @ts-ignore - href is provided by expo-router despite top-tab types
                    if ((options as any).href === null || route.name === "charging") return null;

                    return (
                        <Pressable
                            key={route.key}
                            accessibilityRole="button"
                            accessibilityState={isFocused ? { selected: true } : {}}
                            accessibilityLabel={options.tabBarAccessibilityLabel}
                            testID={(options as any).tabBarTestID}
                            onPress={onPress}
                            onLongPress={onLongPress}
                            className="flex-1 items-center justify-center py-2 mt-1"
                        >
                            <View className="items-center">
                                {options.tabBarIcon && options.tabBarIcon({ color, focused: isFocused })}
                                <Text style={{ color, fontSize: 10, marginTop: 4, fontWeight: isFocused ? '600' : '400' }}>
                                    {label as string}
                                </Text>
                            </View>
                        </Pressable>
                    );
                })}
            </View>
        );
    };

    return (
        <MaterialTopTabs
            tabBarPosition="bottom"
            tabBar={(props) => <CustomTabBar {...props} />}
            screenOptions={{
                swipeEnabled: true,
                lazy: true,
                tabBarActiveTintColor: "#fff",
                tabBarInactiveTintColor: "#9BA1A6",
            }}
        >
            <MaterialTopTabs.Screen
                name="home"
                options={{
                    title: "Home",
                    tabBarIcon: ({ color }: { color: string }) => (
                        <Ionicons name="compass" size={26} color={color} />
                    ),
                }}
            />

            <MaterialTopTabs.Screen
                name="booking"
                options={{
                    title: "My Booking",
                    tabBarIcon: ({ color }: { color: string }) => (
                        <MaterialCommunityIcons
                            name="calendar-check"
                            size={26}
                            color={color}
                        />
                    ),
                }}
            />

            <MaterialTopTabs.Screen
                name="qr_scan"
                options={{
                    title: "Scan",
                }}
            />

            <MaterialTopTabs.Screen
                name="payment"
                options={{
                    title: "Payment",
                    tabBarIcon: ({ color }: { color: string }) => (
                        <MaterialCommunityIcons
                            name="wallet"
                            size={28}
                            color={color}
                        />
                    ),
                }}
            />

            <MaterialTopTabs.Screen
                name="setting"
                options={{
                    title: "Setting",
                    tabBarIcon: ({ color }: { color: string }) => (
                        <MaterialCommunityIcons
                            name="cog"
                            color={color}
                            size={26}
                        />
                    ),
                }}
            />

            <MaterialTopTabs.Screen
                name="component-gallery"
                options={{
                    title: "Gallery",
                    href: null,
                } as any}
            />
        </MaterialTopTabs>
    );
}
