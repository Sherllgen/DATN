import { DarkTheme, ThemeProvider } from "@react-navigation/native";
import { Stack } from "expo-router";
import "react-native-reanimated";
import { useEffect } from "react";
import { View, Text } from "react-native";
import * as SplashScreen from "expo-splash-screen";

import { LinearGradient } from "expo-linear-gradient";
import ToastManager from "toastify-react-native";
import "../global.css";

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

    useEffect(() => {
        console.log('RootLayout mounted, hiding splash...');
        // Hide the splash screen after the app is ready
        SplashScreen.hideAsync().catch(console.warn);
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
                </Stack>
            </ThemeProvider>
            <ToastManager duration={1500} />
        </LinearGradient>
    );
}
