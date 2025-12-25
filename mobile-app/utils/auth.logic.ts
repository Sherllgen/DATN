import { registerApi } from "@/apis/authApi/authApi";
import { logAxiosError } from "@/utils/errorLogger";
import { isValidEmail, validatePassword } from "@/utils/validators";
import { RegisterDataProps } from "../app/auth/register";

export const validateRegister = (data: RegisterDataProps): string | null => {
    console.log("sjasfd");
    console.log("data: ", data);

    if (!data.fullName.trim()) return "Vui lòng nhập họ tên";
    if (!data.email.trim()) return "Vui lòng nhập email";
    if (!isValidEmail(data.email)) return "Email không hợp lệ";
    if (!data.password) return "Vui lòng nhập mật khẩu";

    const errors = validatePassword(data.password);
    if (errors.length) return errors.join("\n");

    if (data.password !== data.confirmPassword)
        return "Mật khẩu xác nhận không khớp";

    return null;
};

export const sendRegisterOTP = async (data: RegisterDataProps) => {
    try {
        const res = await registerApi(data.fullName, data.email, data.password);
        if (res.status === 200 || res.status === 201) {
            return true;
        }
    } catch (error: any) {
        logAxiosError(error);
    }
};
