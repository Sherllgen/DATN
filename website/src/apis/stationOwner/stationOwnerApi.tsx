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
