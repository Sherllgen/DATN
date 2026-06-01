import React, { useEffect, useState } from "react";
import { Modal, View, Text, ActivityIndicator, Image, Linking, ScrollView } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Button from "@/components/ui/Button";
import Animated, { useSharedValue, useAnimatedStyle, withSpring, withTiming } from 'react-native-reanimated';
import { getInvoiceByChargingSessionId, createZaloPayOrder, InvoiceResponse } from "@/apis/paymentApi";
import { Toast } from "toastify-react-native";
import { useUserStore } from "@/contexts/user.store";
import { ChargingSessionResponse } from "@/types/charging.types";

export interface SessionDetailsModalProps {
    showModal: boolean;
    session: ChargingSessionResponse | null;
    duration: string;
    date: string;
    onDismiss: (wasPaid?: boolean) => void;
}

export default function SessionDetailsModal({ showModal, session, duration, date, onDismiss }: SessionDetailsModalProps) {
    const translateY = useSharedValue(1000);
    const [loadingInvoice, setLoadingInvoice] = useState(false);
    const [invoice, setInvoice] = useState<InvoiceResponse | null>(null);
    const [paying, setPaying] = useState(false);
    const user = useUserStore(state => state.user);

    useEffect(() => {
        if (showModal && session) {
            translateY.value = withSpring(0, { damping: 20, stiffness: 90 });
            fetchInvoice(session.id);
        } else {
            translateY.value = withTiming(1000, { duration: 300 });
            setInvoice(null);
        }
    }, [showModal, session]);

    const fetchInvoice = async (id: number) => {
        try {
            setLoadingInvoice(true);
            const data = await getInvoiceByChargingSessionId(id);
            setInvoice(data);
        } catch (error: any) {
            if (error?.response?.status === 404) {
                console.log(`Invoice not found for session ${id} (Status 404). Free session.`);
            } else {
                console.log("Failed to fetch invoice", error);
            }
            setInvoice(null);
        } finally {
            setLoadingInvoice(false);
        }
    };

    const handlePay = async () => {
        if (!invoice || !user) return;
        try {
            setPaying(true);
            const data = await createZaloPayOrder({
                amount: Math.round(invoice.totalCost),
                description: `EV-Go Payment for Invoice ${invoice.number}`,
                invoiceId: invoice.id,
                userId: Number(user.id)
            });
            if (data.orderUrl) {
                Linking.openURL(data.orderUrl);
            }
            onDismiss(true);
        } catch (error: any) {
            Toast.error(error?.response?.data?.message || "Failed to create order");
        } finally {
            setPaying(false);
        }
    };

    const animatedStyle = useAnimatedStyle(() => {
        return {
            transform: [{ translateY: translateY.value }],
        };
    });

    if (!showModal && translateY.value === 1000) return null;

    // Parse times
    const formatTime = (timeStr: string | null) => {
        if (!timeStr) return "--:--";
        const dateObj = new Date(timeStr.endsWith("Z") ? timeStr : `${timeStr}Z`);
        return dateObj.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
    };

    const startTime = session ? formatTime(session.startTime) : "--:--";
    const endTime = session ? formatTime(session.endTime) : "--:--";

    return (
        <Modal visible={showModal} transparent animationType="none">
            <View className="flex-1 bg-black/60 justify-end">
                <Animated.View style={animatedStyle} className="bg-[#1A202C] rounded-t-3xl pt-2 pb-8 w-full max-h-[90%]">
                    {/* Swipe indicator */}
                    <View className="items-center mb-4">
                        <View className="w-12 h-1 rounded-full bg-[#4A5568]" />
                    </View>

                    <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: 24, paddingBottom: 40 }}>
                        <View className="items-center mb-6">
                            <View className="w-16 h-16 rounded-full bg-secondary/10 items-center justify-center mb-3">
                                <Ionicons name="receipt-outline" size={32} color="#00A452" />
                            </View>
                            <Text className="text-white text-2xl font-bold text-center">Session Details</Text>
                            <Text className="text-text-secondary text-sm text-center mt-1">
                                {date} • {duration}
                            </Text>
                        </View>

                        {/* Session Metrics */}
                        <View className="bg-white/5 border border-white/10 rounded-2xl p-4 mb-6 flex-row justify-around">
                            <View className="items-center">
                                <Ionicons name="time-outline" size={24} color="#8B5CF6" />
                                <Text className="text-text-secondary text-xs mt-1">Start Time</Text>
                                <Text className="text-white font-semibold mt-0.5">{startTime}</Text>
                            </View>
                            <View className="w-[1px] bg-white/10 h-10 self-center" />
                            <View className="items-center">
                                <Ionicons name="log-out-outline" size={24} color="#EF4444" />
                                <Text className="text-text-secondary text-xs mt-1">End Time</Text>
                                <Text className="text-white font-semibold mt-0.5">{endTime}</Text>
                            </View>
                            <View className="w-[1px] bg-white/10 h-10 self-center" />
                            <View className="items-center">
                                <Ionicons name="flash-outline" size={24} color="#F59E0B" />
                                <Text className="text-text-secondary text-xs mt-1">Energy</Text>
                                <Text className="text-white font-semibold mt-0.5">{(session?.totalKwh || 0).toFixed(2)} kWh</Text>
                            </View>
                        </View>

                        {loadingInvoice ? (
                            <View className="py-8 items-center justify-center">
                                <ActivityIndicator color="#00A452" size="large" />
                                <Text className="text-text-secondary mt-4">Retrieving invoice details...</Text>
                            </View>
                        ) : invoice ? (
                            <>
                                {/* Invoice Summary */}
                                <View className="bg-white/5 border border-white/10 p-4 rounded-2xl mb-6">
                                    <View className="flex-row justify-between mb-3">
                                        <Text className="text-text-secondary text-base">Invoice Number</Text>
                                        <Text className="text-white font-semibold text-base">{invoice.number}</Text>
                                    </View>
                                    <View className="flex-row justify-between mb-3">
                                        <Text className="text-text-secondary text-base">Total Cost</Text>
                                        <Text className="text-white font-semibold text-base">{invoice.totalCost.toLocaleString('vi-VN')} VND</Text>
                                    </View>
                                    <View className="flex-row justify-between items-center mb-1">
                                        <Text className="text-text-secondary text-base">Payment Status</Text>
                                        <View className={`px-3 py-1 rounded-full ${
                                            invoice.status === 'PAID' ? 'bg-[#00A452]/20' : 'bg-[#F59E0B]/20'
                                        }`}>
                                            <Text className={`font-bold text-sm ${
                                                invoice.status === 'PAID' ? 'text-[#00A452]' : 'text-[#F59E0B]'
                                            }`}>
                                                {invoice.status}
                                            </Text>
                                        </View>
                                    </View>
                                </View>

                                {invoice.status === 'PENDING' && (
                                    <>
                                        {/* Payment Method Section */}
                                        <Text className="text-white font-semibold text-base mb-2">Payment Method</Text>
                                        <View className="bg-white/5 border border-white/10 p-4 rounded-2xl flex-row items-center gap-x-3 mb-8">
                                            <Image
                                                source={require("@/assets/images/zalopay.webp")}
                                                style={{ width: 40, height: 40, borderRadius: 8 }}
                                                resizeMode="contain"
                                            />
                                            <Text className="text-white font-semibold text-base flex-1">ZaloPay App</Text>
                                            <Ionicons name="checkmark-circle" size={24} color="#00A452" />
                                        </View>

                                        <View className="gap-y-3">
                                            <Button 
                                                onPress={handlePay}
                                                variant="primary"
                                                className="w-full"
                                                style={{ height: 56 }}
                                                loading={paying}
                                            >
                                                Pay Now ({invoice.totalCost.toLocaleString('vi-VN')} VND)
                                            </Button>
                                        </View>
                                    </>
                                )}
                            </>
                        ) : (
                            <View className="py-8 items-center justify-center bg-white/5 border border-white/10 rounded-2xl mb-6">
                                <Ionicons name="gift-outline" size={40} color="#00A452" className="mb-2" />
                                <Text className="text-white font-semibold text-lg text-center">Free Session</Text>
                                <Text className="text-text-secondary text-sm text-center mt-1 px-4">
                                    This session consumed negligible energy and generated no charges.
                                </Text>
                            </View>
                        )}

                        <View className="mt-4">
                            <Button onPress={() => onDismiss(false)} variant="outline" className="w-full" style={{ height: 56 }}>
                                Close
                            </Button>
                        </View>
                    </ScrollView>
                </Animated.View>
            </View>
        </Modal>
    );
}
