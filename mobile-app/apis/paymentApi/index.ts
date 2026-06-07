import axiosInstance from "@/utils/axiosInstance";
import { ApiResponse } from "@/types/booking.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;

export interface ZaloPayOrderRequest {
    invoiceId: number;
    userId: number;
    amount: number;
    description: string;
    redirectUrl?: string;
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

import * as ExpoLinking from "expo-linking";

/**
 * Creates a ZaloPay Sandbox order and returns App-to-App URLs
 */
export const createZaloPayOrder = async (request: ZaloPayOrderRequest): Promise<ZaloPayOrderResponse> => {
    // Automatically inject a deep link redirect URL if not provided
    // This dynamically handles both Expo Go (exp://) and Standalone build (com.evgo.mobile://)
    if (!request.redirectUrl) {
        request.redirectUrl = ExpoLinking.createURL('booking');
    }

    const res = await axiosInstance.post<ApiResponse<ZaloPayOrderResponse>>(
        `${API_BACKEND_URL}/api/v1/zalopay/orders`,
        request
    );
    return res.data.data;
};

import { Linking, Platform } from "react-native";

/**
 * Handles ZaloPay Payment by opening the Web Gateway.
 * Note: Direct App-to-App deep linking (zalopay://app) without the official ZPDK SDK 
 * is blocked by ZaloPay and will only open the ZaloPay home screen.
 * The Web Gateway handles opening the ZaloPay app securely.
 */
export const processZaloPayPayment = async (order: ZaloPayOrderResponse) => {
    if (order.orderUrl) {
        await Linking.openURL(order.orderUrl);
    }
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

export const checkUnpaidInvoices = async (): Promise<boolean> => {
    const res = await axiosInstance.get<ApiResponse<boolean>>(
        `${API_BACKEND_URL}/api/v1/invoices/unpaid/check`
    );
    return res.data.data;
};
