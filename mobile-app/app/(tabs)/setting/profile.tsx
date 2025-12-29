import {
    getProfileApi,
    getUploadSignature,
    updateProfileApi,
    uploadAvatarApi,
} from "@/apis/profileApi/profileApi";
import AppHeader from "@/components/ui/AppHeader";
import Dropdown from "@/components/ui/Dropdown";
import ImagePickerModal from "@/components/ui/ImagePickerModal";
import { useUserStore } from "@/contexts/user.store";
import { logAxiosError } from "@/utils/errorLogger";
import { Ionicons } from "@expo/vector-icons";
import * as ImagePicker from "expo-image-picker";
import { LinearGradient } from "expo-linear-gradient";
import { useFocusEffect } from "expo-router";
import { useCallback, useState } from "react";

import {
    Image,
    RefreshControl,
    ScrollView,
    Text,
    TextInput,
    TouchableOpacity,
    View,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Toast } from "toastify-react-native";

type BikeBrand = "YAMAHA" | "HONDA" | "SUZUKI" | "VINFAST" | "OTHER";
type Gender = "MALE" | "FEMALE" | "OTHER";
type GenderItem = { label: Gender; value: Gender };

export default function ProfilePage() {
    const user = useUserStore((state) => state.user);
    const setUser = useUserStore((state) => state.setUser);
    const [isChanged, setIsChanged] = useState(false);
    const [refreshing, setRefreshing] = useState(false);
    const [state, setState] = useState({
        fullName: user?.fullName || "",
        gender: (user?.gender as Gender) || "",
        birthdate: user?.birthday || "",
        bikeBrand: "" as BikeBrand,
        profileImage: user?.avatarUrl || (null as string | null),
        showImageModal: false,
    });
    const [listGender] = useState<GenderItem[]>([
        { label: "MALE", value: "MALE" },
        { label: "FEMALE", value: "FEMALE" },
        { label: "OTHER", value: "OTHER" },
    ]);

    const pickImage = async () => {
        setState((prev) => ({ ...prev, showImageModal: false }));

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

        setIsChanged(true);

        if (!result.canceled && result.assets[0]) {
            const imageUri = result.assets[0].uri;
            setState((prev) => ({
                ...prev,
                profileImage: imageUri,
            }));
            await updateAvatar(imageUri);
        }
    };

    const takePhoto = async () => {
        setState((prev) => ({ ...prev, showImageModal: false }));

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

        setIsChanged(true);

        if (!result.canceled && result.assets[0]) {
            const imageUri = result.assets[0].uri;
            setState((prev) => ({
                ...prev,
                profileImage: imageUri,
            }));
            await updateAvatar(imageUri);
        }
    };

    const handleImageChange = () => {
        setState((prev) => ({ ...prev, showImageModal: true }));
    };

    const validateBirthdate = (dateString: string): boolean => {
        if (!dateString) return true;

        const selectedDate = new Date(dateString);
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (isNaN(selectedDate.getTime())) {
            Toast.error("Invalid date format");
            return false;
        }

        if (selectedDate >= today) {
            Toast.error("Invalid date format");
            return false;
        }

        return true;
    };

    const updateAvatar = async (imageUri: string) => {
        try {
            // Bước 1: Lấy signature từ backend
            const signatureRes = await getUploadSignature();
            const { signature, timestamp, apiKey, cloudName, folder } =
                signatureRes.data;

            // Bước 2: Upload lên Cloudinary
            const formData = new FormData();
            const imageFile = {
                uri: imageUri,
                type: "image/jpeg",
                name: "avatar.jpg",
            } as any;

            formData.append("file", imageFile);
            formData.append("signature", signature);
            formData.append("timestamp", timestamp);
            formData.append("api_key", apiKey);
            formData.append("folder", folder);

            console.log("Uploading to Cloudinary with:", {
                cloudName,
                folder,
                timestamp,
            });

            const cloudinaryResponse = await fetch(
                `https://api.cloudinary.com/v1_1/${cloudName}/image/upload`,
                {
                    method: "POST",
                    body: formData,
                }
            );

            const cloudinaryData = await cloudinaryResponse.json();
            console.log("Cloudinary response:", cloudinaryData);

            if (!cloudinaryResponse.ok) {
                console.error("Cloudinary error details:", cloudinaryData);
                throw new Error(
                    `Cloudinary upload failed: ${cloudinaryData.error?.message || "Unknown error"}`
                );
            }

            // Bước 3: Lưu URL vào database
            const { secure_url, public_id } = cloudinaryData;
            const res = await uploadAvatarApi(secure_url, public_id);

            if (res.status === 200 || res.status === 201) {
                setUser(res.data);
                Toast.success("Avatar updated successfully!");
                await fetchProfile();
            }
        } catch (error) {
            console.error("Upload avatar error:", error);
            logAxiosError(error);
            Toast.error("Failed to update avatar");
        }
    };

    const updateProfile = async () => {
        try {
            if (!validateBirthdate(state.birthdate)) {
                return;
            }

            const res = await updateProfileApi({
                fullName: state.fullName,
                gender: state.gender,
                birthday: state.birthdate,
            });
            console.log("update profile: ", res);
            if (res.status === 200 || res.status === 201) {
                fetchProfile();
                setUser(res.data);
                Toast.success("Update profile successfully!");
            }
        } catch (error) {
            logAxiosError(error);
        }
    };
    const fetchProfile = async () => {
        try {
            const res = await getProfileApi();

            setState((prev) => ({
                ...prev,
                fullName: res.data.fullName,
                gender: res.data.gender,
                birthdate: res.data.birthday,
                profileImage: res.data.avatarUrl,
                phoneNumber: res.data.phone,
            }));

            setIsChanged(false);
        } catch (error) {
            logAxiosError(error);
        }
    };

    useFocusEffect(
        useCallback(() => {
            fetchProfile();
        }, [])
    );

    console.log("state--------", state);

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
                                    state.profileImage ||
                                    "https://i.pravatar.cc/150?img=12",
                            }}
                            className="bg-white border-4 border-white rounded-full size-[100px]"
                        />
                        {/* Edit Button */}
                        <View className="right-0 bottom-0 absolute justify-center items-center bg-white rounded-full size-8">
                            <Ionicons name="camera" size={22} color="#4CAF50" />
                        </View>
                    </TouchableOpacity>
                </View>

                {isChanged && (
                    <View className="top-24 right-6 absolute items-end">
                        <TouchableOpacity
                            onPress={updateProfile}
                            activeOpacity={0.7}
                            className="bg-slate-300 px-4 py-2 rounded-lg w-24"
                        >
                            <Text>update</Text>
                        </TouchableOpacity>
                    </View>
                )}

                {/* Form Section */}
                <ScrollView
                    className="flex-1 mt-6 px-6"
                    refreshControl={
                        <RefreshControl
                            refreshing={refreshing}
                            onRefresh={async () => {
                                setRefreshing(true);
                                await fetchProfile();
                                setRefreshing(false);
                            }}
                            tintColor="#4CAF50"
                            colors={["#4CAF50"]}
                        />
                    }
                >
                    {/* Full name */}
                    <View className="mt-5">
                        <Text className="mb-1 text-[#9BA1A6] text-sm">
                            Full name
                        </Text>
                        <TextInput
                            value={state.fullName}
                            onChangeText={(text) => {
                                setState((prev) => ({
                                    ...prev,
                                    fullName: text,
                                }));
                                setIsChanged(true);
                            }}
                            className="pb-3 border-[#4A5568] border-b text-[#4CAF50] text-base"
                            placeholderTextColor="#9BA1A6"
                        />
                    </View>

                    {/* Gender */}
                    <Dropdown
                        label="Gender"
                        value={state.gender}
                        defaultValue={state.gender}
                        items={listGender}
                        onValueChange={(value) => {
                            setState((prev) => ({
                                ...prev,
                                gender: value as Gender,
                            }));
                            setIsChanged(true);
                        }}
                    />

                    {/* Birthdate */}
                    <View className="mt-4">
                        <Text className="mb-1 text-[#9BA1A6] text-sm">
                            Birthdate
                        </Text>
                        <TextInput
                            value={state.birthdate}
                            placeholder="yyyy-mm-dd"
                            onChangeText={(text) => {
                                setState((prev) => ({
                                    ...prev,
                                    birthdate: text,
                                }));
                                setIsChanged(true);
                            }}
                            className="pb-3 border-[#4A5568] border-b text-[#4CAF50] text-base"
                            placeholderTextColor="#9BA1A6"
                        />
                    </View>

                    {/* Email */}
                    <View className="mt-4">
                        <Text className="mb-4 text-[#9BA1A6] text-sm">
                            Email
                        </Text>
                        <View className="flex-row justify-between items-center pb-3 border-[#4A5568] border-b">
                            <Text className="text-[#4CAF50] text-base">
                                andrewainsley@gmail.com
                            </Text>
                            <Text className="text-[#9BA1A6] text-sm">
                                Read only
                            </Text>
                        </View>
                    </View>

                    {/* Phone number */}
                    <View className="mt-4">
                        <Text className="mb-3 text-[#9BA1A6] text-sm">
                            Phone number
                        </Text>
                        <View className="flex-row justify-between items-center pb-3 border-[#4A5568] border-b">
                            <Text className="text-[#4CAF50] text-base">
                                {user?.phone || "Chưa cập nhật"}
                            </Text>
                            {user?.phone && (
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
                            )}
                        </View>
                    </View>
                </ScrollView>
            </SafeAreaView>

            {/* Image Picker Modal */}
            <ImagePickerModal
                visible={state.showImageModal}
                onClose={() =>
                    setState((prev) => ({ ...prev, showImageModal: false }))
                }
                onTakePhoto={takePhoto}
                onChooseLibrary={pickImage}
            />
        </LinearGradient>
    );
}
