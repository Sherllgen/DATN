import React, { useState, useEffect, useCallback } from 'react';
import { View, Text, FlatList, TouchableOpacity, Linking, Alert, ActivityIndicator, AppState, AppStateStatus } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import GradientBackground from "@/components/ui/GradientBackground";
import { InvoiceCard, InvoiceCardSkeleton } from '@/components/InvoiceCard';
import { getMyInvoices, payInvoice } from '@/apis/invoiceApi';
import { InvoiceResponse, PageResponse } from '@/types/invoice.types';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

type TabType = 'UNPAID' | 'PAID';

export default function InvoiceScreen() {
    const router = useRouter();
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
        fetchInvoices(activeTab, 0);
    }, [activeTab, fetchInvoices]);

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

    const renderHeader = () => (
        <View className="mb-6">
            <View className="flex-row items-center mb-4">
                <TouchableOpacity 
                    onPress={() => router.back()} 
                    className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-4"
                >
                    <Ionicons name="arrow-back" size={24} color="white" />
                </TouchableOpacity>
                <Text className="text-white font-bold text-2xl">My Invoices</Text>
            </View>

            {/* Custom Tab Switcher */}
            <View className="flex-row bg-[#1A2634] rounded-xl p-1 border border-white/10">
                <TouchableOpacity
                    activeOpacity={0.7}
                    onPress={() => setActiveTab('UNPAID')}
                    className={`flex-1 py-3 rounded-lg items-center ${activeTab === 'UNPAID' ? 'bg-primary' : 'bg-transparent'}`}
                >
                    <Text className={`font-bold ${activeTab === 'UNPAID' ? 'text-white' : 'text-white/50'}`}>
                        Unpaid
                    </Text>
                </TouchableOpacity>
                <TouchableOpacity
                    activeOpacity={0.7}
                    onPress={() => setActiveTab('PAID')}
                    className={`flex-1 py-3 rounded-lg items-center ${activeTab === 'PAID' ? 'bg-[#10B981]' : 'bg-transparent'}`}
                >
                    <Text className={`font-bold ${activeTab === 'PAID' ? 'text-white' : 'text-white/50'}`}>
                        Paid
                    </Text>
                </TouchableOpacity>
            </View>
        </View>
    );

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

    return (
        <GradientBackground preset="main">
            <SafeAreaView className="flex-1 px-4 pt-4">
                <FlatList
                    data={invoices}
                    keyExtractor={(item) => item.id.toString()}
                    renderItem={({ item }) => (
                        <InvoiceCard 
                            invoice={item} 
                            onPayAction={handlePayAction} 
                        />
                    )}
                    ListHeaderComponent={renderHeader}
                    ListEmptyComponent={renderEmptyComponent}
                    contentContainerStyle={{ paddingBottom: 24 }}
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
