import { Stack } from "expo-router";

export default function NotificeLayout() {
    return (
        <Stack>
            <Stack.Screen name="index" options={{ headerShown: false }} />
            <Stack.Screen name="detail" options={{ headerShown: false }} />

            {/* <Stack.Screen name="[id]" options={{ headerShown: false }} /> */}
        </Stack>
    );
}
