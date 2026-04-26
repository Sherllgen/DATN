import React, { useState } from "react";
import { View, Text, Switch, TouchableOpacity } from "react-native";
import { Ionicons, MaterialCommunityIcons, Feather } from "@expo/vector-icons";
import { BookingResponse, BookingStatus } from "@/types/booking.types";
import { AppColors } from "@/constants/theme";
import Button from "@/components/ui/Button";
import BookingStatItem from "./BookingStatItem";

interface BookingCardProps {
    booking: BookingResponse;
    onCancel?: () => void;
    onView?: () => void;
    onPay?: () => void;
}

const BookingCard = ({ booking, onCancel, onView, onPay }: BookingCardProps) => {
    const [remindMe, setRemindMe] = useState(true);
    const isUpcoming = booking.status === BookingStatus.CONFIRMED;
    const isPending = booking.status === BookingStatus.PENDING;

    // Booking times are stored in UTC – append 'Z' to convert to device local time
    const startStr = booking.startTime?.endsWith('Z') ? booking.startTime : `${booking.startTime}Z`;
    const endStr = booking.endTime?.endsWith('Z') ? booking.endTime : `${booking.endTime}Z`;
    const dateObj = new Date(startStr);
    const endObj = new Date(endStr);
    
    const dateStr = dateObj.toLocaleDateString("en-US", { month: "short", day: "2-digit", year: "numeric" });
    const timeStr = dateObj.toLocaleTimeString("en-US", { hour: "2-digit", minute: "2-digit" });
    
    // Duration formatting
    const diffMins = Math.round((endObj.getTime() - dateObj.getTime()) / 60000);
    const durationStr = diffMins >= 60 ? `${(diffMins / 60).toFixed(1)} Hours` : `${diffMins} mins`;

    return (
        <View className="bg-border-gray/20 rounded-3xl border border-border-gray p-5 mb-6">
            {/* Header: Date/Time + Remind Me */}
            <View className="flex-row justify-between items-start mb-4">
                <View>
                    <Text className="text-white text-lg font-semibold mb-1">
                        {dateStr}
                    </Text>
                    <Text className="text-white/60 text-sm">
                        {timeStr}
                    </Text>
                </View>
                {isUpcoming && (
                    <View className="flex-row items-center">
                        <Text className="text-white/60 text-xs mr-2">Remind me</Text>
                        <Switch
                            value={remindMe}
                            onValueChange={setRemindMe}
                            trackColor={{ false: "#2D3748", true: AppColors.secondary }}
                            thumbColor="white"
                            ios_backgroundColor="#2D3748"
                        />
                    </View>
                )}
            </View>

            <View className="h-[1px] bg-white/5 mb-5" />

            {/* Station Info */}
            <View className="flex-row justify-between items-center mb-6">
                <View className="flex-1 mr-4">
                    <Text className="text-white text-xl font-bold mb-1" numberOfLines={1}>
                        {booking.stationName || `Station #${booking.stationId}`}
                    </Text>
                    <Text className="text-white/50 text-xs" numberOfLines={1}>
                        {booking.stationAddress || "Unknown Address"}
                    </Text>
                </View>
                <TouchableOpacity
                    activeOpacity={0.7}
                    className="w-12 h-12 rounded-full bg-secondary items-center justify-center shadow-lg shadow-[#00A452]/20"
                >
                    <MaterialCommunityIcons name="navigation-variant" size={24} color="white" />
                </TouchableOpacity>
            </View>

            {/* Stats Grid */}
            <View className="flex-row bg-white/5 rounded-2xl py-4 mb-6">
                <BookingStatItem
                    label={booking.connectorType || "Plug"}
                    value="Type"
                    icon={<MaterialCommunityIcons name="ev-plug-tesla" size={32} color="white" />}
                    showBorderRight
                />
                <BookingStatItem
                    label="Max. power"
                    value={booking.maxPower ? `${booking.maxPower} kW` : "N/A"}
                    showBorderRight
                />
                <BookingStatItem
                    label="Duration"
                    value={durationStr}
                    showBorderRight
                />
                <BookingStatItem
                    label="Amount"
                    value={`₫${booking.totalPrice?.toFixed(2) || "0.00"}`}
                />
            </View>

            {/* Action Buttons */}
            <View className="flex-row gap-3 mb-4">
                {(isUpcoming || isPending) && (
                    <Button
                        variant="outline"
                        onPress={onCancel}
                        className="flex-1 h-12"
                        style={{ borderColor: '#fff' }}
                        textClassName="text-[#fff] text-sm font-semibold"
                        activeOpacity={0.8}
                    >
                        Cancel Booking
                    </Button>
                )}
                {isPending ? (
                    <Button
                        variant="primary"
                        onPress={onPay}
                        className="flex-1 h-12"
                        textClassName="text-white text-sm font-semibold"
                        activeOpacity={0.8}
                    >
                        Pay Now
                    </Button>
                ) : (
                    <Button
                        variant="primary"
                        onPress={onView}
                        className="flex-1 h-12"
                        textClassName="text-white text-sm font-semibold"
                        activeOpacity={0.8}
                    >
                        View
                    </Button>
                )}
            </View>

            {/* Info Note */}
            {isUpcoming && (
                <View className="bg-[#00A452]/10 rounded-xl p-3 flex-row items-start border border-[#00A452]/5">
                    <Ionicons name="information-circle" size={18} color="#00A452" className="mr-2 mt-0.5" />
                    <Text className="text-secondary text-sm leading-5 flex-1">
                        Insert the charger connection into your car to start charging. If you do not charge after 15 minutes from the time, this booking will be automatically cancelled.
                    </Text>
                </View>
            )}
            {isPending && (
                <View className="bg-[#EAB308]/10 rounded-xl p-3 flex-row items-start border border-[#EAB308]/5">
                    <Ionicons name="time" size={18} color="#EAB308" className="mr-2 mt-0.5" />
                    <Text className="text-[#EAB308] text-sm leading-5 flex-1 font-medium">
                        Payment Pending: Please click "Pay Now" to finalize this booking.
                    </Text>
                </View>
            )}
        </View>
    );
};

export default BookingCard;
