import React, { useState, useEffect, useCallback } from "react";
import { View, Text, FlatList, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

import GradientBackground from "@/components/ui/GradientBackground";
import AppHeader from "@/components/ui/AppHeader";
import HistoryItem from "@/components/home/HistoryItem";
import { getChargingSessionHistory } from "@/apis/chargingApi";
import { useUserStore } from "@/contexts/user.store";
import { ChargingSessionStatus, ChargingSessionResponse } from "@/types/charging.types";

const PAGE_SIZE = 10;

export default function ChargingHistoryPage() {
    const user = useUserStore((state) => state.user);
    const [displayedSessions, setDisplayedSessions] = useState<ChargingSessionResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [loadingMore, setLoadingMore] = useState(false);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);

    const fetchSessions = async (pageNumber: number, isLoadMore = false) => {
        if (!user?.id) return;
        try {
            if (isLoadMore) {
                setLoadingMore(true);
            } else {
                setLoading(true);
            }
            
            const response = await getChargingSessionHistory("COMPLETED", pageNumber, PAGE_SIZE);
            const completed = response.content;
            
            if (isLoadMore) {
                setDisplayedSessions(prev => [...prev, ...completed]);
            } else {
                setDisplayedSessions(completed);
            }
            
            setHasMore(!response.last);
            setPage(pageNumber);
        } catch (err) {
            console.error("Failed to fetch charging history:", err);
        } finally {
            setLoading(false);
            setLoadingMore(false);
        }
    };

    useEffect(() => {
        fetchSessions(0);
    }, [user?.id]);

    const loadMore = useCallback(() => {
        if (!loadingMore && hasMore) {
            fetchSessions(page + 1, true);
        }
    }, [page, hasMore, loadingMore]);

    const renderItem = useCallback(
        ({ item }: { item: ChargingSessionResponse }) => {
            const dateObj = new Date(item.startTime || item.createdAt || 0);
            const dateStr = `${dateObj.getDate()} ${dateObj.toLocaleString("en-us", {
                month: "short",
            })} ${dateObj.getFullYear()}`;

            let duration = "0 min";
            if (item.startTime && item.endTime) {
                const diff =
                    new Date(item.endTime).getTime() -
                    new Date(item.startTime).getTime();
                const totalMins = Math.floor(diff / 60000);
                const h = Math.floor(totalMins / 60);
                const m = totalMins % 60;
                duration = h > 0 ? `${h}h ${m}m` : `${m} min`;
            }

            return (
                <HistoryItem
                    date={dateStr}
                    duration={duration}
                    energy={item.totalKwh || 0}
                />
            );
        },
        []
    );



    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }}>
                <AppHeader title="Charging History" showBack />

                {loading ? (
                    <View className="flex-1 items-center justify-center">
                        <ActivityIndicator color="#00A452" size="large" />
                    </View>
                ) : displayedSessions.length === 0 ? (
                    <View className="flex-1 items-center justify-center px-6">
                        <Text className="text-white/50 text-base text-center">
                            No charging history yet.{"\n"}Your completed sessions will appear here.
                        </Text>
                    </View>
                ) : (
                    <FlatList
                        data={displayedSessions}
                        renderItem={renderItem}
                        keyExtractor={(item) => item.id.toString()}
                        contentContainerStyle={{ paddingHorizontal: 16, paddingBottom: 24 }}
                        showsVerticalScrollIndicator={false}
                        onEndReached={hasMore ? loadMore : undefined}
                        onEndReachedThreshold={0.3}
                        ListFooterComponent={
                            hasMore ? (
                                <View className="py-4 items-center">
                                    <ActivityIndicator color="#00A452" size="small" />
                                </View>
                            ) : (
                                <View className="py-4 items-center">
                                    <Text className="text-white/30 text-sm">
                                        You've reached the end
                                    </Text>
                                </View>
                            )
                        }
                    />
                )}
            </SafeAreaView>
        </GradientBackground>
    );
}
