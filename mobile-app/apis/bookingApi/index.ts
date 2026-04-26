import axiosInstance from "@/utils/axiosInstance";
import {
    ApiResponse,
    DurationConfigResponse,
    CalendarStatusResponse,
    AvailableSlotResponse,
    CheckAvailabilityRequest,
    CreateBookingRequest,
    BookingResponse
} from "@/types/booking.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

/**
 * Get configured charging durations (1.0 to 12.0 hours)
 */
export const getDurationsConfig = async (): Promise<DurationConfigResponse> => {
    const res = await axiosInstance.get<ApiResponse<DurationConfigResponse>>(
        `${API_BACKEND_URL}/api/v1/config/durations`
    );
    return res.data.data;
};

/**
 * Get availability status for all days in a specific month
 */
export const getCalendarStatus = async (
    stationId: number,
    year: number,
    month: number
): Promise<CalendarStatusResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<CalendarStatusResponse[]>>(
        `${API_BACKEND_URL}/api/v1/bookings/calendar-status`,
        {
            params: {
                stationId,
                month: `${year}-${month.toString().padStart(2, '0')}`,
            },
        }
    );
    return res.data.data;
};

/**
 * Get available time slots for a specific date and duration
 */
export const getAvailableSlots = async (
    stationId: number,
    date: string,
    duration: number
): Promise<AvailableSlotResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<AvailableSlotResponse[]>>(
        `${API_BACKEND_URL}/api/v1/bookings/available-slots`,
        {
            params: {
                stationId,
                date,
                duration,
            },
        }
    );
    return res.data.data;
};

/**
 * Check availability and create an 8-minute temporary Redis lock
 */
export const checkAvailability = async (request: CheckAvailabilityRequest): Promise<void> => {
    await axiosInstance.post<ApiResponse<void>>(
        `${API_BACKEND_URL}/api/v1/bookings/check-availability`,
        request
    );
};

/**
 * Create a PENDING booking
 */
export const createBooking = async (request: CreateBookingRequest): Promise<BookingResponse> => {
    const res = await axiosInstance.post<ApiResponse<BookingResponse>>(
        `${API_BACKEND_URL}/api/v1/bookings`,
        request
    );
    return res.data.data;
};

/**
 * Get all bookings for the currently authenticated user
 */
export const getMyBookings = async (): Promise<BookingResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<BookingResponse[]>>(
        `${API_BACKEND_URL}/api/v1/bookings/my`
    );
    return res.data.data;
};

/**
 * Get booking by ID
 */
export const getBookingById = async (id: string | number): Promise<BookingResponse> => {
    const res = await axiosInstance.get<ApiResponse<BookingResponse>>(
        `${API_BACKEND_URL}/api/v1/bookings/${id}`
    );
    return res.data.data;
};
