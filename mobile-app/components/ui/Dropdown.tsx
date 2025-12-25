import { Ionicons } from "@expo/vector-icons";
import { useState } from "react";
import { Text, TouchableOpacity, View } from "react-native";

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
                className="flex-row justify-between items-center pb-3 border-[#4A5568] border-b"
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

            {/* Dropdown Options */}
            {isOpen && (
                <View className="bg-primary/15 mt-2 border border-[#4A5568] rounded-lg overflow-hidden">
                    {items.map((item, index) => (
                        <TouchableOpacity
                            key={item.value}
                            className={`px-4 py-3 ${
                                index !== items.length - 1
                                    ? "border-b border-[#4A5568]"
                                    : ""
                            } ${currentValue === item.value ? "bg-[#4CAF50]/20" : ""}`}
                            activeOpacity={0.7}
                            onPress={() => handleValueChange(item.value)}
                        >
                            <View className="flex-row justify-between items-center">
                                <Text
                                    className={`text-base ${
                                        currentValue === item.value
                                            ? "text-[#4CAF50] font-semibold"
                                            : "text-white"
                                    }`}
                                >
                                    {item.label}
                                </Text>
                                {currentValue === item.value && (
                                    <Ionicons
                                        name="checkmark"
                                        size={20}
                                        color="#4CAF50"
                                    />
                                )}
                            </View>
                        </TouchableOpacity>
                    ))}
                </View>
            )}
        </View>
    );
}
