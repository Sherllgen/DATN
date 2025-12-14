import { Ionicons } from "@expo/vector-icons";
import { Text, TouchableOpacity, View } from "react-native";

export default function MenuItem({
    title,
    onPress,
    rightText,
    showChevron = true,
}: {
    title: string;
    onPress?: () => void;
    rightText?: string;
    showChevron?: boolean;
}) {
    return (
        <TouchableOpacity
            className="flex-row justify-between items-center py-4"
            onPress={onPress}
            activeOpacity={0.7}
        >
            <Text className="font-normal text-white text-base">{title}</Text>
            <View className="flex-row items-center gap-2">
                {rightText && (
                    <Text className="text-[#9BA1A6] text-sm">{rightText}</Text>
                )}
                {showChevron && (
                    <Ionicons
                        name="chevron-forward"
                        size={20}
                        color="#9BA1A6"
                    />
                )}
            </View>
        </TouchableOpacity>
    );
}
