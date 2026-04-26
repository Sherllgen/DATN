import React from "react";
import { View, Text } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useRouter } from "expo-router";
import Button from "../ui/Button";

export interface GuestPlaceholderProps {
    /** Title of the placeholder */
    title?: string;
    /** Description message */
    description?: string;
    /** Icon name from Ionicons */
    icon?: keyof typeof Ionicons.glyphMap;
    /** Custom text for login button */
    loginButtonText?: string;
    /** Custom action for login button (default: navigate to /auth/login) */
    onLoginPress?: () => void;
}

/**
 * A placeholder component shown when a user is not authenticated
 * but tries to access a protected feature/page.
 */
export default function GuestPlaceholder({
    title = "Authentication Required",
    description = "Please log in to view your data and use all features of EV-Go.",
    icon = "person-circle-outline",
    loginButtonText = "Login Now",
    onLoginPress,
}: GuestPlaceholderProps) {
    const router = useRouter();

    const handleLogin = () => {
        if (onLoginPress) {
            onLoginPress();
        } else {
            router.push("/auth/login");
        }
    };

    return (
        <View className="flex-1 justify-center items-center px-8 py-10">
            {/* Icon Container */}
            <View className="bg-white/5 p-6 rounded-full mb-6 border border-white/10">
                <Ionicons name={icon} size={80} color="#00A452" />
            </View>
            
            {/* Text Content */}
            <Text className="text-white text-2xl font-bold text-center mb-2">
                {title}
            </Text>
            
            <Text className="text-white/60 text-base text-center mb-10 leading-6">
                {description}
            </Text>
            
            {/* Actions */}
            <Button 
                variant="primary" 
                size="lg" 
                fullWidth 
                onPress={handleLogin}
            >
                {loginButtonText}
            </Button>
            
            <Button 
                variant="ghost" 
                className="mt-4"
                textClassName="text-white/40"
                onPress={() => router.push("/(tabs)/home")}
            >
                Return to Home
            </Button>
        </View>
    );
}
