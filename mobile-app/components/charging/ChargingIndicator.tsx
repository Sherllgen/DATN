import React, { useEffect, useRef, useMemo } from "react";
import { View, Text, Dimensions, Animated, Easing } from "react-native";
import Svg, { Circle, Defs, RadialGradient, Stop } from "react-native-svg";
import { Ionicons } from "@expo/vector-icons";
import { ChargingSessionStatus } from "@/types/charging.types";

const { width } = Dimensions.get("window");

export interface ChargingIndicatorProps {
    consumedKwh: number;
    status: ChargingSessionStatus;
}

export default function ChargingIndicator({ consumedKwh, status }: ChargingIndicatorProps) {
    const size = width * 0.75;
    const strokeWidth = 16;
    const radius = (size - strokeWidth) / 2;

    // Status-based color
    let color = "#00A452";
    switch (status) {
        case ChargingSessionStatus.PREPARING:
            color = "#F59E0B";
            break;
        case ChargingSessionStatus.CHARGING:
        case ChargingSessionStatus.FINISHING:
        case ChargingSessionStatus.COMPLETED:
            color = "#00A452";
            break;
        case ChargingSessionStatus.FAULTED:
            color = "#EF4444";
            break;
        case ChargingSessionStatus.SUSPENDED_EV:
        case ChargingSessionStatus.SUSPENDED_EVSE:
        case ChargingSessionStatus.INTERRUPTED:
            color = "#F59E0B";
            break;
        default:
            color = "#00A452";
    }

    // Pulse animation for the outer glow ring
    const pulseAnim = useRef(new Animated.Value(1)).current;

    useEffect(() => {
        const isActive = [
            ChargingSessionStatus.CHARGING,
            ChargingSessionStatus.PREPARING,
        ].includes(status);

        if (isActive) {
            const loop = Animated.loop(
                Animated.sequence([
                    Animated.timing(pulseAnim, {
                        toValue: 1.08,
                        duration: 1800,
                        easing: Easing.inOut(Easing.ease),
                        useNativeDriver: true,
                    }),
                    Animated.timing(pulseAnim, {
                        toValue: 1,
                        duration: 1800,
                        easing: Easing.inOut(Easing.ease),
                        useNativeDriver: true,
                    }),
                ])
            );
            loop.start();
            return () => loop.stop();
        } else {
            pulseAnim.setValue(1);
        }
    }, [status]);

    // Opacity animation for the glow
    const glowOpacity = useRef(new Animated.Value(0.5)).current;

    useEffect(() => {
        const isActive = [
            ChargingSessionStatus.CHARGING,
            ChargingSessionStatus.PREPARING,
        ].includes(status);

        if (isActive) {
            const loop = Animated.loop(
                Animated.sequence([
                    Animated.timing(glowOpacity, {
                        toValue: 0.9,
                        duration: 2000,
                        easing: Easing.inOut(Easing.ease),
                        useNativeDriver: true,
                    }),
                    Animated.timing(glowOpacity, {
                        toValue: 0.4,
                        duration: 2000,
                        easing: Easing.inOut(Easing.ease),
                        useNativeDriver: true,
                    }),
                ])
            );
            loop.start();
            return () => loop.stop();
        } else {
            glowOpacity.setValue(0.5);
        }
    }, [status]);

    // Decorative stars positioned around the circle
    const stars = useMemo(() => {
        const count = 20;
        const result = [];
        const glowSize = size + 100;
        for (let i = 0; i < count; i++) {
            const angle = Math.random() * Math.PI * 2;
            const distance = radius + 20 + Math.random() * 40;
            result.push({
                x: glowSize / 2 + Math.cos(angle) * distance,
                y: glowSize / 2 + Math.sin(angle) * distance,
                r: 1 + Math.random() * 2,
                opacity: 0.3 + Math.random() * 0.5,
            });
        }
        return result;
    }, [size, radius]);

    return (
        <View className="items-center justify-center py-10">
            <View style={{ width: size, height: size }} className="items-center justify-center">
                {/* Animated Glow Effect */}
                <Animated.View
                    style={{
                        position: "absolute",
                        width: size + 100,
                        height: size + 100,
                        transform: [{ scale: pulseAnim }],
                        opacity: glowOpacity,
                    }}
                >
                    <Svg width={size + 100} height={size + 100}>
                        <Defs>
                            <RadialGradient id="glow" cx="50%" cy="50%" r="50%">
                                <Stop offset="0%" stopColor={color} stopOpacity="0.25" />
                                <Stop offset="70%" stopColor={color} stopOpacity="0.08" />
                                <Stop offset="100%" stopColor={color} stopOpacity="0" />
                            </RadialGradient>
                        </Defs>
                        <Circle
                            cx={(size + 100) / 2}
                            cy={(size + 100) / 2}
                            r={(size + 100) / 2}
                            fill="url(#glow)"
                        />

                        {/* Decorative Stars */}
                        {stars.map((star, index) => (
                            <Circle
                                key={index}
                                cx={star.x}
                                cy={star.y}
                                r={star.r}
                                fill={color}
                                opacity={star.opacity}
                            />
                        ))}
                    </Svg>
                </Animated.View>

                {/* Full Circle Ring */}
                <Svg width={size} height={size}>
                    {/* Dark background ring */}
                    <Circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke="#1A202C"
                        strokeWidth={strokeWidth}
                        fill="none"
                    />
                    {/* Full colored ring */}
                    <Circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke={color}
                        strokeWidth={strokeWidth}
                        fill="none"
                        opacity={0.85}
                    />
                </Svg>

                {/* Center Content */}
                <View className="absolute items-center">
                    <Ionicons name="flash" size={40} color={color} style={{ marginBottom: 16 }} />
                    <View className="flex-row items-baseline">
                        <Text className="text-white text-6xl font-bold">
                            {consumedKwh.toFixed(1)}
                        </Text>
                        <Text className="text-white text-xl ml-2">kWh</Text>
                    </View>
                    <Text className="text-text-secondary text-lg mt-2">Energy</Text>
                </View>
            </View>
        </View>
    );
}
