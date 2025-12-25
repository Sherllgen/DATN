import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export async function getAllVehicleApi() {
    const res = await axiosInstance.get(
        `${API_BACKEND_URL}/api/v1/users/me/vehicles`
    );

    return res.data;
}

export async function addVehicleApi(
    brand: string,
    modelName: string,
    connectorTypes: string[]
) {
    const payload = { brand, modelName, connectorTypes };
    console.log(
        "📤 Adding vehicle with payload:",
        JSON.stringify(payload, null, 2)
    );

    const res = await axiosInstance.post(
        `${API_BACKEND_URL}/api/v1/users/me/vehicles`,
        payload
    );

    return res.data;
}

export async function deleteVehicleApi(vehicleId: string) {
    const res = await axiosInstance.delete(
        `${API_BACKEND_URL}/api/v1/users/me/vehicles/${vehicleId}`
    );
    return res.data;
}

export async function updateVehicleApi(
    vehicleId: string,
    brand: string,
    modelName: string,
    connectorTypes: string[]
) {
    const payload = { brand, modelName, connectorTypes };

    const res = await axiosInstance.put(
        `${API_BACKEND_URL}/api/v1/users/me/vehicles/${vehicleId}`,
        payload
    );
    return res.data;
}
