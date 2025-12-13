import { LinearGradient } from "expo-linear-gradient";
import { useState } from "react";
import { Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomePage() {
    const [isSwitchOn, setIsSwitchOn] = useState(false);

    const onToggleSwitch = () => setIsSwitchOn(!isSwitchOn);

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-6 pt-6"
        >
            <SafeAreaView>
                <View>
                    <Text className="text-red-300 text-2xl">Home Page</Text>
                </View>
            </SafeAreaView>
        </LinearGradient>
    );
}
