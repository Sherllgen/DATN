import React, { useState } from "react";
import { View, ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import BookingTabs from "@/components/booking/BookingTabs";
import BookingCard from "@/components/booking/BookingCard";
import { useRouter } from "expo-router";
import { mockBookings, BookingStatus } from "@/data/bookingData";

export default function BookingPage() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState<BookingStatus>("Upcoming");

    const filteredBookings = mockBookings.filter(
        (booking) => booking.status === activeTab
    );

    return (
        <GradientBackground preset="main">
            <SafeAreaView className="flex-1" edges={["top", "left", "right"]}>
                <AppHeader title="My Booking" showBack />

                <BookingTabs
                    activeTab={activeTab}
                    onTabChange={setActiveTab}
                />

                <ScrollView
                    className="flex-1 px-4"
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ paddingBottom: 20 }}
                >
                    {filteredBookings.map((booking) => (
                        <BookingCard
                            key={booking.id}
                            booking={booking}
                            onCancel={() => router.push({ pathname: "/booking/[id]", params: { id: booking.id } })}
                            onView={() => router.push({ pathname: "/booking/[id]", params: { id: booking.id } })}
                        />
                    ))}
                </ScrollView>
            </SafeAreaView>
        </GradientBackground>
    );
}
