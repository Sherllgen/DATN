import React from "react";
import { Stack } from "expo-router";

export default function BookingPageLayout() {
    return (
        <Stack
            screenOptions={{
                headerShown: false,
            }}
        >
            <Stack.Screen name="index" />
        </Stack>
    );
}