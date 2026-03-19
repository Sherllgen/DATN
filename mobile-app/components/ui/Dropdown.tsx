import { Ionicons } from "@expo/vector-icons";
import { useState } from "react";
import { Modal, Text, TouchableOpacity, View, ScrollView } from "react-native";

interface DropdownItem {
    label: string;
    value: string;
}

interface DropdownProps {
    label: string;
    value?: string;
    defaultValue?: string;
    items: DropdownItem[];
    onValueChange: (value: string) => void;
    placeholder?: string;
    disabled?: boolean;
}

export default function Dropdown({
    label,
    value,
    defaultValue,
    items,
    onValueChange,
    placeholder = "Select an option",
    disabled = false,
}: DropdownProps) {
    const [isOpen, setIsOpen] = useState(false);
    const [internalValue, setInternalValue] = useState(defaultValue || "");

    // Use controlled value if provided, otherwise use internal value
    const currentValue = value !== undefined ? value : internalValue;
    const selectedItem = items.find((item) => item.value === currentValue);

    const handleValueChange = (newValue: string) => {
        if (value === undefined) {
            setInternalValue(newValue);
        }
        onValueChange(newValue);
        setIsOpen(false);
    };

    return (
        <View className="mt-4">
            <Text className="mb-3 text-[#9BA1A6] text-sm">{label}</Text>
            <TouchableOpacity
                className="flex-row justify-between items-center pb-3 border-border-gray border-b"
                activeOpacity={0.7}
                onPress={() => !disabled && setIsOpen(!isOpen)}
                disabled={disabled}
            >
                <Text className="text-[#4CAF50] text-base">
                    {selectedItem?.label || defaultValue || placeholder}
                </Text>
                <Ionicons
                    name={isOpen ? "chevron-up" : "chevron-down"}
                    size={20}
                    color="#4CAF50"
                />
            </TouchableOpacity>

            {/* Dropdown Options Modal */}
            <Modal
                visible={isOpen}
                animationType="slide"
                transparent={true}
                onRequestClose={() => setIsOpen(false)}
            >
                <TouchableOpacity
                    className="flex-1 justify-end bg-black/60"
                    activeOpacity={1}
                    onPress={() => setIsOpen(false)}
                >
                    <TouchableOpacity activeOpacity={1}>
                        <View className="bg-surface pt-2 pb-8 rounded-t-3xl">
                            {/* Swipe Indicator */}
                            <View className="items-center mb-4">
                                <View className="bg-border-gray rounded-full w-12 h-1" />
                            </View>

                            <View className="flex-row justify-center items-center px-6 pb-4 border-border-gray border-b">
                                <Text className="font-semibold text-white text-lg text-center">
                                    {label}
                                </Text>
                            </View>

                            <ScrollView className="max-h-80 px-4 mt-2">
                                {items.map((item) => {
                                    const isSelected = currentValue === item.value;
                                    return (
                                        <TouchableOpacity
                                            key={item.value}
                                            className={`flex-row justify-between items-center px-4 py-4 mb-2 rounded-xl ${isSelected
                                                ? "bg-[#4CAF50]/20 border border-[#4CAF50]"
                                                : "bg-white/5 border border-transparent"
                                                }`}
                                            activeOpacity={0.7}
                                            onPress={() => handleValueChange(item.value)}
                                        >
                                            <Text
                                                className={`text-base ${isSelected
                                                    ? "text-[#4CAF50] font-semibold"
                                                    : "text-white"
                                                    }`}
                                            >
                                                {item.label}
                                            </Text>
                                            {isSelected && (
                                                <Ionicons
                                                    name="checkmark-circle"
                                                    size={24}
                                                    color="#4CAF50"
                                                />
                                            )}
                                        </TouchableOpacity>
                                    );
                                })}
                            </ScrollView>
                        </View>
                    </TouchableOpacity>
                </TouchableOpacity>
            </Modal>
        </View>
    );
}
