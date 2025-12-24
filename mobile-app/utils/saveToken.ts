import * as SecureStore from "expo-secure-store";

// Access token chỉ lưu trong memory
let accessToken: string | null = null;

// ---- Access Token (RAM) ----
export const setAccessToken = (token: string | null) => {
    accessToken = token;
};

export const getAccessToken = () => {
    return accessToken;
};

export async function saveRefreshToken(token: string) {
    await SecureStore.setItemAsync("refresh_token", token);
}

export async function getRefreshToken() {
    return await SecureStore.getItemAsync("refresh_token");
}

export async function removeRefreshToken() {
    await SecureStore.deleteItemAsync("refresh_token");
}
