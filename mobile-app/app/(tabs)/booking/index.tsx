import React, { useState, useCallback } from "react";
import { View, FlatList, ActivityIndicator, Text, Linking, Alert } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import BookingTabs, { TabName } from "@/components/booking/BookingTabs";
import BookingCard from "@/components/booking/BookingCard";
import { useRouter, useFocusEffect } from "expo-router";
import { getMyBookings, cancelBooking } from "@/apis/bookingApi";
import { BookingResponse } from "@/types/booking.types";
import { getInvoiceByBookingId, createZaloPayOrder } from "@/apis/paymentApi";
import { useUserStore } from "@/contexts/user.store";
import GuestPlaceholder from "@/components/auth/GuestPlaceholder";

export default function BookingPage() {
    const router = useRouter();
    const { user } = useUserStore();
    const [activeTab, setActiveTab] = useState<TabName>("Upcoming");
    const [bookings, setBookings] = useState<BookingResponse[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [isPaying, setIsPaying] = useState(false);
    const [isCancelling, setIsCancelling] = useState(false);

    const fetchBookings = useCallback(async (
        tab: TabName,
        pageNum: number,
        shouldRefresh = false
    ) => {
        try {
            if (pageNum === 0) {
                if (shouldRefresh) {
                    setIsRefreshing(true);
                } else {
                    setIsLoading(true);
                    setBookings([]); // Clear old data to trigger the loading indicator
                }
            } else {
                setIsLoadingMore(true);
            }

            // Map tab to statuses
            let statuses: string[] = [];
            if (tab === "Pending") {
                statuses = ["PENDING"];
            } else if (tab === "Upcoming") {
                statuses = ["CONFIRMED", "IN_PROGRESS"];
            } else if (tab === "Completed") {
                statuses = ["COMPLETED"];
            } else if (tab === "Cancelled") {
                statuses = ["CANCELLED"];
            }

            const pageResponse = await getMyBookings(pageNum, 5, statuses);

            if (pageNum === 0) {
                setBookings(pageResponse.content);
            } else {
                setBookings((prev) => [...prev, ...pageResponse.content]);
            }

            setHasMore(!pageResponse.last);
            setPage(pageNum);
        } catch (error) {
            console.error("Failed to fetch bookings:", error);
            Alert.alert("Error", "Failed to fetch bookings. Please try again later.");
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
            setIsLoadingMore(false);
        }
    }, []);

    useFocusEffect(
        useCallback(() => {
            if (user) {
                fetchBookings(activeTab, 0);
            }
        }, [fetchBookings, user, activeTab])
    );

    const handleRefresh = () => {
        if (user) {
            fetchBookings(activeTab, 0, true);
        }
    };

    const handleLoadMore = () => {
        if (!isLoading && !isRefreshing && !isLoadingMore && hasMore && user) {
            fetchBookings(activeTab, page + 1);
        }
    };

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
                            await fetchBookings(activeTab, 0);
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

                <FlatList
                    style={{ flex: 1 }}
                    className="px-4"
                    data={bookings}
                    keyExtractor={(item) => item.id.toString()}
                    renderItem={({ item }) => (
                        <BookingCard
                            booking={item}
                            onCancel={() => handleCancel(item)}
                            onView={() => router.push(`/booking/${item.id}` as any)}
                            onPay={() => handlePayNow(item)}
                        />
                    )}
                    ListEmptyComponent={() => {
                        if (isLoading) {
                            return (
                                <View className="flex-1 justify-center items-center py-20 mt-20">
                                    <ActivityIndicator size="large" color="#00A452" />
                                    <Text className="text-white mt-4">Loading bookings...</Text>
                                </View>
                            );
                        }
                        return (
                            <View className="flex-1 justify-center items-center py-20 mt-20">
                                <Text className="text-white/50 text-base">No {activeTab.toLowerCase()} bookings found.</Text>
                            </View>
                        );
                    }}
                    contentContainerStyle={{ flexGrow: 1, paddingBottom: 20 }}
                    showsVerticalScrollIndicator={false}
                    onRefresh={handleRefresh}
                    refreshing={isRefreshing}
                    onEndReached={handleLoadMore}
                    onEndReachedThreshold={0.5}
                    ListFooterComponent={() => (
                        isLoadingMore ? (
                            <ActivityIndicator size="small" color="#00A452" className="my-4" />
                        ) : null
                    )}
                />
            </SafeAreaView>
        </GradientBackground>
    );
}
