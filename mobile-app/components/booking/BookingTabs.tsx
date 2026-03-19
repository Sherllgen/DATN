import React from "react";
import { View, Text, TouchableOpacity, ScrollView } from "react-native";
import { BookingStatus } from "@/data/bookingData";

interface BookingTabsProps {
    activeTab: BookingStatus;
    onTabChange: (tab: BookingStatus) => void;
}

const tabs: BookingStatus[] = ["Upcoming", "Completed", "Cancelled"];

const BookingTabs = ({ activeTab, onTabChange }: BookingTabsProps) => {
    return (
        <View className="border-b border-white/10 mb-6">
            <View className="flex-row px-4">
                {tabs.map((tab) => {
                    const isActive = activeTab === tab;
                    return (
                        <TouchableOpacity
                            key={tab}
                            onPress={() => onTabChange(tab)}
                            activeOpacity={0.7}
                            className="flex-1 items-center py-4 relative"
                        >
                            <Text
                                className={`text-base font-medium ${isActive ? "text-secondary" : "text-white/40"
                                    }`}
                            >
                                {tab}
                            </Text>
                            {isActive && (
                                <View
                                    className="absolute bottom-0 left-0 right-0 h-1 bg-secondary rounded-t-full"
                                />
                            )}
                        </TouchableOpacity>
                    );
                })}
            </View>
        </View>
    );
};

export default BookingTabs;
