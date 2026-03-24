import axiosInstance from "@/utils/axiosInstance";
import {
    ApiResponse,
    PaginatedResponse,
    StationReview,
    StationReviewsSummary
} from "@/types/station.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

/**
 * Get review summary for a specific station (avg rating, total reviews, distribution)
 */
export const getReviewSummary = async (stationId: number): Promise<StationReviewsSummary> => {
    const res = await axiosInstance.get<ApiResponse<StationReviewsSummary>>(
        `${API_BACKEND_URL}/api/v1/reviews/station/${stationId}/summary`
    );
    return res.data.data;
};

/**
 * Get paginated and sorted review list for a specific station
 */
export const getStationReviews = async (
    stationId: number,
    params: { page?: number; size?: number; sort?: string } = {}
): Promise<PaginatedResponse<StationReview>> => {
    const res = await axiosInstance.get<ApiResponse<PaginatedResponse<StationReview>>>(
        `${API_BACKEND_URL}/api/v1/reviews/station/${stationId}`,
        { params }
    );
    return res.data.data;
};

/**
 * Submit a new review for a station
 */
export const createReview = async (
    stationId: number,
    data: { rating: number; comment: string }
): Promise<StationReview> => {
    const res = await axiosInstance.post<ApiResponse<StationReview>>(
        `${API_BACKEND_URL}/api/v1/reviews/station/${stationId}`,
        data
    );
    return res.data.data;
};

/**
 * Update an existing review
 */
export const updateReview = async (
    reviewId: number,
    data: { rating: number; comment: string }
): Promise<StationReview> => {
    const res = await axiosInstance.put<ApiResponse<StationReview>>(
        `${API_BACKEND_URL}/api/v1/reviews/${reviewId}`,
        data
    );
    return res.data.data;
};

/**
 * Delete a review by ID
 */
export const deleteReview = async (reviewId: number): Promise<void> => {
    await axiosInstance.delete(`${API_BACKEND_URL}/api/v1/reviews/${reviewId}`);
};
