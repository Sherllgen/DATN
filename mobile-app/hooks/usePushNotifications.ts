import { useEffect, useRef, useState } from "react";
import * as Device from "expo-device";
import * as Notifications from "expo-notifications";
import { Platform } from "react-native";
import Constants from "expo-constants";
import { registerPushTokenApi } from "@/apis/notificationApi/notificationApi";

// ─────────────────────────────────────────────────────────
// Configure notification handler (how notifs appear in-app)
// ─────────────────────────────────────────────────────────
Notifications.setNotificationHandler({
    handleNotification: async () => ({
        shouldShowAlert: true,
        shouldPlaySound: true,
        shouldSetBadge: true,
        shouldShowBanner: true,
        shouldShowList: true,
    }),
});

// ─────────────────────────────────────────────────────────
// Types
// ─────────────────────────────────────────────────────────
export interface PushNotificationState {
    expoPushToken: string | null;
    notification: Notifications.Notification | null;
}

// ─────────────────────────────────────────────────────────
// Hook
// ─────────────────────────────────────────────────────────

/**
 * Manages Expo Push Notifications end-to-end:
 * 1. Sets up Android notification channel (required for Android 8+).
 * 2. Requests OS permission.
 * 3. Fetches the device's Expo Push Token.
 * 4. Registers the token with the EV-Go backend.
 * 5. Listens for foreground notifications.
 * 6. Listens for notification interactions (tap to open).
 *
 * @param isAuthenticated - Pass `true` only when the user is logged in,
 *                          so the token is registered against the correct account.
 */
export function usePushNotifications(isAuthenticated: boolean): PushNotificationState {
    const [expoPushToken, setExpoPushToken] = useState<string | null>(null);
    const [notification, setNotification] = useState<Notifications.Notification | null>(null);

    const notificationListener = useRef<Notifications.EventSubscription | null>(null);
    const responseListener = useRef<Notifications.EventSubscription | null>(null);

    // ── Android notification channel ─────────────────────────
    useEffect(() => {
        if (Platform.OS === "android") {
            Notifications.setNotificationChannelAsync("default", {
                name: "EV-Go Notifications",
                importance: Notifications.AndroidImportance.MAX,
                vibrationPattern: [0, 250, 250, 250],
                lightColor: "#00A452",
                sound: "default",
            }).catch((err: unknown) =>
                console.warn("[usePushNotifications] Failed to set Android notification channel:", err)
            );
        }
    }, []);

    // ── Register & fetch token ──────────────────────────────
    useEffect(() => {
        if (!isAuthenticated) return;

        registerForPushNotificationsAsync()
            .then((token) => {
                if (!token) return;
                setExpoPushToken(token);

                console.log('\n\n================================================');
                console.log('🔔 YOUR EXPO PUSH TOKEN for testing:');
                console.log(token);
                console.log('================================================\n\n');

                // Register with backend
                const deviceType: "ios" | "android" = Platform.OS === "ios" ? "ios" : "android";
                registerPushTokenApi(token, deviceType).catch((err: unknown) =>
                    console.warn("[usePushNotifications] Failed to register token with backend:", err)
                );
            })
            .catch((err: unknown) =>
                console.warn("[usePushNotifications] Failed to get push token:", err)
            );
    }, [isAuthenticated]);

    // ── Foreground notification listener ────────────────────
    useEffect(() => {
        notificationListener.current =
            Notifications.addNotificationReceivedListener((notif) => {
                console.log(
                    "[usePushNotifications] Foreground notification received:",
                    notif.request.content.title,
                    notif.request.content.body
                );
                setNotification(notif);
            });

        // Interaction listener (user taps a notification)
        responseListener.current =
            Notifications.addNotificationResponseReceivedListener((response) => {
                const content = response.notification.request.content;
                console.log(
                    "[usePushNotifications] Notification tapped:",
                    content.title,
                    content.body
                );
                // TODO: navigate to the relevant screen based on response.notification.request.content.data
            });

        return () => {
            notificationListener.current?.remove();
            responseListener.current?.remove();
        };
    }, []);

    return { expoPushToken, notification };
}

// ─────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────

/**
 * Requests OS permission for push notifications and returns the Expo Push Token.
 * Returns `null` if running on a simulator/emulator (iOS only — Android emulators
 * support push notifications), permission is denied, or projectId is missing.
 *
 * NOTE: Push notifications do NOT work on iOS Simulators. Testing on iOS
 * requires a physical device. Android Emulators with Google Play Services
 * DO support push notifications.
 */
async function registerForPushNotificationsAsync(): Promise<string | null> {
    // Push notifications require a physical device on iOS.
    // Android emulators with Google Play Services can receive push notifications.
    if (!Device.isDevice) {
        console.warn(
            "[usePushNotifications] Must use physical device for Push Notifications. " +
            "(Android Emulators may work, but iOS Simulators will not.)"
        );
        return null;
    }

    // Check and request permission
    const { status: existingStatus } = await Notifications.getPermissionsAsync();
    let finalStatus = existingStatus;

    if (existingStatus !== "granted") {
        const { status } = await Notifications.requestPermissionsAsync();
        finalStatus = status;
    }

    if (finalStatus !== "granted") {
        console.warn("[usePushNotifications] Push notification permission denied.");
        return null;
    }

    // Requires a valid EAS projectId
    const projectId =
        Constants.expoConfig?.extra?.eas?.projectId ??
        Constants.easConfig?.projectId;

    if (!projectId) {
        console.warn("[usePushNotifications] Missing EAS projectId — cannot fetch push token.");
        return null;
    }

    try {
        const tokenData = await Notifications.getExpoPushTokenAsync({ projectId });
        return tokenData.data;
    } catch (err: unknown) {
        console.error("[usePushNotifications] Error fetching Expo push token:", err);
        return null;
    }
}
