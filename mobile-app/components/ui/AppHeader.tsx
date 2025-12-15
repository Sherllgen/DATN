import MaterialIcons from "@expo/vector-icons/MaterialIcons";
import { router } from "expo-router";
import React from "react";
import { Pressable, Text, View } from "react-native";

type AppHeaderProps = {
    title?: string;
    showBack?: boolean;
    onBack?: () => void;
    right?: React.ReactNode;

    // style hooks
    className?: string; // wrapper
    titleClassName?: string;
    bgClassName?: string; // header background
    safeTop?: boolean;
};

export default function AppHeader({
    title = "",
    showBack = true,
    onBack,
    right,
    className = "",
    titleClassName = "",
    bgClassName = "transparent",
    safeTop = true,
}: AppHeaderProps) {
    const handleBack = () => {
        if (onBack) return onBack();
        // expo-router back
        router.back();
    };

    const HeaderContent = (
        <View
            className={[
                "h-14 px-4 mb-2 flex-row items-center",
                bgClassName,
                className,
            ].join(" ")}
        >
            {/* Left */}
            <View className="w-11">
                {showBack ? (
                    <Pressable
                        onPress={handleBack}
                        hitSlop={12}
                        android_ripple={{
                            color: "rgba(255,255,255,0.15)",
                            borderless: true,
                        }}
                        className="justify-center items-center rounded-full w-11 h-11"
                    >
                        <MaterialIcons
                            name="arrow-back-ios"
                            size={22}
                            color="white"
                        />
                    </Pressable>
                ) : null}
            </View>

            {/* Center title */}
            <View className="flex-1 justify-center items-center">
                <Text
                    numberOfLines={1}
                    className={["text-white ", titleClassName].join(" ")}
                >
                    {title}
                </Text>
            </View>

            {/* Right */}
            <View className="justify-center items-end w-11">
                {right ? right : null}
            </View>
        </View>
    );

    if (!safeTop) return HeaderContent;

    return <>{HeaderContent}</>;
}
