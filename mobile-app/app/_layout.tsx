import { DarkTheme, ThemeProvider } from "@react-navigation/native";
import { Stack } from "expo-router";
import "react-native-reanimated";
import { useEffect } from "react";
import { View, Text } from "react-native";
import * as SplashScreen from "expo-splash-screen";

import { LinearGradient } from "expo-linear-gradient";
import ToastManager from "toastify-react-native";
import "../global.css";

import { useAuthStore } from "@/contexts/auth.store";
import { useUserStore } from "@/contexts/user.store";
import { getProfileApi } from "@/apis/profileApi/profileApi";
import { usePushNotifications } from "@/hooks/usePushNotifications";

// Prevent the splash screen from auto-hiding before asset loading is complete
// SplashScreen.preventAutoHideAsync().catch(console.warn);

const BlackTheme = {
    ...DarkTheme, // hoặc DefaultTheme đều được, miễn override colors
    colors: {
        ...DarkTheme.colors,
        background: "#000000",
        card: "#000000",
        border: "#000000",
        text: "#FFFFFF",
    },
};

export default function RootLayout() {
    console.log('RootLayout rendering...');
    const accessToken = useAuthStore((state) => state.accessToken);
    const user = useUserStore((state) => state.user);
    const setUser = useUserStore((state) => state.setUser);
    const logout = useAuthStore((state) => state.logout);

    // Register & listen for push notifications once the user is authenticated
    usePushNotifications(!!accessToken);

    useEffect(() => {
        console.log('RootLayout mounted, hiding splash...');
        // Hide the splash screen after the app is ready
        SplashScreen.hideAsync().catch(console.warn);
    }, []);

    useEffect(() => {
        const hydrateUser = async () => {
            try {
                // Since we use withCredentials: true, the session cookie will automatically authenticate this request if valid
                const res = await getProfileApi();
                if (res && res.data) {
                    setUser(res.data);
                }
            } catch (error) {
                // User is not logged in / Session expired -> clear token memory just in case
                console.log("Not logged in or session expired.");
                logout();
            }
        };
        hydrateUser();
    }, []);

    console.log('About to return JSX...');
    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1"
        >
            <ThemeProvider value={BlackTheme}>
                <Stack
                    screenOptions={{
                        headerShown: false, // Remove all native headers
                    }}
                >
                    <Stack.Screen
                        name="(tabs)"
                        options={{ headerShown: false }}
                    />
                    <Stack.Screen
                        name="auth"
                        options={{ headerShown: false }}
                    />
                    <Stack.Screen
                        name="payment"
                        options={{ headerShown: false }}
                    />
                    <Stack.Screen
                        name="map"
                        options={{ headerShown: false }}
                    />
                    <Stack.Screen
                        name="station"
                        options={{ headerShown: false }}
                    />
                    <Stack.Screen
                        name="booking"
                        options={{ headerShown: false }}
                    />
                    <Stack.Screen
                        name="charging"
                        options={{ headerShown: false }}
                    />
                </Stack>
            </ThemeProvider>
            <ToastManager duration={1500} />
        </LinearGradient>
    );
}
