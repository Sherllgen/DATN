import axiosInstance from "@/utils/axiosInstance";
import { ApiResponse } from "@/types/booking.types";
import { ChargerResponse } from "@/types/charger.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export const getChargersByStationId = async (stationId: number): Promise<ChargerResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<ChargerResponse[]>>(
        `${API_BACKEND_URL}/api/v1/chargers`,
        { params: { stationId } }
    );
    return res.data.data;
};
