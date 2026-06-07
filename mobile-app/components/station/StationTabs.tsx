import React, { useState } from "react";
import { View, Text, TouchableOpacity } from "react-native";
import { Station, PriceSettingResponse } from "@/types/station.types";
import InfoTab from "@/components/station/tabs/InfoTab";
import ReviewsTab from "@/components/station/tabs/ReviewsTab";
import AboutTab from "@/components/station/tabs/AboutTab";

export type StationTabType = "Info" | "Reviews" | "About";

interface StationTabsProps {
    station: Station;
    priceSetting?: PriceSettingResponse | null;
    activeTab?: StationTabType;
    onTabChange?: (tab: StationTabType) => void;
}

const tabs: StationTabType[] = ["Info", "Reviews", "About"];

const StationTabs = ({ station, priceSetting, activeTab: propActiveTab, onTabChange }: StationTabsProps) => {
    const [localActiveTab, setLocalActiveTab] = useState<StationTabType>("Info");
    const activeTab = propActiveTab || localActiveTab;

    const handleTabChange = (tab: StationTabType) => {
        setLocalActiveTab(tab);
        if (onTabChange) onTabChange(tab);
    };

    const renderTabContent = () => {
        switch (activeTab) {
            case "Info":
                return <InfoTab station={station} priceSetting={priceSetting} />;
            case "Reviews":
                return <ReviewsTab stationId={station.id} />;
            case "About":
                return <AboutTab station={station} />;
            default:
                return null;
        }
    };

    return (
        <View className="flex-1">
            {/* Tab Bar */}
            <View className="border-b border-white/10 mb-6">
                <View className="flex-row px-0">
                    {tabs.map((tab) => {
                        const isActive = activeTab === tab;
                        return (
                            <TouchableOpacity
                                key={tab}
                                onPress={() => handleTabChange(tab)}
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

            {/* Tab Content */}
            <View className="min-h-[300px]">
                {renderTabContent()}
            </View>
        </View>
    );
};

export default StationTabs;
