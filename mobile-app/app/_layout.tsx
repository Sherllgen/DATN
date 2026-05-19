import { DarkTheme, ThemeProvider } from "@react-navigation/native";
import { Stack } from "expo-router";
import "react-native-reanimated";
import { useEffect, useState } from "react";
import { View, Text } from "react-native";
import * as SplashScreen from "expo-splash-screen";
import * as SecureStore from "expo-secure-store";

import { LinearGradient } from "expo-linear-gradient";
import ToastManager from "toastify-react-native";
import "../global.css";

import { useAuthStore, SECURE_KEY_ACCESS_TOKEN, SECURE_KEY_REFRESH_TOKEN } from "@/contexts/auth.store";
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
    const setUser = useUserStore((state) => state.setUser);
    const logout = useAuthStore((state) => state.logout);
    const [tokensLoaded, setTokensLoaded] = useState(false);
    // Keeps splash visible until user profile is fully hydrated
    const [isAppReady, setIsAppReady] = useState(false);

    // Register & listen for push notifications once the user is authenticated
    usePushNotifications(!!accessToken);

    // Get tokens from SecureStore on startup
    useEffect(() => {
        const hydrateTokens = async () => {
            try {
                const [storedAccess, storedRefresh] = await Promise.all([
                    SecureStore.getItemAsync(SECURE_KEY_ACCESS_TOKEN),
                    SecureStore.getItemAsync(SECURE_KEY_REFRESH_TOKEN),
                ]);
                if (storedAccess && storedRefresh) {
                    // Restore both tokens into memory
                    useAuthStore.setState({ accessToken: storedAccess, refreshToken: storedRefresh });
                }
            } catch (e) {
                console.warn("SecureStore hydration failed:", e);
            } finally {
                setTokensLoaded(true);
            }
        };
        hydrateTokens();
    }, []);

    // SplashScreen is hidden only after the full hydration pipeline finishes (Fix #1)
    useEffect(() => {
        if (isAppReady) {
            SplashScreen.hideAsync().catch(console.warn);
        }
    }, [isAppReady]);

    // Hydrate user profile once tokens are restored from SecureStore
    useEffect(() => {
        if (!tokensLoaded) return; // wait for SecureStore read to finish

        const hydrateUser = async () => {
            try {
                const res = await getProfileApi();
                if (res && res.data) {
                    setUser(res.data);
                }
            } catch (error) {
                console.log("Not logged in or session expired.");
                logout();
            } finally {
                setIsAppReady(true); // Always ungate the splash regardless of auth status
            }
        };
        hydrateUser();
    }, [tokensLoaded]);

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
