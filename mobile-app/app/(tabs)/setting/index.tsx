import { useAuth } from "@/contexts/AuthContext";
import { Ionicons } from "@expo/vector-icons";
import { Image, ScrollView, Text, TouchableOpacity, View } from "react-native";

export default function SettingPage() {
    const { logout } = useAuth();

    const MenuItem = ({
        title,
        onPress,
        rightText,
        showChevron = true,
    }: {
        title: string;
        onPress?: () => void;
        rightText?: string;
        showChevron?: boolean;
    }) => (
        <TouchableOpacity
            className="flex-row justify-between items-center py-5"
            onPress={onPress}
            activeOpacity={0.7}
        >
            <Text className="font-normal text-[17px] text-white">{title}</Text>
            <View className="flex-row items-center gap-2">
                {rightText && (
                    <Text className="text-[#9BA1A6] text-base">
                        {rightText}
                    </Text>
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

    return (
        <ScrollView className="flex-1 bg-[#33404F]">
            {/* Profile Section */}
            <TouchableOpacity
                className="flex-row items-center px-6 py-5"
                activeOpacity={0.7}
            >
                <Image
                    source={{ uri: "https://i.pravatar.cc/150?img=12" }}
                    className="bg-[#4A5568] rounded-full w-[70px] h-[70px]"
                />
                <View className="flex-1 ml-4">
                    <Text className="mb-1 font-semibold text-white text-xl">
                        Andrew Ainsley
                    </Text>
                    <Text className="text-[#9BA1A6] text-[15px]">
                        +1111467378399
                    </Text>
                </View>
                <Ionicons name="chevron-forward" size={24} color="#9BA1A6" />
            </TouchableOpacity>

            <View className="bg-[#4A5568] mx-6 my-2 h-[1px]" />

            {/* Menu Items */}
            <View className="px-6 pt-4">
                <MenuItem
                    title="My Vehicle"
                    onPress={() => console.log("My Vehicle")}
                />
                <MenuItem
                    title="Payment Methods"
                    onPress={() => console.log("Payment Methods")}
                />
                <MenuItem
                    title="Personal Info"
                    onPress={() => console.log("Personal Info")}
                />
                <MenuItem
                    title="Security"
                    onPress={() => console.log("Security")}
                />
                <MenuItem
                    title="Language"
                    rightText="English (US)"
                    onPress={() => console.log("Language")}
                />
                <MenuItem
                    title="Help Center"
                    onPress={() => console.log("Help Center")}
                />
                <MenuItem
                    title="Privacy Policy"
                    onPress={() => console.log("Privacy Policy")}
                />
                <MenuItem
                    title="About EVPoint"
                    onPress={() => console.log("About EVPoint")}
                />
            </View>

            {/* Logout Button */}
            <TouchableOpacity
                className="mx-6 mt-8 py-5"
                onPress={logout}
                activeOpacity={0.7}
            >
                <Text className="font-semibold text-[#FF3B30] text-[17px]">
                    Logout
                </Text>
            </TouchableOpacity>

            <View className="h-10" />
        </ScrollView>
    );
}
