import { FontAwesome5 } from "@expo/vector-icons";
import { LinearGradient } from "expo-linear-gradient";
import React, { useEffect, useRef, useState } from "react";
import {
    Animated,
    Dimensions,
    Easing,
    Text,
    TouchableOpacity,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import Svg, { Circle } from "react-native-svg";

const AnimatedCircle = Animated.createAnimatedComponent(Circle);

const { width } = Dimensions.get("window");

export default function ChargingPage() {
    const [chargingPercentage, setChargingPercentage] = useState(90);
    const [power, setPower] = useState(240);
    const [price, setPrice] = useState(0);
    const [chargingTime, setChargingTime] = useState("00:00:00");

    // Animation values
    const progressAnim = useRef(new Animated.Value(0)).current;
    const pulseAnim = useRef(new Animated.Value(1)).current;
    const fadeAnim = useRef(new Animated.Value(0)).current;

    const stationName = "Trạm sạc EV";
    const address = "16, Lý Thường Kiệt, Dĩ An, Bình Dương";

    // Circle properties
    const size = width * 0.47;
    const strokeWidth = 10;
    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;

    const progress = progressAnim.interpolate({
        inputRange: [0, 100],
        outputRange: [circumference, 0],
    });

    const handleStop = () => {
        console.log("Stop charging");
        // Add stop charging logic
    };

    const handlePayment = () => {
        console.log("Go to payment");
        // Add payment navigation logic
    };

    // Animate progress when percentage changes
    useEffect(() => {
        Animated.timing(progressAnim, {
            toValue: chargingPercentage,
            duration: 1000,
            easing: Easing.out(Easing.cubic),
            useNativeDriver: true,
        }).start();
    }, [chargingPercentage]);

    // Pulse animation for "Charging..." text
    useEffect(() => {
        Animated.loop(
            Animated.sequence([
                Animated.timing(pulseAnim, {
                    toValue: 1.1,
                    duration: 1000,
                    easing: Easing.inOut(Easing.ease),
                    useNativeDriver: true,
                }),
                Animated.timing(pulseAnim, {
                    toValue: 1,
                    duration: 1000,
                    easing: Easing.inOut(Easing.ease),
                    useNativeDriver: true,
                }),
            ])
        ).start();
    }, []);

    // Fade in animation on mount
    useEffect(() => {
        Animated.timing(fadeAnim, {
            toValue: 1,
            duration: 800,
            easing: Easing.out(Easing.ease),
            useNativeDriver: true,
        }).start();
    }, []);

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-6 pt-6"
        >
            <SafeAreaView className="flex-1">
                {/* Main Content */}
                <View className="flex-1 pt-8">
                    {/* Circular Progress */}
                    <Animated.View
                        className="justify-center items-center mb-10"
                        style={{
                            opacity: fadeAnim,
                            shadowColor: "#10B981",
                            shadowOffset: { width: 0, height: 0 },
                            shadowOpacity: 0.8,
                            shadowRadius: 20,
                            elevation: 10,
                        }}
                    >
                        <Svg width={size} height={size}>
                            {/* Background Circle */}
                            <Circle
                                cx={size / 2}
                                cy={size / 2}
                                r={radius}
                                stroke="#2C3E50"
                                strokeWidth={strokeWidth}
                                fill="none"
                            />
                            {/* Progress Circle */}
                            <AnimatedCircle
                                cx={size / 2}
                                cy={size / 2}
                                r={radius}
                                stroke="#10B981"
                                strokeWidth={strokeWidth}
                                fill="none"
                                strokeDasharray={circumference}
                                strokeDashoffset={progress}
                                strokeLinecap="round"
                                transform={`rotate(-90 ${size / 2} ${size / 2})`}
                            />
                        </Svg>

                        {/* Center Content */}
                        <View className="absolute justify-center items-center mt-2 ml-5">
                            <Animated.View
                                className="flex-row items-baseline"
                                style={{
                                    transform: [{ scale: pulseAnim }],
                                }}
                            >
                                <Text className="font-bold text-white text-5xl">
                                    {chargingPercentage}
                                </Text>
                                <FontAwesome5
                                    name="percent"
                                    size={22}
                                    color="white"
                                    className="ml-2"
                                />
                            </Animated.View>
                            <Animated.Text
                                className="mt-2 text-secondary text-base"
                                style={{
                                    opacity: pulseAnim.interpolate({
                                        inputRange: [1, 1.1],
                                        outputRange: [0.7, 1],
                                    }),
                                }}
                            >
                                Charing...
                            </Animated.Text>
                        </View>
                    </Animated.View>

                    {/* Stop and Payment */}
                    <View className="flex-row justify-between gap-4 mb-12">
                        <TouchableOpacity
                            className="flex-1 items-center py-3 border border-gray-400 rounded-full"
                            onPress={handleStop}
                        >
                            <Text className="font-semibold text-white">
                                Stop
                            </Text>
                        </TouchableOpacity>

                        <TouchableOpacity
                            className="flex-1 items-center bg-secondary py-3 rounded-full"
                            onPress={handlePayment}
                        >
                            <Text className="font-semibold text-white">
                                Payment
                            </Text>
                        </TouchableOpacity>
                    </View>

                    {/* Information Section */}
                    <View className="flex-1 bg-white/10 -mx-6 p-6 rounded-t-3xl">
                        {/* <Text className="mb-6 font-bold text-white text-lg">
                            Thông tin
                        </Text> */}

                        <View className="flex-row justify-start items-center gap-4 mb-4">
                            <Text className="font-medium text-white text-base">
                                Trạm:
                            </Text>
                            <Text className="text-gray-400 text-sm">
                                {stationName}
                            </Text>
                        </View>

                        <View className="flex-row justify-start items-center gap-4 mb-4">
                            <Text className="font-medium text-white text-base">
                                Địa chỉ:
                            </Text>
                            <Text className="text-gray-400 text-sm">
                                {address}
                            </Text>
                        </View>

                        <View className="flex-row justify-start items-center gap-4 mb-4">
                            <Text className="font-medium text-white text-base">
                                Công suất:
                            </Text>
                            <Text className="text-gray-400 text-sm">
                                {power} kW
                            </Text>
                        </View>

                        <View className="flex-row justify-start items-center gap-4 mb-4">
                            <Text className="font-medium text-white text-base">
                                Đơn giá:
                            </Text>
                            <Text className="text-gray-400 text-sm">
                                {price.toLocaleString()} VNĐ/kWh
                            </Text>
                        </View>

                        <View className="flex-row justify-start items-center gap-4 mb-4">
                            <Text className="font-medium text-white text-base">
                                Thời gian sạc:
                            </Text>
                            <Text className="text-gray-400 text-sm">
                                {chargingTime}
                            </Text>
                        </View>
                    </View>
                </View>
            </SafeAreaView>
        </LinearGradient>
    );
}
