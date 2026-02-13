import { Stack } from "expo-router";

export default function StationLayout() {
    return (
        <Stack
            screenOptions={{
                headerShown: false,
                // Force instant navigation - no animations
                presentation: "card",
                animation: "none",
                animationDuration: 0,
            }}
        >
            <Stack.Screen name="[id]" />
        </Stack>
    );
}
