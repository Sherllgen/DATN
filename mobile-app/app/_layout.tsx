import { DarkTheme, ThemeProvider } from "@react-navigation/native";
import { Stack } from "expo-router";
import "react-native-reanimated";

import { LinearGradient } from "expo-linear-gradient";
import ToastManager from "toastify-react-native";
import "../global.css";

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
                        animation: "fade",
                        animationDuration: 1550,
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
                </Stack>
            </ThemeProvider>
            <ToastManager duration={1500} />
        </LinearGradient>
    );
}
