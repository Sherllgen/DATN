import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Linking, Alert, ActivityIndicator, AppState, AppStateStatus } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import GradientBackground from "@/components/ui/GradientBackground";
import { InvoiceCard, InvoiceCardSkeleton } from '@/components/InvoiceCard';
import { getMyInvoices, payInvoice } from '@/apis/invoiceApi';
import { InvoiceResponse } from '@/types/invoice.types';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useAuthStore } from '@/contexts/auth.store';
import GuestPlaceholder from '@/components/auth/GuestPlaceholder';

type TabType = 'UNPAID' | 'PAID';

export default function PaymentPage() {
    const router = useRouter();
    const { accessToken } = useAuthStore();
    const [activeTab, setActiveTab] = useState<TabType>('UNPAID');
    const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [isProcessingPayment, setIsProcessingPayment] = useState(false);

    const fetchInvoices = useCallback(async (tab: TabType, pageNum: number, shouldRefresh = false) => {
        try {
            if (pageNum === 0 && !shouldRefresh) {
                setIsLoading(true);
            }
            const response = await getMyInvoices(tab, pageNum, 10);
            
            if (shouldRefresh || pageNum === 0) {
                setInvoices(response.content);
            } else {
                setInvoices(prev => [...prev, ...response.content]);
            }
            
            setHasMore(!response.last);
            setPage(pageNum);
        } catch (error) {
            console.error('Error fetching invoices:', error);
            Alert.alert('Error', 'Failed to fetch invoices. Please try again later.');
        } finally {
            setIsLoading(false);
            setIsRefreshing(false);
        }
    }, []);

    useEffect(() => {
        if (accessToken) {
            fetchInvoices(activeTab, 0);
        }
    }, [activeTab, fetchInvoices, accessToken]);

    // Listen to AppState to refresh UNPAID invoices when returning from ZaloPay
    useEffect(() => {
        const subscription = AppState.addEventListener('change', (nextAppState: AppStateStatus) => {
            if (nextAppState === 'active' && activeTab === 'UNPAID') {
                // Background refresh when coming back to the app
                fetchInvoices('UNPAID', 0, true);
            }
        });

        return () => {
            subscription.remove();
        };
    }, [activeTab, fetchInvoices]);

    const handleRefresh = () => {
        setIsRefreshing(true);
        fetchInvoices(activeTab, 0, true);
    };

    const handleLoadMore = () => {
        if (!isLoading && !isRefreshing && hasMore) {
            fetchInvoices(activeTab, page + 1);
        }
    };

    const handlePayAction = async (invoice: InvoiceResponse) => {
        if (isProcessingPayment) return;
        
        try {
            setIsProcessingPayment(true);
            const orderResponse = await payInvoice(invoice.id);
            
            const canOpen = await Linking.canOpenURL(orderResponse.orderUrl);
            if (canOpen) {
                await Linking.openURL(orderResponse.orderUrl);
            } else {
                Alert.alert('Error', 'ZaloPay app is not installed or cannot be opened.');
            }
        } catch (error: any) {
            console.error('Error initiating payment:', error);
            const message = error.response?.data?.message || 'Failed to initiate payment.';
            Alert.alert('Payment Error', message);
        } finally {
            setIsProcessingPayment(false);
        }
    };

    const renderEmptyComponent = () => {
        if (isLoading) {
            return (
                <View>
                    <InvoiceCardSkeleton />
                    <InvoiceCardSkeleton />
                    <InvoiceCardSkeleton />
                </View>
            );
        }

        return (
            <View className="flex-1 items-center justify-center py-20">
                <Ionicons 
                    name={activeTab === 'UNPAID' ? 'receipt-outline' : 'checkmark-done-circle-outline'} 
                    size={64} 
                    color="rgba(255,255,255,0.2)" 
                />
                <Text className="text-white/50 text-lg mt-4 font-medium">
                    No {activeTab.toLowerCase()} invoices found
                </Text>
            </View>
        );
    };

    if (!accessToken) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 pt-4" edges={["top", "left", "right"]}>
                    <View className="flex-row items-center mb-2 px-4">
                        <Text className="text-white font-bold text-2xl">Payment History</Text>
                    </View>
                    <GuestPlaceholder
                        description="Sign in to view your invoices and payment history."
                        icon="receipt-outline"
                    />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    return (
        <GradientBackground preset="main">
            <SafeAreaView className="flex-1 pt-4" edges={["top", "left", "right"]}>
                {/* Header Title */}
                <View className="flex-row items-center mb-2 px-4">
                    <Text className="text-white font-bold text-2xl">Payment History</Text>
                </View>

                {/* Tab Switcher */}
                <View className="border-b border-white/10 mb-6">
                    <View className="flex-row px-4">
                        {(['UNPAID', 'PAID'] as TabType[]).map((tab) => {
                            const isActive = activeTab === tab;
                            const label = tab === 'UNPAID' ? 'Unpaid' : 'Paid';
                            return (
                                <TouchableOpacity
                                    key={tab}
                                    activeOpacity={0.7}
                                    onPress={() => setActiveTab(tab)}
                                    className="flex-1 items-center py-4 relative"
                                >
                                    <Text
                                        className={`text-base font-medium ${isActive ? "text-secondary" : "text-white/40"
                                            }`}
                                    >
                                        {label}
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

                <FlatList
                    className="px-4"
                    data={invoices}
                    keyExtractor={(item) => item.id.toString()}
                    renderItem={({ item }) => (
                        <InvoiceCard 
                            invoice={item} 
                            onPayAction={handlePayAction} 
                        />
                    )}
                    ListEmptyComponent={renderEmptyComponent}
                    contentContainerStyle={{ paddingBottom: 24, flexGrow: 1 }}
                    showsVerticalScrollIndicator={false}
                    onRefresh={handleRefresh}
                    refreshing={isRefreshing}
                    onEndReached={handleLoadMore}
                    onEndReachedThreshold={0.5}
                    ListFooterComponent={() => (
                        hasMore && !isLoading && invoices.length > 0 ? (
                            <ActivityIndicator size="small" color="#4CAF50" className="my-4" />
                        ) : null
                    )}
                />
            </SafeAreaView>
        </GradientBackground>
    );
}
