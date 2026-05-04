import axios from "axios";

/**
 * Get current user profile via Next.js API route
 * This proxies the request through Next.js server to include httpOnly cookies
 */
export async function getProfileApi() {
    const res = await axios.get("/api/user/profile", {
        withCredentials: true,
    });

    return res.data;
}

export interface GetOwnerBookingsParams {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}

export async function getOwnerBookingsApi(params?: GetOwnerBookingsParams) {
    const res = await axios.get("/api/stationOwner/bookings", {
        params,
        withCredentials: true,
    });

    return res.data;
}

export async function getOwnerBookingStatsApi() {
    const res = await axios.get("/api/stationOwner/stats/bookings", {
        withCredentials: true,
    });
    return res.data;
}

export async function getOwnerInvoiceStatsApi() {
    const res = await axios.get("/api/stationOwner/stats/invoices", {
        withCredentials: true,
    });
    return res.data;
}

export async function getOwnerStationStatsApi() {
    const res = await axios.get("/api/stationOwner/stats/stations", {
        withCredentials: true,
    });
    return res.data;
}

export async function getOwnerMonthlyChartApi() {
    const res = await axios.get("/api/stationOwner/monthly-chart", {
        withCredentials: true,
    });
    return res.data;
}
