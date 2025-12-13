import { DarkTheme, ThemeProvider } from "@react-navigation/native";
import { Stack } from "expo-router";
import "react-native-reanimated";

import * as SplashScreen from "expo-splash-screen";

import { AuthProvider } from "@/contexts/AuthContext";

import { LinearGradient } from "expo-linear-gradient";
import "../global.css";

export const unstable_settings = {
    anchor: "(tabs)",
};

SplashScreen.setOptions({
    duration: 1000,
    fade: true,
});

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
        <AuthProvider>
            <LinearGradient
                colors={["#33404F", "#000000"]}
                start={{ x: 0, y: 0 }}
                end={{ x: 0, y: 1 }}
                className="flex-1"
            >
                <ThemeProvider value={BlackTheme}>
                    <Stack
                        screenOptions={{
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
                    </Stack>
                </ThemeProvider>
            </LinearGradient>
        </AuthProvider>
    );
}
