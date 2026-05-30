import React, { useState, useEffect, useCallback } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    Linking,
    Alert,
    ActivityIndicator,
    AppState,
    AppStateStatus,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import GradientBackground from "@/components/ui/GradientBackground";
import { InvoiceCard, InvoiceCardSkeleton } from '@/components/InvoiceCard';
import { getMyInvoices, payInvoice } from '@/apis/invoiceApi';
import { InvoiceResponse, InvoicePurpose } from '@/types/invoice.types';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { useUserStore } from '@/contexts/user.store';
import GuestPlaceholder from '@/components/auth/GuestPlaceholder';

// ─── Types ────────────────────────────────────────────────────────────────────

type TabType = 'UNPAID' | 'PAID';
type PurposeFilter = 'ALL' | InvoicePurpose;

// ─── Helpers ──────────────────────────────────────────────────────────────────

interface PurposeFilterConfig {
    key: PurposeFilter;
    label: string;
    icon: React.ComponentProps<typeof Ionicons>['name'];
}

const PURPOSE_FILTERS: PurposeFilterConfig[] = [
    { key: 'ALL', label: 'All', icon: 'list-outline' },
    { key: 'CHARGING_SESSION', label: 'Charging', icon: 'flash-outline' },
    { key: 'BOOKING', label: 'Booking', icon: 'calendar-outline' },
    { key: 'IDLE_FEE', label: 'Idle Fee', icon: 'time-outline' },
];

// ─── Atom: PurposeFilterChip ──────────────────────────────────────────────────

interface PurposeFilterChipProps {
    config: PurposeFilterConfig;
    isActive: boolean;
    onPress: () => void;
}

const PurposeFilterChip: React.FC<PurposeFilterChipProps> = ({ config, isActive, onPress }) => (
    <TouchableOpacity
        activeOpacity={0.7}
        onPress={onPress}
        className={[
            'flex-row items-center px-3 py-1.5 rounded-full mr-2 border',
            isActive
                ? 'bg-secondary border-secondary'
                : 'bg-white/5 border-white/10',
        ].join(' ')}
    >
        <Ionicons
            name={config.icon}
            size={12}
            color={isActive ? '#ffffff' : 'rgba(255,255,255,0.5)'}
        />
        <Text className={['text-xs font-semibold ml-1', isActive ? 'text-white' : 'text-white/50'].join(' ')}>
            {config.label}
        </Text>
    </TouchableOpacity>
);

// ─── Screen ───────────────────────────────────────────────────────────────────

