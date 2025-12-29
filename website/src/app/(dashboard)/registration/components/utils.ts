import { OwnerType, ProfileStatus } from "@/types/profileRegistration";

export const getOwnerTypeColor = (role: OwnerType) => {
    switch (role) {
        case "ENTERPRISE":
            return "text-green-600 bg-green-50 dark:text-green-400 dark:bg-green-900/20";
        case "INDIVIDUAL":
            return "text-yellow-600 bg-yellow-50 dark:text-yellow-400 dark:bg-yellow-900/20";

        default:
            return "text-gray-600 bg-gray-50 dark:text-gray-400 dark:bg-gray-900/20";
    }
};

export const getStatusColor = (status: ProfileStatus) => {
    switch (status) {
        case "APPROVED":
            return "text-green-600 bg-green-50 dark:text-green-400 dark:bg-green-900/20";
        case "SUBMITTED":
            return "text-orange-600 bg-orange-50 dark:text-orange-400 dark:bg-orange-900/20";
        case "REJECTED":
            return "text-red-600 bg-red-50 dark:text-red-400 dark:bg-red-900/20";
        case "UNDER_REVIEW":
            return "text-purple-600 bg-purple-50 dark:text-purple-400 dark:bg-purple-900/20";
        default:
            return "text-gray-600 bg-gray-50 dark:text-gray-400 dark:bg-gray-900/20";
    }
};
