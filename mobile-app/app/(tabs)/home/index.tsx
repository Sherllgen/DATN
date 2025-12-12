import { useState } from "react";
import { Switch } from "react-native-paper";
import { SafeAreaView } from "react-native-safe-area-context";

export default function HomePage() {
    const [isSwitchOn, setIsSwitchOn] = useState(false);

    const onToggleSwitch = () => setIsSwitchOn(!isSwitchOn);
    return (
        <SafeAreaView>
            <Switch value={isSwitchOn} onValueChange={onToggleSwitch} />
        </SafeAreaView>
    );
}
