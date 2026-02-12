import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL || "http://localhost:8081";

export interface RouteResponse {
    encodedPolyline: string;
    distance: number; // meters
    duration: number; // seconds
}

export const getRoute = async (
    originLat: number,
    originLng: number,
    destLat: number,
    destLng: number
): Promise<RouteResponse> => {
    try {
        const response = await axiosInstance.get(`${API_BACKEND_URL}/api/v1/stations/directions`, {
            params: {
                originLat,
                originLng,
                destLat,
                destLng,
            },
        });
        // AxiosInstance likely returns the data directly or we need to check how it's configured.
        // stationApi.ts uses: return res.data.data;
        // checking stationApi.ts again.. yes it returns res.data.data
        // But axiosInstance.get<ApiResponse<T>> returns AxiosResponse<ApiResponse<T>>. 
        // So res.data is ApiResponse. res.data.data is the actual data.
        return response.data.data;
    } catch (error) {
        console.error("Error fetching route:", error);
        throw error;
    }
};
