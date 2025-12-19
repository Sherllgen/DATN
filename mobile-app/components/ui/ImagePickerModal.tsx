import { Ionicons } from "@expo/vector-icons";
import { Modal, Text, TouchableOpacity, View } from "react-native";

interface ImagePickerModalProps {
    visible: boolean;
    onClose: () => void;
    onTakePhoto: () => void;
    onChooseLibrary: () => void;
}

export default function ImagePickerModal({
    visible,
    onClose,
    onTakePhoto,
    onChooseLibrary,
}: ImagePickerModalProps) {
    return (
        <Modal
            visible={visible}
            transparent={true}
            animationType="fade"
            onRequestClose={onClose}
        >
            <TouchableOpacity
                className="flex-1 justify-end bg-black/80"
                onPress={onClose}
                activeOpacity={1}
            >
                <TouchableOpacity
                    activeOpacity={1}
                    onPress={(e) => e.stopPropagation()}
                >
                    <View className="bg-[#2e2e2e] px-6 pt-6 pb-10 rounded-t-3xl">
                        <View className="items-center mb-6">
                            <View className="bg-[#4A5568] mb-3 rounded-full w-12 h-1" />
                            <Text className="font-semibold text-white text-lg">
                                Change Profile Picture
                            </Text>
                        </View>

                        <TouchableOpacity
                            className="flex-row items-center bg-[#3a3a3a] mb-3 px-4 py-4 rounded-xl"
                            onPress={onTakePhoto}
                            activeOpacity={0.7}
                        >
                            <View className="justify-center items-center bg-[#4CAF50]/20 mr-4 rounded-full w-12 h-12">
                                <Ionicons
                                    name="camera"
                                    size={24}
                                    color="#4CAF50"
                                />
                            </View>
                            <Text className="flex-1 font-medium text-white text-base">
                                Take Photo
                            </Text>
                            <Ionicons
                                name="chevron-forward"
                                size={20}
                                color="#9BA1A6"
                            />
                        </TouchableOpacity>

                        <TouchableOpacity
                            className="flex-row items-center bg-[#3a3a3a] mb-3 px-4 py-4 rounded-xl"
                            onPress={onChooseLibrary}
                            activeOpacity={0.7}
                        >
                            <View className="justify-center items-center bg-[#4CAF50]/20 mr-4 rounded-full w-12 h-12">
                                <Ionicons
                                    name="images"
                                    size={24}
                                    color="#4CAF50"
                                />
                            </View>
                            <Text className="flex-1 font-medium text-white text-base">
                                Choose from Library
                            </Text>
                            <Ionicons
                                name="chevron-forward"
                                size={20}
                                color="#9BA1A6"
                            />
                        </TouchableOpacity>

                        <TouchableOpacity
                            className="bg-[#3a3a3a] mt-2 px-4 py-4 rounded-xl"
                            onPress={onClose}
                            activeOpacity={0.7}
                        >
                            <Text className="font-semibold text-[#9BA1A6] text-base text-center">
                                Cancel
                            </Text>
                        </TouchableOpacity>
                    </View>
                </TouchableOpacity>
            </TouchableOpacity>
        </Modal>
    );
}
