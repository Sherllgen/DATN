import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { router } from 'expo-router';
import BatteryIndicator from '@/components/ui/BatteryIndicator';
import { useChargingStore } from '@/stores/chargingStore';
import { ChargingSessionStatus } from '@/types/charging.types';

export default function ActiveChargingNotification() {
  const activeSession = useChargingStore(state => state.activeSession);

  if (!activeSession || activeSession.status !== ChargingSessionStatus.CHARGING) {
    return null;
  }

  return (
    <TouchableOpacity
      activeOpacity={0.8}
      onPress={() => router.push('/charging')}
      className="bg-black/85 border border-white/15 rounded-3xl p-5 flex-row justify-between items-center shadow-lg shadow-white/15"
    >
        <View className="flex-row items-center flex-1">
           {/* Fallback to 0 if we don't have battery percentage from global store yet */}
          <BatteryIndicator percentage={0} /> 
        </View>

      <View className="items-end justify-center">
        <Text className="text-[#A0AEC0] text-[13px] mb-1">Status</Text>
        <Text className="text-white font-bold text-[18px]">
          Charging in progress
        </Text>
      </View>
    </TouchableOpacity>
  );
}
