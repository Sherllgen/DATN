import { LinearGradient } from "expo-linear-gradient";
import { StyleSheet, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomePage() {
    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-6 pt-6"
        >
            <SafeAreaView className="flex-1">
                <View>
                    <Text style={styles.h3}>EVGo</Text>
                    <Text style={[styles.h3, { marginTop: 20 }]}>
                        Hi, Authur!
                    </Text>
                </View>

                <View className="flex-1 bg-white/10 -mx-6 mt-8 px-6 rounded-s-[3em]">
                    {/* Map */}
                    <View>
                        <Text style={[styles.h3, { marginTop: 30 }]}>Map</Text>

                        <View style={{ height: 208, marginTop: 16 }}>
                            <View className="bg-gray-600 rounded-2xl w-full h-60"></View>
                            {/* <MapView
                            style={StyleSheet.absoluteFillObject}
                            initialRegion={{
                                latitude: 10.7769,
                                longitude: 106.7009,
                                latitudeDelta: 0.05,
                                longitudeDelta: 0.05,
                            }}
                        >
                            <Marker
                                coordinate={{
                                    latitude: 10.7769,
                                    longitude: 106.7009,
                                }}
                            />
                        </MapView> */}
                        </View>
                    </View>

                    {/* Activities */}
                    <View>
                        <Text style={[styles.h3, { marginTop: 30 }]}>
                            Activities
                        </Text>
                        <View className="flex flex-row justify-between mt-4">
                            <View className="bg-white/10 rounded-2xl w-[64%] h-52"></View>
                            <View className="bg-white/10 rounded-2xl w-[33%] h-52"></View>
                        </View>
                    </View>
                </View>
            </SafeAreaView>
        </LinearGradient>
    );
}

const styles = StyleSheet.create({
    h3: { fontSize: 16, fontWeight: "600", color: "white" },
});
