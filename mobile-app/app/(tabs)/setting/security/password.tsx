import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { ScrollView, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function PasswordScreen() {
    const router = useRouter();

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-6 pt-8"
        >
            <SafeAreaView>
                <ScrollView>
                    <View>
                        <Text>hello</Text>
                    </View>
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