export default function PaymentPage() {
    const router = useRouter();
    const { user } = useUserStore();

    const [activeTab, setActiveTab] = useState<TabType>('UNPAID');
    const [activePurpose, setActivePurpose] = useState<PurposeFilter>('ALL');
    const [invoices, setInvoices] = useState<InvoiceResponse[]>([]);
    const [page, setPage] = useState(0);
    const [isLoading, setIsLoading] = useState(true);
    const [isRefreshing, setIsRefreshing] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [isProcessingPayment, setIsProcessingPayment] = useState(false);

    const fetchInvoices = useCallback(async (
        tab: TabType,
        purpose: PurposeFilter,
        pageNum: number,
        shouldRefresh = false
    ) => {
        try {
            if (pageNum === 0 && !shouldRefresh) {
                setIsLoading(true);
            }

            const purposeParam = purpose === 'ALL' ? undefined : purpose;
            const response = await getMyInvoices(tab, pageNum, 10, purposeParam);

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

    // Reload when tab or purpose filter changes
    useEffect(() => {
        if (user) {
            fetchInvoices(activeTab, activePurpose, 0);
        }
    }, [activeTab, activePurpose, fetchInvoices, user]);

    // Refresh UNPAID invoices when returning from ZaloPay
    useEffect(() => {
        const subscription = AppState.addEventListener('change', (nextAppState: AppStateStatus) => {
            if (nextAppState === 'active' && activeTab === 'UNPAID' && user) {
                fetchInvoices('UNPAID', activePurpose, 0, true);
            }
        });
        return () => {
            subscription.remove();
        };
    }, [activeTab, activePurpose, fetchInvoices, user]);

    const handleTabChange = (tab: TabType) => {
        setActiveTab(tab);
        setActivePurpose('ALL'); // Reset filter when switching tabs
    };

    const handleRefresh = () => {
        setIsRefreshing(true);
        fetchInvoices(activeTab, activePurpose, 0, true);
    };

    const handleLoadMore = () => {
        if (!isLoading && !isRefreshing && hasMore) {
            fetchInvoices(activeTab, activePurpose, page + 1);
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

        const purposeLabel = activePurpose === 'ALL'
            ? ''
            : ` ${PURPOSE_FILTERS.find(f => f.key === activePurpose)?.label ?? ''}`;

        return (
            <View className="flex-1 items-center justify-center py-20">
                <Ionicons
                    name={activeTab === 'UNPAID' ? 'receipt-outline' : 'checkmark-done-circle-outline'}
                    size={64}
                    color="rgba(255,255,255,0.2)"
                />
                <Text className="text-white/50 text-lg mt-4 font-medium text-center">
                    No {activeTab.toLowerCase()}{purposeLabel} invoices found
                </Text>
                {activePurpose !== 'ALL' && (
                    <TouchableOpacity
                        activeOpacity={0.7}
                        onPress={() => setActivePurpose('ALL')}
                        className="mt-4 bg-white/10 px-4 py-2 rounded-full"
                    >
                        <Text className="text-white/70 text-sm">Clear filter</Text>
                    </TouchableOpacity>
                )}
            </View>
        );
    };

    if (!user) {
        return (
            <GradientBackground preset="main" dismissKeyboard={false}>
                <SafeAreaView style={{ flex: 1 }} className="pt-4" edges={["top", "left", "right"]}>
                    <View className="flex-row items-center mb-2 px-4">
                        <Text className="text-white font-bold text-2xl">Payment History</Text>
                    </View>
                    <GuestPlaceholder
                        title="Payment History"
                        description="Sign in to view your invoices and payment history."
                        icon="receipt-outline"
                    />
                </SafeAreaView>
            </GradientBackground>
        );
    }

    return (
        <GradientBackground preset="main" dismissKeyboard={false}>
            <SafeAreaView style={{ flex: 1 }} className="pt-4" edges={["top", "left", "right"]}>
                {/* Header */}
                <View className="flex-row items-center mb-2 px-4">
                    <Text className="text-white font-bold text-2xl">Payment History</Text>
                </View>

                {/* Status Tab Switcher (UNPAID / PAID) */}
                <View className="border-b border-white/10 mb-3">
                    <View className="flex-row px-4">
                        {(['UNPAID', 'PAID'] as TabType[]).map((tab) => {
                            const isActive = activeTab === tab;
                            const label = tab === 'UNPAID' ? 'Unpaid' : 'Paid';
                            return (
                                <TouchableOpacity
                                    key={tab}
                                    activeOpacity={0.7}
                                    onPress={() => handleTabChange(tab)}
                                    className="flex-1 items-center py-4 relative"
                                >
                                    <Text className={`text-base font-medium ${isActive ? "text-secondary" : "text-white/40"}`}>
                                        {label}
                                    </Text>
                                    {isActive && (
                                        <View className="absolute bottom-0 left-0 right-0 h-1 bg-secondary rounded-t-full" />
                                    )}
                                </TouchableOpacity>
                            );
                        })}
                    </View>
                </View>

                {/* Purpose Filter Chips */}
                <View className="mb-4">
                    <FlatList
                        horizontal
                        data={PURPOSE_FILTERS}
                        keyExtractor={(item) => item.key}
                        contentContainerStyle={{ paddingHorizontal: 16 }}
                        showsHorizontalScrollIndicator={false}
                        renderItem={({ item }) => (
                            <PurposeFilterChip
                                config={item}
                                isActive={activePurpose === item.key}
                                onPress={() => setActivePurpose(item.key)}
                            />
                        )}
                    />
                </View>

                {/* Invoice List */}
                <FlatList
                    style={{ flex: 1 }}
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
