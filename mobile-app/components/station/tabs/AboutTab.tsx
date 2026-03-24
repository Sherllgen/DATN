import React from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { Station } from "@/types/station.types";

interface AboutTabProps {
    station: Station;
}

const AboutTab = ({ station }: AboutTabProps) => {
    // Mock data for phone and website
    const phoneNumber = "(+84) 123 456 789";
    const website = "www.evgo-station.com";

    return (
        <View>
            {/* Contact Info - ONLY Phone and Website as per user feedback */}
            <View className="mb-8">
                <Text className="text-lg font-semibold text-white mb-4">
                    Contact Information
                </Text>
                <View className="bg-white/5 rounded-2xl p-4 border border-white/10 gap-y-4">
                    <TouchableOpacity className="flex-row items-center" activeOpacity={0.7}>
                        <View className="w-10 h-10 rounded-full bg-blue-500/20 items-center justify-center mr-4">
                            <Ionicons name="call-outline" size={20} color="#3B82F6" />
                        </View>
                        <View>
                            <Text className="text-xs text-[#9BA1A6] mb-0.5">Phone Number</Text>
                            <Text className="text-base text-white font-medium">{phoneNumber}</Text>
                        </View>
                    </TouchableOpacity>

                    <View className="h-[1px] bg-white/5 ml-14" />

                    <TouchableOpacity className="flex-row items-center" activeOpacity={0.7}>
                        <View className="w-10 h-10 rounded-full bg-purple-500/20 items-center justify-center mr-4">
                            <Ionicons name="globe-outline" size={20} color="#A855F7" />
                        </View>
                        <View>
                            <Text className="text-xs text-[#9BA1A6] mb-0.5">Website</Text>
                            <Text className="text-base text-white font-medium">{website}</Text>
                        </View>
                    </TouchableOpacity>
                </View>
            </View>
        </View>
    );
};

export default AboutTab;
