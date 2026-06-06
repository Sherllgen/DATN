import axiosInstance from "@/utils/axiosInstance";
import {
    ApiResponse,
    DurationConfigResponse,
    CalendarStatusResponse,
    AvailableSlotResponse,
    CheckAvailabilityRequest,
    CreateBookingRequest,
    BookingResponse,
    PageResponse
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
    duration: number,
    portId?: number
): Promise<AvailableSlotResponse[]> => {
    const res = await axiosInstance.get<ApiResponse<AvailableSlotResponse[]>>(
        `${API_BACKEND_URL}/api/v1/bookings/available-slots`,
        {
            params: {
                stationId,
                date,
                duration,
                portId,
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
 * Get bookings for the currently authenticated user with pagination and status filtering
 */
export const getMyBookings = async (
    page: number = 0,
    size: number = 5,
    statuses?: string[]
): Promise<PageResponse<BookingResponse>> => {
    const statusParam = statuses && statuses.length > 0 ? `&statuses=${statuses.join(",")}` : "";
    const res = await axiosInstance.get<ApiResponse<PageResponse<BookingResponse>>>(
        `${API_BACKEND_URL}/api/v1/bookings/my?page=${page}&size=${size}${statusParam}`
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

/**
 * Cancel a booking by ID.
 * Backend enforces the rule: cancellation is only allowed for CONFIRMED/PENDING bookings
 * that are more than 2 hours before the start time.
 */
export const cancelBooking = async (bookingId: number): Promise<void> => {
    await axiosInstance.post<ApiResponse<void>>(
        `${API_BACKEND_URL}/api/v1/bookings/${bookingId}/cancel`
    );
};
