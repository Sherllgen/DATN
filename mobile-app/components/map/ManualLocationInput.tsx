import React, { useState } from "react";
import { View, Text, Modal, TouchableOpacity, TextInput, ActivityIndicator } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import Button from "@/components/ui/Button";

interface ManualLocationInputProps {
    visible: boolean;
    onClose: () => void;
    onLocationSet: (latitude: number, longitude: number) => void;
}

export default function ManualLocationInput({
    visible,
    onClose,
    onLocationSet,
}: ManualLocationInputProps) {
    const [address, setAddress] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async () => {
        if (!address.trim()) {
            setError("Please enter an address");
            return;
        }

        setLoading(true);
        setError("");

        try {
            // Use Nominatim (OpenStreetMap) geocoding API - doesn't require location permissions
            const encodedAddress = encodeURIComponent(address);
            const url = `https://nominatim.openstreetmap.org/search?q=${encodedAddress}&format=json&limit=1`;

            const response = await fetch(url, {
                headers: {
                    'User-Agent': 'EV-Go Mobile App', // Required by Nominatim
                },
            });

            if (!response.ok) {
                throw new Error('Geocoding request failed');
            }

            const results = await response.json();

            if (!results || results.length === 0) {
                setError("Address not found. Please try a different address.");
                setLoading(false);
                return;
            }

            // Use the first result
            const { lat, lon } = results[0];
            const latitude = parseFloat(lat);
            const longitude = parseFloat(lon);

            console.log("Geocoded address:", address, "to", latitude, longitude);

            onLocationSet(latitude, longitude);
            handleClose();
        } catch (err) {
            console.error("Geocoding error:", err);
            setError("Failed to find location. Please check your internet connection and try again.");
        } finally {
            setLoading(false);
        }
    };

    const handleClose = () => {
        setAddress("");
        setError("");
        setLoading(false);
        onClose();
    };

    const handleQuickSelect = (quickAddress: string) => {
        setAddress(quickAddress);
    };

    return (
        <Modal
            visible={visible}
            transparent
            animationType="slide"
            onRequestClose={handleClose}
        >
            <View className="flex-1 justify-end bg-black/50">
                {/* Bottom Sheet */}
                <View className="bg-[#1E293B] rounded-t-3xl p-6 pb-8">
                    {/* Swipe Indicator */}
                    <View className="w-12 h-1 bg-border-gray rounded-full self-center mb-6" />

                    {/* Header */}
                    <View className="flex-row items-center justify-between mb-6">
                        <Text className="text-xl font-bold text-white">
                            Enter Your Location
                        </Text>
                        <TouchableOpacity
                            onPress={handleClose}
                            activeOpacity={0.7}
                        >
                            <Ionicons name="close" size={24} color="#9BA1A6" />
                        </TouchableOpacity>
                    </View>

                    {/* Description */}
                    <Text className="text-sm text-[#9BA1A6] mb-6">
                        Enter your address to find nearby charging stations
                    </Text>

                    {/* Address Input */}
                    <View className="mb-4">
                        <Text className="text-sm font-medium text-white mb-2">
                            Address or City
                        </Text>
                        <TextInput
                            value={address}
                            onChangeText={setAddress}
                            placeholder="e.g. District 1, Ho Chi Minh City"
                            placeholderTextColor="#4A5568"
                            className="bg-[#0F172A] border border-border-gray rounded-xl px-4 py-3 text-white"
                            multiline
                        />
                    </View>

                    {/* Error Message */}
                    {error ? (
                        <Text className="text-sm text-[#EF4444] mb-4">
                            {error}
                        </Text>
                    ) : null}

                    {/* Quick Location Presets */}
                    <View className="mb-6">
                        <Text className="text-sm font-medium text-[#9BA1A6] mb-3">
                            Quick select:
                        </Text>
                        <View className="gap-2">
                            <TouchableOpacity
                                onPress={() => handleQuickSelect("District 1, Ho Chi Minh City")}
                                className="bg-[#0F172A] border border-border-gray rounded-xl px-4 py-3"
                                activeOpacity={0.7}
                            >
                                <Text className="text-white font-medium">
                                    District 1, Ho Chi Minh City
                                </Text>
                            </TouchableOpacity>

                            <TouchableOpacity
                                onPress={() => handleQuickSelect("Hanoi, Vietnam")}
                                className="bg-[#0F172A] border border-border-gray rounded-xl px-4 py-3"
                                activeOpacity={0.7}
                            >
                                <Text className="text-white font-medium">
                                    Hanoi, Vietnam
                                </Text>
                            </TouchableOpacity>
                        </View>
                    </View>

                    {/* Submit Button */}
                    <Button
                        variant="primary"
                        fullWidth
                        onPress={handleSubmit}
                        disabled={loading}
                        textWrapper={false}
                    >
                        {loading ? (
                            <View className="flex-row items-center gap-2">
                                <ActivityIndicator color="#FFF" />
                                <Text className="text-white font-semibold">
                                    Finding Location...
                                </Text>
                            </View>
                        ) : (
                            <Text className="text-white text-base font-semibold">
                                Use This Location
                            </Text>
                        )}
                    </Button>
                </View>
            </View>
        </Modal>
    );
}
