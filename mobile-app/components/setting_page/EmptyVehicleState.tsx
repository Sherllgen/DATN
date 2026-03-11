import { MaterialCommunityIcons } from "@expo/vector-icons";
import { Text, View } from "react-native";

export default function EmptyVehicleState() {
    return (
        <View className="justify-center items-center bg-border-gray/20 my-10 py-12 rounded-lg">
            <MaterialCommunityIcons
                name="motorbike-electric"
                size={70}
                color="#9BA1A6"
            />
            <Text className="mt-4 text-[#9BA1A6]">No vehicles added yet</Text>
            <Text className="mt-1 text-[#9BA1A6] text-sm">
                Tap the Add button to add a vehicle
            </Text>
        </View>
    );
}
