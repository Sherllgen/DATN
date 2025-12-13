import {
    DarkTheme,
    DefaultTheme,
    ThemeProvider,
} from "@react-navigation/native";
import { Stack } from "expo-router";
import "react-native-reanimated";

import * as SplashScreen from "expo-splash-screen";

import { AuthProvider } from "@/contexts/AuthContext";
import { useColorScheme } from "@/hooks/use-color-scheme";

import "../global.css";

export const unstable_settings = {
    anchor: "(tabs)",
};

SplashScreen.setOptions({
    duration: 1000,
    fade: true,
});

export default function RootLayout() {
    const colorScheme = useColorScheme();

    return (
        <AuthProvider>
            <ThemeProvider
                value={colorScheme === "dark" ? DarkTheme : DefaultTheme}
            >
                <Stack>
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
        </AuthProvider>
    );
}
