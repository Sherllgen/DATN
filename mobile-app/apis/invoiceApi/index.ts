import axiosInstance from "@/utils/axiosInstance";

const API_BACKEND_URL = process.env.EXPO_PUBLIC_BACKEND_URL;
import { PageResponse, ApiResponse, InvoiceResponse, PaymentOrderResponse } from "@/types/invoice.types";

export const getMyInvoices = async (status: 'UNPAID' | 'PAID', page: number, size: number): Promise<PageResponse<InvoiceResponse>> => {
    const backendStatus = status === 'UNPAID' ? 'PENDING' : 'PAID';
    const res = await axiosInstance.get<ApiResponse<PageResponse<InvoiceResponse>>>(
        `${API_BACKEND_URL}/api/v1/invoices/me?status=${backendStatus}&page=${page}&size=${size}`
    );
    return res.data.data;
};

export const payInvoice = async (invoiceId: number): Promise<PaymentOrderResponse> => {
    const res = await axiosInstance.post<ApiResponse<PaymentOrderResponse>>(
        `${API_BACKEND_URL}/api/v1/zalopay/invoices/${invoiceId}/pay`
    );
    return res.data.data;
};
