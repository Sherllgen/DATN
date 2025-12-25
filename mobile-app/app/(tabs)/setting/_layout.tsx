import { Stack } from "expo-router";

export default function SettingLayout() {
    return (
        <Stack>
            <Stack.Screen name="index" options={{ headerShown: false }} />
            <Stack.Screen name="profile" options={{ headerShown: false }} />
            <Stack.Screen name="myVehicle" options={{ headerShown: false }} />
            <Stack.Screen name="security" options={{ headerShown: false }} />

            {/* <Stack.Screen name="[id]" options={{ headerShown: false }} /> */}
        </Stack>
    );
}
