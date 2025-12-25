import { Modal, Text, TouchableOpacity, View } from "react-native";

interface DeleteConfirmModalProps {
    visible: boolean;
    onCancel: () => void;
    onConfirm: () => void;
    isDeleting?: boolean;
}

export default function DeleteConfirmModal({
    visible,
    onCancel,
    onConfirm,
    isDeleting = false,
}: DeleteConfirmModalProps) {
    return (
        <Modal
            visible={visible}
            animationType="fade"
            transparent={true}
            onRequestClose={onCancel}
        >
            <TouchableOpacity
                className="flex-1 justify-center items-center bg-black/70"
                onPress={onCancel}
                activeOpacity={1}
            >
                <TouchableOpacity
                    className="bg-[#292929] mx-6 p-6 rounded-2xl w-[85%]"
                    activeOpacity={1}
                >
                    <Text className="mb-2 font-bold text-white text-lg text-center">
                        Xóa phương tiện
                    </Text>
                    <Text className="mb-6 text-[#9BA1A6] text-sm text-center">
                        Bạn có chắc chắn muốn xóa phương tiện này không?
                    </Text>

                    <View className="flex-row gap-3">
                        <TouchableOpacity
                            className="flex-1 py-3 border border-gray-400 rounded-full"
                            activeOpacity={0.7}
                            onPress={onCancel}
                            disabled={isDeleting}
                        >
                            <Text className="font-semibold text-white text-base text-center">
                                Hủy
                            </Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            className={`flex-1 py-3 rounded-full ${
                                isDeleting ? "bg-red-500/50" : "bg-red-500"
                            }`}
                            activeOpacity={0.7}
                            onPress={onConfirm}
                            disabled={isDeleting}
                        >
                            <Text className="font-semibold text-white text-base text-center">
                                {isDeleting ? "Đang xóa..." : "Xóa"}
                            </Text>
                        </TouchableOpacity>
                    </View>
                </TouchableOpacity>
            </TouchableOpacity>
        </Modal>
    );
}
