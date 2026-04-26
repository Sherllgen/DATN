import React, { useState, useCallback } from "react";
import { View, ScrollView, ActivityIndicator, Text, RefreshControl, Linking, Alert } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import BookingTabs, { TabName } from "@/components/booking/BookingTabs";
import BookingCard from "@/components/booking/BookingCard";
import { useRouter, useFocusEffect } from "expo-router";
import { getMyBookings } from "@/apis/bookingApi";
import { BookingResponse, BookingStatus } from "@/types/booking.types";
import { getInvoiceByBookingId, createZaloPayOrder } from "@/apis/paymentApi";

export default function BookingPage() {
    const router = useRouter();
    const [activeTab, setActiveTab] = useState<TabName>("Upcoming");
    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isPaying, setIsPaying] = useState(false);

    const fetchBookings = useCallback(async () => {
        try {
            setIsLoading(true);
            const data = await getMyBookings();
            // Sort by startTime descending (newest first)
            data.sort((a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime());
            setBookings(data);
        } catch (error) {
            console.error("Failed to fetch bookings:", error);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useFocusEffect(
        useCallback(() => {
            fetchBookings();
        }, [fetchBookings])
    );

    const filteredBookings = bookings.filter((booking) => {
        if (activeTab === "Pending") return booking.status === BookingStatus.PENDING;
        if (activeTab === "Upcoming") return booking.status === BookingStatus.CONFIRMED || booking.status === BookingStatus.IN_PROGRESS;
        if (activeTab === "Completed") return booking.status === BookingStatus.COMPLETED;
        if (activeTab === "Cancelled") return booking.status === BookingStatus.CANCELLED;
        return false;
    });

    const handlePayNow = async (booking: BookingResponse) => {
        if (isPaying) return;
        try {
            setIsPaying(true);
            const invoice = await getInvoiceByBookingId(booking.id);
            const order = await createZaloPayOrder({
                invoiceId: invoice.id,
                userId: booking.userId,
                amount: booking.totalPrice || 0,
                description: `EV-Go Booking ${booking.id}`
            });

            if (order.orderUrl) {
                await Linking.openURL(order.orderUrl);
            } else {
                Alert.alert("Error", "Could not acquire a payment URL from ZaloPay.");
            }
        } catch (err: any) {
            Alert.alert("Payment Error", err?.response?.data?.message || "Something went wrong during payment processing.");
        } finally {
            setIsPaying(false);
        }
    };

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }} edges={["top", "left", "right"]}>
                <AppHeader title="My Bookings" showBack />

                <BookingTabs
                    activeTab={activeTab}
                    onTabChange={setActiveTab}
                />

                <ScrollView
                    style={{ flex: 1 }}
                    className="px-4"
                    showsVerticalScrollIndicator={false}
                    contentContainerStyle={{ flexGrow: 1, paddingBottom: 20 }}
                    refreshControl={
                        <RefreshControl refreshing={isLoading && bookings.length > 0} onRefresh={fetchBookings} tintColor="#00A452" />
                    }
                >
                    {isLoading && bookings.length === 0 ? (
                        <View className="flex-1 justify-center items-center py-20 mt-20">
                            <ActivityIndicator size="large" color="#00A452" />
                            <Text className="text-white mt-4">Loading bookings...</Text>
                        </View>
                    ) : filteredBookings.length === 0 ? (
                        <View className="flex-1 justify-center items-center py-20 mt-20">
                            <Text className="text-white/50 text-base">No {activeTab.toLowerCase()} bookings found.</Text>
                        </View>
                    ) : (
                        filteredBookings.map((booking) => (
                            <BookingCard
                                key={booking.id}
                                booking={booking}
                                onCancel={() => { }}
                                onView={() => router.push(`/booking/${booking.id}` as any)}
                                onPay={() => handlePayNow(booking)}
                            />
                        ))
                    )}
                </ScrollView>
            </SafeAreaView>
        </GradientBackground>
    );
}
