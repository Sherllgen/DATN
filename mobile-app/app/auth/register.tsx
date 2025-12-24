import PhoneNumberStep from "@/components/auth/PhoneNumberStep";
import { sendRegisterOTP, validateRegister } from "@/utils/auth.logic";
import { LinearGradient } from "expo-linear-gradient";
import { useRouter } from "expo-router";
import React, { useState } from "react";
import { ScrollView } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Toast } from "toastify-react-native";

export interface RegisterDataProps {
    fullName: string;
    email: string;
    password: string;
    confirmPassword: string;
}

export default function RegisterScreen() {
    const [registerData, setRegisterData] = useState<RegisterDataProps>({
        fullName: "",
        email: "",
        password: "",
        confirmPassword: "",
    });

    const router = useRouter();

    const handleSendOTP = async () => {
        const error = validateRegister(registerData);

        if (error) {
            Toast.error(error);
            return;
        }

        const isSend = await sendRegisterOTP(registerData);

        if (isSend) {
            router.push({
                pathname: "/auth/otp-verify",
                params: {
                    email: registerData.email,
                },
            });
        }
    };

    return (
        <LinearGradient
            colors={["#33404F", "#000000"]}
            start={{ x: 0, y: 0 }}
            end={{ x: 0, y: 1 }}
            className="flex-1 px-12"
        >
            <SafeAreaView>
                <ScrollView
                    contentContainerClassName="flex-grow"
                    className="pt-10"
                    showsVerticalScrollIndicator={false}
                >
                    <PhoneNumberStep
                        registerData={registerData}
                        onRegisterDataChange={setRegisterData}
                        onContinue={handleSendOTP}
                    />
                </ScrollView>
            </SafeAreaView>
        </LinearGradient>
    );
}
