import React, { useState } from "react";
import { View, Text, Switch, TouchableOpacity } from "react-native";
import { Ionicons, MaterialCommunityIcons, Feather } from "@expo/vector-icons";
import { Booking } from "@/data/bookingData";
import { AppColors } from "@/constants/theme";
import Button from "@/components/ui/Button";
import BookingStatItem from "./BookingStatItem";

interface BookingCardProps {
    booking: Booking;
    onCancel?: () => void;
    onView?: () => void;
}

const BookingCard = ({ booking, onCancel, onView }: BookingCardProps) => {
    const [remindMe, setRemindMe] = useState(true);
    const isUpcoming = booking.status === "Upcoming";

    return (
        <View className="bg-border-gray/20 rounded-3xl border border-border-gray p-5 mb-6">
            {/* Header: Date/Time + Remind Me */}
            <View className="flex-row justify-between items-start mb-4">
                <View>
                    <Text className="text-white text-lg font-semibold mb-1">
                        {booking.date}
                    </Text>
                    <Text className="text-white/60 text-sm">
                        {booking.time}
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
                        {booking.station.name}
                    </Text>
                    <Text className="text-white/50 text-xs" numberOfLines={1}>
                        {booking.station.address}
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
                    label="Tesla (Plug)"
                    value={booking.charger.connectorType}
                    icon={<MaterialCommunityIcons name="ev-plug-tesla" size={32} color="white" />}
                    showBorderRight
                />
                <BookingStatItem
                    label="Max. power"
                    value={booking.charger.maxPower}
                    showBorderRight
                />
                <BookingStatItem
                    label="Duration"
                    value={booking.duration}
                    showBorderRight
                />
                <BookingStatItem
                    label="Amount"
                    value={`$${booking.amount.toFixed(2)}`}
                />
            </View>

            {/* Action Buttons */}
            <View className="flex-row gap-3 mb-4">
                {isUpcoming && (
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
                <Button
                    variant="primary"
                    onPress={onView}
                    className="flex-1 h-12"
                    textClassName="text-white text-sm font-semibold"
                    activeOpacity={0.8}
                >
                    View
                </Button>
            </View>

            {/* Info Note (Only for Upcoming) */}
            {isUpcoming && (
                <View className="bg-[#00A452]/10 rounded-xl p-3 flex-row items-start border border-[#00A452]/5">
                    <Ionicons name="information-circle" size={18} color="#00A452" className="mr-2 mt-0.5" />
                    <Text className="text-secondary text-sm leading-5 flex-1">
                        Insert the charger connection into your car to start charging. If you do not charge after 15 minutes from the time, this booking will be automatically cancelled.
                    </Text>
                </View>
            )}
        </View>
    );
};

export default BookingCard;
