import { Stack } from "expo-router";

export default function ChargingLayout() {
    return (
        <Stack>
            <Stack.Screen name="index" options={{ headerShown: false }} />

        </Stack>
    );
}
