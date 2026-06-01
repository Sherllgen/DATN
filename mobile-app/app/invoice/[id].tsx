import React, { useEffect, useState, useCallback } from 'react';
import { View, Text, TouchableOpacity, ScrollView, ActivityIndicator, Alert, Linking, AppState } from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { SafeAreaView } from 'react-native-safe-area-context';
import GradientBackground from '@/components/ui/GradientBackground';
import { Ionicons } from '@expo/vector-icons';
import { getInvoiceById, payInvoice } from '@/apis/invoiceApi';
import { checkUnpaidInvoices } from '@/apis/paymentApi';
import { useUserStore } from '@/contexts/user.store';
import { InvoiceResponse } from '@/types/invoice.types';

export default function InvoiceDetailScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const router = useRouter();
    
    const [invoice, setInvoice] = useState<InvoiceResponse | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isProcessingPayment, setIsProcessingPayment] = useState(false);

    const fetchInvoiceDetail = useCallback(async (showLoading = false) => {
        if (!id) return;
        try {
            if (showLoading) {
                setIsLoading(true);
            }
            const data = await getInvoiceById(Number(id));
            setInvoice(data);

            // Also check unpaid invoices globally and sync store
            const hasUnpaid = await checkUnpaidInvoices();
            useUserStore.getState().setUnpaidCount(hasUnpaid ? 1 : 0);
        } catch (error) {
            console.error("Failed to fetch invoice details:", error);
            if (showLoading) {
                Alert.alert("Error", "Failed to fetch invoice details.");
            }
        } finally {
            if (showLoading) {
                setIsLoading(false);
            }
        }
    }, [id]);

    useEffect(() => {
        fetchInvoiceDetail(true);
    }, [fetchInvoiceDetail]);

    useEffect(() => {
        const subscription = AppState.addEventListener('change', (nextAppState) => {
            if (nextAppState === 'active') {
                fetchInvoiceDetail(false);
            }
        });
        return () => {
            subscription.remove();
        };
    }, [fetchInvoiceDetail]);

    const handlePayAction = async () => {
        if (isProcessingPayment || !invoice) return;
        
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

    if (isLoading) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 justify-center items-center">
                    <ActivityIndicator size="large" color="#4CAF50" />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    if (!invoice) {
        return (
            <GradientBackground preset="main">
                <SafeAreaView className="flex-1 px-4 pt-4">
                    <View className="flex-row items-center mb-6">
                        <TouchableOpacity 
                            onPress={() => router.back()} 
                            className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-4"
                        >
                            <Ionicons name="arrow-back" size={24} color="white" />
                        </TouchableOpacity>
                        <Text className="text-white font-bold text-2xl">Invoice Not Found</Text>
                    </View>
                </SafeAreaView>
            </GradientBackground>
        );
    }

    const isUnpaid = invoice.status === 'PENDING';

    return (
        <GradientBackground preset="main">
            <SafeAreaView className="flex-1 px-4 pt-4" edges={['top', 'left', 'right', 'bottom']}>
                <View className="flex-row items-center mb-6">
                    <TouchableOpacity 
                        onPress={() => router.back()} 
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-4"
                    >
                        <Ionicons name="arrow-back" size={24} color="white" />
                    </TouchableOpacity>
                    <Text className="text-white font-bold text-2xl">Invoice Details</Text>
                </View>

                <ScrollView showsVerticalScrollIndicator={false} className="flex-1">
                    {/* Main Receipt Card */}
                    <View className="bg-[#1A2634] rounded-3xl p-6 border border-white/10 shadow-xl mb-6">
                        {/* Header icon */}
                        <View className="items-center mb-6">
                            <View className="w-16 h-16 rounded-full bg-primary/20 items-center justify-center mb-4">
                                <Ionicons name="receipt" size={32} color="#4CAF50" />
                            </View>
                            <Text className="text-white/60 font-medium mb-1">Total Amount</Text>
                            <Text className="text-white font-bold text-4xl tracking-tight">
                                {invoice.totalCost.toLocaleString('vi-VN')} ₫
                            </Text>
                        </View>

                        <View className="h-[1px] bg-white/10 w-full mb-6" />

                        {/* Details Rows */}
                        <View className="space-y-4">
                            <View className="flex-row justify-between items-center">
                                <Text className="text-white/60">Invoice No</Text>
                                <Text className="text-white font-medium">#{invoice.number}</Text>
                            </View>
                            
                            <View className="flex-row justify-between items-center">
                                <Text className="text-white/60">Status</Text>
                                <View className={`px-3 py-1 rounded-full ${isUnpaid ? 'bg-red-500/20' : 'bg-green-500/20'}`}>
                                    <Text className={`text-xs font-bold ${isUnpaid ? 'text-red-400' : 'text-green-400'}`}>
                                        {isUnpaid ? 'UNPAID' : 'PAID'}
                                    </Text>
                                </View>
                            </View>

                            <View className="flex-row justify-between items-center">
                                <Text className="text-white/60">Date</Text>
                                <Text className="text-white font-medium">
                                    {new Date(invoice.createdAt.endsWith('Z') ? invoice.createdAt : `${invoice.createdAt}Z`).toLocaleString('en-GB', {
                                        day: '2-digit', month: '2-digit', year: 'numeric',
                                        hour: '2-digit', minute: '2-digit'
                                    })}
                                </Text>
                            </View>

                            <View className="flex-row justify-between items-center">
                                <Text className="text-white/60">Purpose</Text>
                                <Text className="text-white font-medium">{invoice.purpose}</Text>
                            </View>

                            {invoice.bookingId && (
                                <View className="flex-row justify-between items-center">
                                    <Text className="text-white/60">Booking ID</Text>
                                    <Text className="text-white font-medium">#{invoice.bookingId}</Text>
                                </View>
                            )}
                            
                            {invoice.chargingSessionId && (
                                <View className="flex-row justify-between items-center">
                                    <Text className="text-white/60">Session ID</Text>
                                    <Text className="text-white font-medium">#{invoice.chargingSessionId}</Text>
                                </View>
                            )}
                        </View>
                    </View>

                    {/* Pay Button */}
                    {isUnpaid && (
                        <TouchableOpacity 
                            className={`py-4 rounded-2xl items-center flex-row justify-center space-x-2 ${isProcessingPayment ? 'bg-primary/50' : 'bg-primary'}`}
                            activeOpacity={0.8}
                            onPress={handlePayAction}
                            disabled={isProcessingPayment}
                        >
                            {isProcessingPayment ? (
                                <ActivityIndicator size="small" color="white" />
                            ) : (
                                <>
                                    <Ionicons name="card" size={24} color="white" />
                                    <Text className="text-white font-bold text-lg ml-2">Pay Invoice</Text>
                                </>
                            )}
                        </TouchableOpacity>
                    )}
                </ScrollView>
            </SafeAreaView>
        </GradientBackground>
    );
}
