import React, { useState, useCallback } from "react";
import { View, ScrollView, ActivityIndicator, Text, RefreshControl, Linking, Alert } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import BookingTabs, { TabName } from "@/components/booking/BookingTabs";
import BookingCard from "@/components/booking/BookingCard";
import { useRouter, useFocusEffect } from "expo-router";
import { getMyBookings, cancelBooking } from "@/apis/bookingApi";
import { BookingResponse, BookingStatus } from "@/types/booking.types";
import { getInvoiceByBookingId, createZaloPayOrder } from "@/apis/paymentApi";
import { useUserStore } from "@/contexts/user.store";
import GuestPlaceholder from "@/components/auth/GuestPlaceholder";

export default function BookingPage() {
    const router = useRouter();
    const { user } = useUserStore();
    const [activeTab, setActiveTab] = useState<TabName>("Upcoming");
    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isPaying, setIsPaying] = useState(false);
    const [isCancelling, setIsCancelling] = useState(false);

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
            if (user) {
                fetchBookings();
            }
        }, [fetchBookings, user])
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
                amount: invoice.totalCost,
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

    const handleCancel = (booking: BookingResponse) => {
        Alert.alert(
            "Cancel Booking",
            "Are you sure you want to cancel this booking? This action cannot be undone.",
            [
                { text: "Keep Booking", style: "cancel" },
                {
                    text: "Yes, Cancel",
                    style: "destructive",
                    onPress: async () => {
                        if (isCancelling) return;
                        try {
                            setIsCancelling(true);
                            await cancelBooking(booking.id);
                            Alert.alert("Success", "Your booking has been cancelled.");
                            await fetchBookings();
                        } catch (err: any) {
                            const errorCode = err?.response?.data?.message;
                            if (errorCode === "BOOKING_CANCELLATION_NOT_ALLOWED") {
                                Alert.alert(
                                    "Cannot Cancel",
                                    "This booking can no longer be cancelled. Cancellations must be made more than 2 hours before the scheduled start time."
                                );
                            } else {
                                Alert.alert("Error", errorCode || "Unable to cancel booking. Please try again.");
                            }
                        } finally {
                            setIsCancelling(false);
                        }
                    }
                }
            ]
        );
    };

    if (!user) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView style={{ flex: 1 }} edges={["top", "left", "right"]}>
                    <AppHeader title="My Bookings" showBack={false} />
                    <GuestPlaceholder
                        title="Your Bookings"
                        description="Sign in to view and manage your charging station reservations."
                        icon="calendar-outline"
                    />
                </SafeAreaView>
            </GradientBackground>
        );
    }

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
                                onCancel={() => handleCancel(booking)}
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
