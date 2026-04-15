import axiosInstance from "@/utils/axiosInstance";
import { ApiResponse } from "@/types/booking.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export interface ZaloPayOrderRequest {
    invoiceId: number;
    userId: number;
    amount: number;
    description: string;
}

export interface InvoiceResponse {
    id: number;
    bookingId?: number;
    chargingSessionId?: number;
    userId: number;
    number: string;
    totalCost: number;
    purpose: string;
    status: string;
    createdAt?: string;
}

export interface ZaloPayOrderResponse {
    orderUrl: string;
    zpTransToken: string;
    appTransId: string;
}

/**
 * Creates a ZaloPay Sandbox order and returns App-to-App URLs
 */
export const createZaloPayOrder = async (request: ZaloPayOrderRequest): Promise<ZaloPayOrderResponse> => {
    const res = await axiosInstance.post<ApiResponse<ZaloPayOrderResponse>>(
        `${API_BACKEND_URL}/api/v1/zalopay/orders`,
        request
    );
    return res.data.data;
};

export const getInvoiceByBookingId = async (bookingId: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/booking/${bookingId}`
    );
    return res.data.data;
};

export const getInvoiceByChargingSessionId = async (sessionId: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/session/${sessionId}`
    );
    return res.data.data;
};

