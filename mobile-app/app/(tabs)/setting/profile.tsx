import AppHeader from "@/components/ui/AppHeader";
import Dropdown from "@/components/ui/Dropdown";
import ImagePickerModal from "@/components/ui/ImagePickerModal";
import { Ionicons } from "@expo/vector-icons";
import * as ImagePicker from "expo-image-picker";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import { useState } from "react";

import {
    Image,
    ScrollView,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";

type BikeBrand = "YAMAHA" | "HONDA" | "SUZUKI" | "VINFAST" | "OTHER";
type BikeBrandItem = { label: BikeBrand; value: BikeBrand };

export default function ProfilePage() {
    const [fullName, setFullName] = useState("Hoàng Trung Anh");
    const [bikeBrand, setBikeBrand] = useState<BikeBrand>("VINFAST");
    const [profileImage, setProfileImage] = useState<string | null>(null);
    const [showImageModal, setShowImageModal] = useState(false);
    const [listBikeBrand] = useState<BikeBrandItem[]>([
        { label: "YAMAHA", value: "YAMAHA" },
        { label: "HONDA", value: "HONDA" },
        { label: "SUZUKI", value: "SUZUKI" },
        { label: "VINFAST", value: "VINFAST" },
        { label: "OTHER", value: "OTHER" },
    ]);

    const router = useRouter();

    const pickImage = async () => {
        setShowImageModal(false);

        // Request permission
        const permissionResult =
            await ImagePicker.requestMediaLibraryPermissionsAsync();

        if (!permissionResult.granted) {
            return;
        }

        // Open image picker
        const result = await ImagePicker.launchImageLibraryAsync({
            mediaTypes: ["images"],
            allowsEditing: true,
            aspect: [1, 1],
            quality: 0.8,
        });

        if (!result.canceled && result.assets[0]) {
            setProfileImage(result.assets[0].uri);
        }
    };

    const takePhoto = async () => {
        setShowImageModal(false);

        // Request permission
        const permissionResult =
            await ImagePicker.requestCameraPermissionsAsync();

        if (!permissionResult.granted) {
            return;
        }

        // Open camera
        const result = await ImagePicker.launchCameraAsync({
            allowsEditing: true,
            aspect: [1, 1],
            quality: 0.8,
        });

        if (!result.canceled && result.assets[0]) {
            setProfileImage(result.assets[0].uri);
        }
    };

    const handleImageChange = () => {
        setShowImageModal(true);
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 pb-[80px]"
        >
            <SafeAreaView className="flex-1">
                {/* Header */}
                <AppHeader title="Profile information" />

                {/* Profile Avatar Section */}
                <View className="items-center">
                    <TouchableOpacity
                        className="relative"
                        activeOpacity={0.7}
                        onPress={handleImageChange}
                    >
                        <Image
                            source={{
                                uri:
                                    profileImage ||
                                    "https://i.pravatar.cc/150?img=12",
                            }}
                            className="bg-white border-4 border-white rounded-full size-[100px]"
                        />
                        {/* Edit Button */}
                        <View className="right-0 bottom-0 absolute justify-center items-center bg-white rounded-full w-10 h-10">
                            <Ionicons name="camera" size={22} color="#4CAF50" />
                        </View>
                    </TouchableOpacity>

                    <Text className="mt-4 font-bold text-white text-lg">
                        Hoàng Trung Anh
                    </Text>
                    <Text className="mt-1 text-white/80 text-sm">
                        ID - 18826
                    </Text>
                </View>

                {/* Form Section */}
                <ScrollView className="flex-1 px-6">
                    {/* Full name */}
                    <View className="mt-6">
                        <Text className="mb-1 text-[#9BA1A6] text-sm">
                            Full name
                        </Text>
                        <TextInput
                            value={fullName}
                            onChangeText={setFullName}
                            className="pb-3 border-[#4A5568] border-b text-[#4CAF50] text-base"
                            placeholderTextColor="#9BA1A6"
                        />
                    </View>

                    {/* Email */}
                    <View className="mt-5">
                        <Text className="mb-4 text-[#9BA1A6] text-sm">
                            Email
                        </Text>
                        <View className="flex-row justify-between items-center pb-3 border-[#4A5568] border-b">
                            <Text className="text-[#4CAF50] text-base">
                                trunganh4002@gmail.com
                            </Text>
                            <Text className="text-[#9BA1A6] text-sm">
                                Read only
                            </Text>
                        </View>
                    </View>

                    {/* Phone number */}
                    <View className="mt-5">
                        <Text className="mb-3 text-[#9BA1A6] text-sm">
                            Phone number
                        </Text>
                        <View className="flex-row justify-between items-center pb-3 border-[#4A5568] border-b">
                            <Text className="text-[#4CAF50] text-base">
                                +84846779714
                            </Text>
                            <View className="flex-row items-center bg-[#4CAF50]/20 px-3 py-1 rounded-full">
                                <Ionicons
                                    name="checkmark-circle"
                                    size={16}
                                    color="#4CAF50"
                                />
                                <Text className="ml-1 text-[#4CAF50] text-sm">
                                    Verified
                                </Text>
                            </View>
                        </View>
                    </View>

                    {/* Bike brand */}
                    <Dropdown
                        label="Bike brand"
                        value={bikeBrand}
                        items={listBikeBrand}
                        onValueChange={(value) =>
                            setBikeBrand(value as BikeBrand)
                        }
                    />

                    <View className="h-10" />
                </ScrollView>
            </SafeAreaView>

            {/* Image Picker Modal */}
            <ImagePickerModal
                visible={showImageModal}
                onClose={() => setShowImageModal(false)}
                onTakePhoto={takePhoto}
                onChooseLibrary={pickImage}
            />
        </LinearGradient>
    );
}
