import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { InvoiceResponse, InvoicePurpose } from '@/types/invoice.types';
import { Ionicons } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

// ─── Types ───────────────────────────────────────────────────────────────────

export interface InvoiceCardProps {
    invoice: InvoiceResponse;
    onPayAction?: (invoice: InvoiceResponse) => void;
}

// ─── Purpose badge configuration ─────────────────────────────────────────────

interface PurposeMeta {
    label: string;
    description: string;
    icon: React.ComponentProps<typeof Ionicons>['name'];
    badgeBg: string;
    badgeText: string;
    iconColor: string;
}

const PURPOSE_META: Record<InvoicePurpose, PurposeMeta> = {
    BOOKING: {
        label: 'Booking Fee',
        description: 'Reservation deposit for a charging slot',
        icon: 'calendar-outline',
        badgeBg: 'bg-blue-500/20',
        badgeText: 'text-blue-400',
        iconColor: '#60A5FA',
    },
    CHARGING_SESSION: {
        label: 'Charging Session',
        description: 'Energy consumed during charging',
        icon: 'flash-outline',
        badgeBg: 'bg-secondary/20',
        badgeText: 'text-secondary',
        iconColor: '#00A452',
    },
    IDLE_FEE: {
        label: 'Idle Fee',
        description: 'Vehicle remained connected after session ended',
        icon: 'time-outline',
        badgeBg: 'bg-warning/20',
        badgeText: 'text-warning',
        iconColor: '#F59E0B',
    },
};

// ─── Atom: PurposeBadge ───────────────────────────────────────────────────────

interface PurposeBadgeProps {
    purpose: InvoicePurpose;
}

const PurposeBadge: React.FC<PurposeBadgeProps> = ({ purpose }) => {
    const meta = PURPOSE_META[purpose] ?? PURPOSE_META.BOOKING;
    return (
        <View className={[
            'flex-row items-center px-2 py-1 rounded-lg space-x-1 self-start',
            meta.badgeBg,
        ].join(' ')}>
            <Ionicons name={meta.icon} size={12} color={meta.iconColor} />
            <Text className={['text-xs font-semibold ml-1', meta.badgeText].join(' ')}>
                {meta.label}
            </Text>
        </View>
    );
};

// ─── Atom: StatusBadge ────────────────────────────────────────────────────────

interface StatusBadgeProps {
    isUnpaid: boolean;
}

const StatusBadge: React.FC<StatusBadgeProps> = ({ isUnpaid }) => (
    <View className={[
        'px-3 py-1 rounded-full flex-row items-center space-x-1',
        isUnpaid ? 'bg-red-500/20' : 'bg-green-500/20',
    ].join(' ')}>
        <Ionicons
            name={isUnpaid ? 'alert-circle' : 'checkmark-circle'}
            size={14}
            color={isUnpaid ? '#EF4444' : '#10B981'}
        />
        <Text className={['text-xs font-bold ml-1', isUnpaid ? 'text-red-400' : 'text-green-400'].join(' ')}>
            {isUnpaid ? 'UNPAID' : 'PAID'}
        </Text>
    </View>
);

// ─── Molecule: InvoiceCard ────────────────────────────────────────────────────

export const InvoiceCard: React.FC<InvoiceCardProps> = ({ invoice, onPayAction }) => {
    const isUnpaid = invoice.status === 'PENDING';
    const isIdleFee = invoice.purpose === 'IDLE_FEE';
    const router = useRouter();

    const formattedDate = new Date(
        invoice.createdAt.endsWith('Z') ? invoice.createdAt : `${invoice.createdAt}Z`
    ).toLocaleString('en-GB', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    });

    return (
        <TouchableOpacity
            activeOpacity={0.7}
            onPress={() => router.push({ pathname: '/invoice/[id]', params: { id: invoice.id } } as any)}
            className="bg-[#1A2634] rounded-2xl p-4 mb-4 border border-white/5 shadow-lg"
        >
            {/* Row 1: Invoice number + payment status */}
            <View className="flex-row justify-between items-center mb-3">
                <Text className="text-white/60 font-medium text-sm">
                    #{invoice.number}
                </Text>
                <StatusBadge isUnpaid={isUnpaid} />
            </View>

            {/* Row 2: Purpose badge */}
            <View className="mb-3">
                <PurposeBadge purpose={invoice.purpose} />
            </View>

            {/* Row 3: IDLE_FEE explanation banner */}
            {isIdleFee && (
                <View className="flex-row items-start bg-warning/10 border border-warning/30 rounded-xl px-3 py-2 mb-3">
                    <Ionicons name="information-circle-outline" size={16} color="#F59E0B" style={{ marginTop: 1 }} />
                    <Text className="text-warning text-xs ml-2 flex-1 leading-4">
                        {PURPOSE_META.IDLE_FEE.description}. This must be settled to resume future charging.
                    </Text>
                </View>
            )}

            {/* Row 4: Date and amount */}
            <View className="flex-row justify-between items-end mb-4">
                <View>
                    <Text className="text-white/60 text-xs mb-1">Date Created</Text>
                    <Text className="text-white font-medium">{formattedDate}</Text>
                </View>
                <View className="items-end">
                    <Text className="text-white/60 text-xs mb-1">Amount</Text>
                    <Text className="text-slate-200 font-bold text-xl">
                        {invoice.totalCost.toLocaleString('vi-VN')} ₫
                    </Text>
                </View>
            </View>

            {/* Row 5: Pay button (unpaid only) */}
            {isUnpaid && onPayAction && (
                <TouchableOpacity
                    className="bg-primary py-3 rounded-xl items-center flex-row justify-center space-x-2"
                    activeOpacity={0.8}
                    onPress={() => onPayAction(invoice)}
                >
                    <Ionicons name="card" size={18} color="white" />
                    <Text className="text-white font-bold text-base ml-2">Pay Now</Text>
                </TouchableOpacity>
            )}
        </TouchableOpacity>
    );
};

// ─── Skeleton ─────────────────────────────────────────────────────────────────

export const InvoiceCardSkeleton: React.FC = () => (
    <View className="bg-[#1A2634] rounded-2xl p-4 mb-4 border border-white/5 opacity-50">
        <View className="flex-row justify-between items-center mb-3">
            <View className="w-24 h-4 bg-white/10 rounded-full" />
            <View className="w-16 h-6 bg-white/10 rounded-full" />
        </View>
        {/* Purpose badge skeleton */}
        <View className="w-32 h-6 bg-white/10 rounded-lg mb-3" />
        <View className="flex-row justify-between items-end mb-4">
            <View>
                <View className="w-20 h-3 bg-white/10 rounded-full mb-2" />
                <View className="w-24 h-4 bg-white/10 rounded-full" />
            </View>
            <View className="items-end">
                <View className="w-16 h-3 bg-white/10 rounded-full mb-2" />
                <View className="w-28 h-6 bg-white/10 rounded-full" />
            </View>
        </View>
        <View className="w-full h-12 bg-white/10 rounded-xl" />
    </View>
);
