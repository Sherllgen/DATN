import { Platform, StyleSheet, Text, TouchableOpacity, View } from "react-native";
import MapView, { UrlTile, Marker } from 'react-native-maps';
import { SafeAreaView } from "react-native-safe-area-context";
import { router } from "expo-router";
import GradientBackground from "@/components/ui/GradientBackground";

export default function HomePage() {
    return (
        <GradientBackground className="flex-1 px-6 pt-6">
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
                        <View className="flex-row items-center justify-between">
                            <Text style={[styles.h3, { marginTop: 30 }]}>Map</Text>
                            <TouchableOpacity
                                // @ts-ignore - Dynamic route
                                onPress={() => router.push("/map")}
                                activeOpacity={0.7}
                                className="mt-7"
                            >
                            </TouchableOpacity>
                        </View>
                        <TouchableOpacity
                            style={{ height: 208, marginTop: 16 }}
                            className="rounded-2xl overflow-hidden"
                            // @ts-ignore - Dynamic route
                            onPress={() => router.push("/map")}
                            activeOpacity={0.9}
                        >
                            <MapView
                                style={{ flex: 1 }}
                                mapType="none"
                                scrollEnabled={false}
                                zoomEnabled={false}
                                initialRegion={{
                                    latitude: 10.8231,
                                    longitude: 106.6297,
                                    latitudeDelta: 0.05,
                                    longitudeDelta: 0.05,
                                }}
                            >
                                <UrlTile
                                    urlTemplate="https://a.basemaps.cartocdn.com/light_all/{z}/{x}/{y}.png"
                                    maximumZ={19}
                                    flipY={false}
                                    zIndex={1}
                                    tileSize={256}
                                />

                                {/* Marker nên có zIndex cao hơn Tile */}
                                <Marker
                                    coordinate={{ latitude: 10.8231, longitude: 106.6297 }}
                                    zIndex={2}
                                />
                            </MapView>
                        </TouchableOpacity>
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
        </GradientBackground>
    );
}

const styles = StyleSheet.create({
    h3: { fontSize: 16, fontWeight: "600", color: "white" },
});
