import React, { useEffect, useState } from "react";
import { Modal, View, Text, ActivityIndicator, Image, Linking, ScrollView } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Button from "@/components/ui/Button";
import Animated, { useSharedValue, useAnimatedStyle, withSpring, withTiming } from 'react-native-reanimated';
import { getInvoiceByChargingSessionId, createZaloPayOrder, InvoiceResponse } from "@/apis/paymentApi";
import { Toast } from "toastify-react-native";
import { useUserStore } from "@/contexts/user.store";

export interface ChargingCompleteModalProps {
    showModal: boolean;
    sessionId?: number | null;
    totalCost: number;
    consumedKwh?: number;
    onDismiss: () => void;
}

export default function ChargingCompleteModal({ showModal, sessionId, totalCost, consumedKwh, onDismiss }: ChargingCompleteModalProps) {
    const translateY = useSharedValue(1000);
    const [loadingInvoice, setLoadingInvoice] = useState(false);
    const [invoice, setInvoice] = useState<InvoiceResponse | null>(null);
    const [paying, setPaying] = useState(false);
    const user = useUserStore(state => state.user);

    useEffect(() => {
        if (showModal) {
            translateY.value = withSpring(0, { damping: 20, stiffness: 90 });
            if (sessionId) {
                fetchInvoice(sessionId);
            }
        } else {
            translateY.value = withTiming(1000, { duration: 300 });
            setInvoice(null);
        }
    }, [showModal, sessionId]);

    const fetchInvoice = async (id: number) => {
        try {
            setLoadingInvoice(true);
            const data = await getInvoiceByChargingSessionId(id);
            setInvoice(data);
        } catch (error) {
            console.log("Failed to fetch invoice", error);
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
            onDismiss(); // Redirect user home or to history after initiating payment
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
                            <View className="w-16 h-16 rounded-full bg-secondary/20 items-center justify-center mb-3">
                                <Ionicons name="checkmark-circle" size={40} color="#00A452" />
                            </View>
                            <Text className="text-white text-2xl font-bold text-center">Charging Complete</Text>
                            <Text className="text-text-secondary text-base text-center mt-1">Please review your invoice below</Text>
                        </View>

                        {loadingInvoice ? (
                            <View className="py-8 items-center justify-center">
                                <ActivityIndicator color="#00A452" size="large" />
                                <Text className="text-text-secondary mt-4">Generating Invoice...</Text>
                            </View>
                        ) : invoice ? (
                            <>
                                {/* Invoice Summary */}
                                <View className="bg-border/20 border border-border p-4 rounded-lg mb-6">
                                    <View className="flex-row justify-between mb-3">
                                        <Text className="text-text-secondary text-base">Invoice Number</Text>
                                        <Text className="text-white font-semibold text-base">{invoice?.number}</Text>
                                    </View>
                                    <View className="flex-row justify-between mb-3">
                                        <Text className="text-text-secondary text-base">Energy Consumed</Text>
                                        <Text className="text-white font-semibold text-base">{(consumedKwh || 0).toFixed(2)} kWh</Text>
                                    </View>
                                    <View className="flex-row justify-between mb-3 pb-3 border-b border-border/50">
                                        <Text className="text-text-secondary text-base">Tax</Text>
                                        <Text className="text-white font-semibold text-base">Free</Text>
                                    </View>
                                    <View className="flex-row justify-between items-center">
                                        <Text className="text-text-secondary font-semibold text-lg">Total Amount</Text>
                                        <Text className="text-secondary font-bold text-xl">
                                            {invoice?.totalCost?.toLocaleString()} VND
                                        </Text>
                                    </View>
                                </View>

                                {/* Payment Method Section */}
                                <Text className="text-white font-semibold text-base mb-2">Payment Method</Text>
                                <View className="bg-border/20 border border-border p-4 rounded-lg flex-row items-center gap-x-3 mb-8">
                                    <View>
                                        <Image
                                            source={require("@/assets/images/zalopay.webp")}
                                            style={{ width: 44, height: 44, borderRadius: 8 }}
                                            resizeMode="contain"
                                        />
                                    </View>
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
                                        Pay Now
                                    </Button>
                                    <Button 
                                        onPress={onDismiss}
                                        variant="outline"
                                        className="w-full"
                                        style={{ height: 56 }}
                                        disabled={paying}
                                    >
                                        Pay Later
                                    </Button>
                                </View>
                            </>
                        ) : (
                            <View className="py-8 items-center justify-center">
                                <Text className="text-text-secondary mb-4">No invoice found</Text>
                                <Button onPress={onDismiss} variant="outline">Close</Button>
                            </View>
                        )}
                    </ScrollView>
                </Animated.View>
            </View>
        </Modal>
    );
}
