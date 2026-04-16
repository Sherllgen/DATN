import axiosInstance from "@/utils/axiosInstance";
import { ApiResponse } from "@/types/booking.types";
import {
    StartChargingRequest,
    StopChargingRequest,
    ChargingSessionResponse
} from "@/types/charging.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export const startCharging = async (request: StartChargingRequest): Promise<ChargingSessionResponse> => {
    const res = await axiosInstance.post<ApiResponse<ChargingSessionResponse>>(
        `${API_BACKEND_URL}/api/v1/charging/start`,
        request
    );
    return res.data.data;
};

export const stopCharging = async (request: StopChargingRequest): Promise<void> => {
    await axiosInstance.post<ApiResponse<void>>(
        `${API_BACKEND_URL}/api/v1/charging/stop`,
        request
    );
};

export const getChargingSession = async (id: number): Promise<ChargingSessionResponse> => {
    const res = await axiosInstance.get<ApiResponse<ChargingSessionResponse>>(
        `${API_BACKEND_URL}/api/v1/charging/${id}`
    );
    return res.data.data;
};

export const getMyChargingSessions = async (userId: number): Promise<ChargingSessionResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<ChargingSessionResponse[]>>(
        `${API_BACKEND_URL}/api/v1/charging/user/${userId}`
    );
    return res.data.data;
};
