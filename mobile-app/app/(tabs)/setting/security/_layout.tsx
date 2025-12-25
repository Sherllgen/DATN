import { Stack } from "expo-router";

export default function SecurityLayout() {
    return (
        <Stack>
            <Stack.Screen name="index" options={{ headerShown: false }} />
            <Stack.Screen
                name="changePassword"
                options={{ headerShown: false }}
            />
            <Stack.Screen
                name="forgotPassword"
                options={{ headerShown: false }}
            />

            {/* <Stack.Screen name="[id]" options={{ headerShown: false }} /> */}
        </Stack>
    );
}
