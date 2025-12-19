import BillingGroupSection from "@/components/notifice/BillingGroupSection";
import EmptyState from "@/components/notifice/EmptyState";
import { mockData } from "@/components/notifice/mockData";
import TabHeader from "@/components/notifice/TabHeader";
import { LinearGradient } from "expo-linear-gradient";
import React, { useState } from "react";
import { ScrollView, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

export default function NotificePage() {
    const [activeTab, setActiveTab] = useState<"payment" | "system">("payment");

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 pt-6 pb-[80px]"
        >
            <SafeAreaView className="flex-1">
                <TabHeader activeTab={activeTab} onTabChange={setActiveTab} />

                <ScrollView className="flex-1">
                    {activeTab === "payment" ? (
                        <View className="py-4">
                            {mockData.map((group, index) => (
                                <BillingGroupSection
                                    key={index}
                                    group={group}
                                />
                            ))}
                        </View>
                    ) : (
                        <EmptyState message="No system notifications" />
                    )}
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
