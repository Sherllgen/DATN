import React from 'react';
import { View, Text, TouchableOpacity } from 'react-native';
import { InvoiceResponse } from '@/types/invoice.types';
import { Ionicons } from '@expo/vector-icons';

interface InvoiceCardProps {
    invoice: InvoiceResponse;
    onPayAction?: (invoice: InvoiceResponse) => void;
}

export const InvoiceCard: React.FC<InvoiceCardProps> = ({ invoice, onPayAction }) => {
    const isUnpaid = invoice.status === 'PENDING';

    return (
        <View className="bg-[#1A2634] rounded-2xl p-4 mb-4 border border-white/5 shadow-lg">
            {/* Header: Status and ID */}
            <View className="flex-row justify-between items-center mb-3">
                <Text className="text-white/60 font-medium text-sm">
                    #{invoice.number}
                </Text>
                <View className={`px-3 py-1 rounded-full flex-row items-center space-x-1 ${
                    isUnpaid ? 'bg-red-500/20' : 'bg-green-500/20'
                }`}>
                    <Ionicons 
                        name={isUnpaid ? 'alert-circle' : 'checkmark-circle'} 
                        size={14} 
                        color={isUnpaid ? '#EF4444' : '#10B981'} 
                    />
                    <Text className={`text-xs font-bold ${
                        isUnpaid ? 'text-red-400' : 'text-green-400'
                    }`}>
                        {isUnpaid ? 'UNPAID' : 'PAID'}
                    </Text>
                </View>
            </View>

            {/* Content: Date and Amount */}
            <View className="flex-row justify-between items-end mb-4">
                <View>
                    <Text className="text-white/60 text-xs mb-1">Date Created</Text>
                    <Text className="text-white font-medium">
                        {new Date(invoice.createdAt.endsWith('Z') ? invoice.createdAt : `${invoice.createdAt}Z`).toLocaleString('en-GB', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                        })}
                    </Text>
                </View>
                <View className="items-end">
                    <Text className="text-white/60 text-xs mb-1">Amount</Text>
                    <Text className="text-slate-200 font-bold text-xl">
                        {invoice.totalCost.toLocaleString('vi-VN')} ₫
                    </Text>
                </View>
            </View>

            {/* Action Button */}
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
        </View>
    );
};

export const InvoiceCardSkeleton: React.FC = () => {
    return (
        <View className="bg-[#1A2634] rounded-2xl p-4 mb-4 border border-white/5 opacity-50">
            <View className="flex-row justify-between items-center mb-3">
                <View className="w-24 h-4 bg-white/10 rounded-full" />
                <View className="w-16 h-6 bg-white/10 rounded-full" />
            </View>
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
};
