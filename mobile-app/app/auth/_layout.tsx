import { Stack } from "expo-router";

export default function AuthLayout() {
    return (
        <Stack
            screenOptions={{
                contentStyle: { backgroundColor: "#000000" },
                animation: "fade",
            }}
        >
            <Stack.Screen name="index" />
            <Stack.Screen name="register" options={{ headerShown: false }} />
            <Stack.Screen name="login" options={{ headerShown: false }} />
            <Stack.Screen name="otp-verify" options={{ headerShown: false }} />

            {/* <Stack.Screen name="[id]" options={{ headerShown: false }} /> */}
        </Stack>
    );
}
