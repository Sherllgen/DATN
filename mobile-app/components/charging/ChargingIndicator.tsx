import React, { useMemo } from "react";
import { View, Text, Dimensions } from "react-native";
import Svg, { Circle, Defs, RadialGradient, Stop } from "react-native-svg";
import { Ionicons } from "@expo/vector-icons";

const { width } = Dimensions.get("window");

interface ChargingIndicatorProps {
    kwh: number;
    percentage: number;
}

export default function ChargingIndicator({ kwh, percentage }: ChargingIndicatorProps) {
    const size = width * 0.75;
    const strokeWidth = 16;
    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;
    const strokeDashoffset = circumference - (percentage / 100) * circumference;

    // Generate random stars once
    const stars = useMemo(() => {
        const count = 20;
        const result = [];
        const glowSize = size + 100;
        for (let i = 0; i < count; i++) {
            // Random angle and distance from center
            const angle = Math.random() * Math.PI * 2;
            const distance = radius + 20 + Math.random() * 40; // Position stars around the circle
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
                {/* Glow Effect using SVG RadialGradient */}
                <Svg style={{ position: 'absolute' }} width={size + 100} height={size + 100}>
                    <Defs>
                        <RadialGradient id="glow" cx="50%" cy="50%" r="50%">
                            <Stop offset="0%" stopColor="#00A452" stopOpacity="0.2" />
                            <Stop offset="100%" stopColor="#00A452" stopOpacity="0" />
                        </RadialGradient>
                    </Defs>
                    <Circle cx={(size + 100) / 2} cy={(size + 100) / 2} r={(size + 100) / 2} fill="url(#glow)" />
                    
                    {/* Decorative Stars */}
                    {stars.map((star, index) => (
                        <Circle
                            key={index}
                            cx={star.x}
                            cy={star.y}
                            r={star.r}
                            fill="#00A452"
                            opacity={star.opacity}
                        />
                    ))}
                </Svg>

                <Svg width={size} height={size}>
                    {/* Background Circle */}
                    <Circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke="#1A202C"
                        strokeWidth={strokeWidth}
                        fill="none"
                    />
                    {/* Progress Circle */}
                    <Circle
                        cx={size / 2}
                        cy={size / 2}
                        r={radius}
                        stroke="#00A452"
                        strokeWidth={strokeWidth}
                        fill="none"
                        strokeDasharray={circumference}
                        strokeDashoffset={strokeDashoffset}
                        strokeLinecap="round"
                        transform={`rotate(-90 ${size / 2} ${size / 2})`}
                    />
                </Svg>

                {/* Center Content */}
                <View className="absolute items-center">
                    <Ionicons name="flash" size={40} color="#F59E0B" style={{ marginBottom: 16 }} />
                    <View className="flex-row items-baseline">
                        <Text className="text-white text-6xl font-bold">{kwh}</Text>
                        <Text className="text-white text-xl ml-2">kWh</Text>
                    </View>
                    <Text className="text-text-secondary text-lg mt-2">Energy</Text>
                </View>
            </View>
        </View>
    );
}
