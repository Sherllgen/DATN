import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { router } from 'expo-router';
import BatteryIndicator from '@/components/ui/BatteryIndicator';
import { ChargingProcessData } from '@/data/chargingData';

interface Props {
  data: ChargingProcessData;
}

export default function ActiveChargingNotification({ data }: Props) {
  const timeRemaining = data.remainingMinutes ? `${data.remainingMinutes} minutes` : '0 minutes';

  return (
    <TouchableOpacity
      activeOpacity={0.8}
      onPress={() => router.push('/charging')}
      className="bg-black/85 border border-white/15 rounded-3xl p-5 flex-row justify-between items-center shadow-lg shadow-white/15"
    >
      <View className="flex-row items-center flex-1">
        <BatteryIndicator percentage={data.batteryPercent} />
      </View>

      <View className="items-end justify-center">
        <Text className="text-[#A0AEC0] text-[13px] mb-1">Time remaining</Text>
        <Text className="text-white font-bold text-[18px]">
          {timeRemaining}
        </Text>
      </View>
    </TouchableOpacity>
  );
}
