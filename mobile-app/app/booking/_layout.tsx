import { Stack } from "expo-router";

export default function BookingLayout() {
    return (
        <Stack screenOptions={{ headerShown: false }}>
            <Stack.Screen name="selectVehicle" options={{ headerShown: false }} />
            <Stack.Screen name="selectCharger" options={{ headerShown: false }} />
            <Stack.Screen name="selectTime" options={{ headerShown: false }} />
            <Stack.Screen name="[id]" options={{ headerShown: false }} />
        </Stack>
    );
}
