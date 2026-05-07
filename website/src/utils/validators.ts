// src/utils/validators.ts
export const isValidEmail = (email: string) =>
    /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email.trim());

export const validatePassword = (password: string) => {
    const errors: string[] = [];

    if (password.length < 8) {
        errors.push("Password must be at least 8 characters");
    }

    if (!/[0-9]/.test(password)) {
        errors.push("Password must contain at least one number");
    }

    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
        errors.push("Password must contain at least one special character");
    }

    return errors;
};
