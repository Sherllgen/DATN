import axiosInstance from "@/utils/axiosInstance";
import { InvoicePurpose } from "@/types/invoice.types";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;
import { PageResponse, ApiResponse, InvoiceResponse, PaymentOrderResponse } from "@/types/invoice.types";


export const getMyInvoices = async (
    status: 'UNPAID' | 'PAID',
    page: number,
    size: number,
    purpose?: InvoicePurpose
): Promise<PageResponse<InvoiceResponse>> => {
    const backendStatus = status === 'UNPAID' ? 'PENDING' : 'PAID';
    const purposeParam = purpose ? `&purpose=${purpose}` : '';
    const res = await axiosInstance.get<ApiResponse<PageResponse<InvoiceResponse>>>(
        `${API_BACKEND_URL}/api/v1/invoices/me?status=${backendStatus}&page=${page}&size=${size}${purposeParam}`
    );
    return res.data.data;
};

export const payInvoice = async (invoiceId: number): Promise<PaymentOrderResponse> => {
    const res = await axiosInstance.post<ApiResponse<PaymentOrderResponse>>(
        `${API_BACKEND_URL}/api/v1/zalopay/invoices/${invoiceId}/pay`
    );
    return res.data.data;
};

export const getInvoiceBySessionId = async (sessionId: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/session/${sessionId}`
    );
    return res.data.data;
};

export const getInvoiceByBookingId = async (bookingId: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/booking/${bookingId}`
    );
    return res.data.data;
};

export const getInvoiceById = async (id: number): Promise<InvoiceResponse> => {
    const res = await axiosInstance.get<ApiResponse<InvoiceResponse>>(
        `${API_BACKEND_URL}/api/v1/invoices/${id}`
    );
    return res.data.data;
};
