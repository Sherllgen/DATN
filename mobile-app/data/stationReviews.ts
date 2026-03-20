import { StationReview, StationReviewsSummary } from "@/types/station.types";

export const mockStationReviews: StationReview[] = [
    {
        id: 1,
        userName: "Darron Kulikowski",
        userAvatar: "https://i.pravatar.cc/150?u=darron",
        rating: 5,
        comment: "The station is well-lit and secure, which is important when charging my car at night 🌙",
        createdAt: "Today 08:04 AM",
    },
    {
        id: 2,
        userName: "Hannah Burress",
        userAvatar: "https://i.pravatar.cc/150?u=hannah",
        rating: 4,
        comment: "Love the fact that it's compatible with multiple EV models 🤩",
        createdAt: "12-28-2023 10:28 AM",
    },
    {
        id: 3,
        userName: "Edgar Torrey",
        userAvatar: "https://i.pravatar.cc/150?u=edgar",
        rating: 5,
        comment: "It's great to see businesses embracing electric vehicles by installing charging stations 👍",
        createdAt: "12-28-2023 12:39 PM",
    },
    {
        id: 4,
        userName: "Rodolfo Goode",
        userAvatar: "https://i.pravatar.cc/150?u=rodolfo",
        rating: 5,
        comment: "The station is user-friendly and intuitive, even for first-time users 😊",
        createdAt: "12-27-2023 16:53 PM",
    },
    {
        id: 5,
        userName: "Marielle Wigington",
        userAvatar: "https://i.pravatar.cc/150?u=marielle",
        rating: 3,
        comment: "Quick and easy to use, perfect for a quick charge while running errands 🔥",
        createdAt: "12-26-2023 14:30 PM",
    },
];

export const mockReviewsSummary: StationReviewsSummary = {
    averageRating: 4.5,
    totalReviews: 128,
    ratingDistribution: {
        5: 80,
        4: 30,
        3: 10,
        2: 5,
        1: 3,
    },
};
