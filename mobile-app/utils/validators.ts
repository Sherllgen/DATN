// src/utils/validators.ts
export const isValidEmail = (email: string) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.trim());

export const validatePassword = (password: string) => {
    const errors: string[] = [];

    if (password.length < 8) {
        errors.push("Mật khẩu phải có ít nhất 8 ký tự");
    }

    if (!/[0-9]/.test(password)) {
        errors.push("Mật khẩu phải chứa ít nhất một  số");
    }

    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
        errors.push("Mật khẩu phải chứa ít nhất một ký tự đặc biệt");
    }

    return errors;
};
