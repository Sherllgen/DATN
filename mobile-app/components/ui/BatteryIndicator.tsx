import React from 'react';
import { View, Text } from 'react-native';
import { Ionicons } from '@expo/vector-icons';

interface BatteryIndicatorProps {
  percentage: number;
}

export default function BatteryIndicator({ percentage }: BatteryIndicatorProps) {
  // Ensure percentage is between 0 and 100
  const clampedPercentage = Math.max(0, Math.min(100, percentage));

  return (
    <View className="flex-row items-center">
      {/* Main Battery Body */}
      <View className="h-10 w-[84px] border-[2px] border-white rounded-[10px] overflow-hidden relative justify-center bg-[#1A1D24]">
        {/* Fill Level */}
        <View
          className="absolute left-0 top-0 bottom-0 bg-secondary"
          style={{ width: `${clampedPercentage}%` }}
        />
        {/* Text inside */}
        <View className="absolute inset-0 flex-row items-center justify-center -ml-1">
          <Text className="text-white font-bold text-body-lg">{clampedPercentage}</Text>
          <Ionicons name="flash" size={18} color="white" />
        </View>
      </View>
      {/* Battery Nub */}
      <View className="h-4 w-[4px] bg-white rounded-r-[3px] -ml-[1px]" />
    </View>
  );
}
