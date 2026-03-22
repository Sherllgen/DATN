import React, { useState, useEffect, useCallback } from "react";
import { View, Text, TouchableOpacity, ScrollView, ActivityIndicator, Alert } from "react-native";
import { router } from "expo-router";
import { Ionicons } from "@expo/vector-icons";
import { getReviewSummary, getStationReviews, createReview, updateReview, deleteReview } from "@/apis/reviewApi/reviewApi";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import StarRating from "@/components/ui/StarRating";
import Input from "@/components/ui/Input";
import Avatar from "@/components/ui/Avatar";
import { StationReview, StationReviewsSummary } from "@/types/station.types";
import { useAuthStore } from "@/contexts/auth.store";

interface ReviewsTabProps {
    stationId: number;
}

const ReviewsTab = ({ stationId }: ReviewsTabProps) => {
    const [isIdShowingReviewModal, setIsIdShowingReviewModal] = useState(false);
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState("");

    // API Data State
    const [summary, setSummary] = useState<StationReviewsSummary | null>(null);
    const [reviews, setReviews] = useState<StationReview[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isLoadingMore, setIsLoadingMore] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const [sortOrder, setSortOrder] = useState<"desc" | "asc">("desc");
    const [editingReviewId, setEditingReviewId] = useState<number | null>(null);

    const fetchSummary = useCallback(async () => {
        try {
            const data = await getReviewSummary(stationId);
            setSummary(data);
        } catch (error) {
            console.error("Error fetching review summary:", error);
        }
    }, [stationId]);

    const fetchReviews = useCallback(async (pageNum: number, isInitial = false) => {
        try {
            if (isInitial) setIsLoading(true);
            else setIsLoadingMore(true);

            const data = await getStationReviews(stationId, { 
                page: pageNum, 
                size: 10,
                sort: `createdAt,${sortOrder}` 
            });

            if (isInitial) {
                setReviews(data.content);
            } else {
                setReviews(prev => [...prev, ...data.content]);
            }

            setHasMore(!data.last);
            setPage(data.page);
        } catch (error) {
            console.error("Error fetching reviews:", error);
        } finally {
            setIsLoading(false);
            setIsLoadingMore(false);
        }
    }, [stationId, sortOrder]);

    useEffect(() => {
        fetchSummary();
        fetchReviews(0, true);
    }, [fetchSummary, fetchReviews]);

    const handleLoadMore = () => {
        if (!isLoadingMore && hasMore) {
            fetchReviews(page + 1);
        }
    };

    const handleSubmitReview = async () => {
        if (rating === 0) return;

        try {
            setIsSubmitting(true);
            
            if (editingReviewId) {
                await updateReview(editingReviewId, { rating, comment });
            } else {
                await createReview(stationId, { rating, comment });
            }

            // Reset and close
            setRating(0);
            setComment("");
            setEditingReviewId(null);
            setIsIdShowingReviewModal(false);

            // Refresh data
            fetchSummary();
            fetchReviews(0, true);

            Alert.alert("Success", editingReviewId ? "Your review has been updated successfully!" : "Your review has been submitted successfully!");
        } catch (error) {
            console.error("Error submitting review:", error);
            Alert.alert("Error", "Failed to submit review. Please try again.");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeleteReview = (reviewId: number) => {
        Alert.alert(
            "Delete Review",
            "Are you sure you want to delete this review? This action cannot be undone.",
            [
                { text: "Cancel", style: "cancel" },
                {
                    text: "Delete",
                    style: "destructive",
                    onPress: async () => {
                        try {
                            await deleteReview(reviewId);
                            // Refresh data
                            fetchSummary();
                            fetchReviews(0, true);
                            Alert.alert("Success", "Review deleted successfully");
                        } catch (error) {
                            console.error("Error deleting review:", error);
                            Alert.alert("Error", "Failed to delete review. Please try again.");
                        }
                    }
                }
            ]
        );
    };

    if (isLoading && page === 0) {
        return (
            <View className="py-10 items-center">
                <ActivityIndicator size="large" color="#00A452" />
            </View>
        );
    }

    return (
        <View className="pb-4">
            {/* Rating Summary Card */}
            {summary && (
                <View className="bg-white/5 rounded-lg p-6 border border-white/10 mb-8 flex-row items-center">
                    <View className="items-center justify-center mr-8">
                        <Text className="text-5xl font-bold text-white mb-2">
                            {summary.averageRating?.toFixed(1) || "0.0"}
                        </Text>
                        <View className="flex-row mb-1">
                            {[1, 2, 3, 4, 5].map((star) => (
                                <Ionicons
                                    key={star}
                                    name={star <= Math.floor(summary.averageRating || 0) ? "star" : (star - 0.5 <= (summary.averageRating || 0) ? "star-half" : "star-outline")}
                                    size={16}
                                    color="#F59E0B"
                                />
                            ))}
                        </View>
                        <Text className="text-xs text-[#9BA1A6]">
                            ({summary.totalReviews || 0} reviews)
                        </Text>
                    </View>

                    {/* Rating Distribution Bars */}
                    <View className="flex-1 gap-y-1.5">
                        {[5, 4, 3, 2, 1].map((ratingItem) => {
                            const count = summary.ratingDistribution[ratingItem] || 0;
                            const percentage = summary.totalReviews > 0 ? (count / summary.totalReviews) * 100 : 0;
                            return (
                                <View key={ratingItem} className="flex-row items-center">
                                    <Text className="text-[10px] text-white/60 w-3 mr-2">{ratingItem}</Text>
                                    <View className="flex-1 h-1.5 bg-white/10 rounded-full overflow-hidden">
                                        <View
                                            className="h-full bg-secondary rounded-full"
                                            style={{ width: `${percentage}%` }}
                                        />
                                    </View>
                                </View>
                            );
                        })}
                    </View>
                </View>
            )}

            {/* Sort by Header */}
            <View className="flex-row justify-between items-center mb-6">
                <Text className="text-lg font-bold text-white">Sort by</Text>
                <TouchableOpacity 
                    className="flex-row items-center" 
                    activeOpacity={0.7}
                    onPress={() => setSortOrder(prev => prev === "desc" ? "asc" : "desc")}
                >
                    <Text className="text-secondary font-medium mr-2">
                        {sortOrder === "desc" ? "Most Recent" : "Oldest First"}
                    </Text>
                    <Ionicons name="swap-vertical" size={18} color="#00A452" />
                </TouchableOpacity>
            </View>

            {/* Reviews List */}
            <View className="border-t border-white/10 pt-4">
                {reviews.length === 0 ? (
                    <View className="py-8 items-center">
                        <Text className="text-white/40 text-base italic">No reviews yet. Be the first!</Text>
                    </View>
                ) : (
                    reviews.map((review: StationReview) => (
                        <View key={review.id.toString()} className="border-b border-white/5 pb-6 mb-6">
                            <View className="flex-row justify-between items-start mb-3">
                                <View className="flex-row items-center">
                                    <Avatar
                                        source={review.userAvatar}
                                        size="md"
                                        className="mr-3"
                                    />
                                    <View>
                                        <Text className="text-base font-bold text-white mb-1">
                                            {review.userName}
                                        </Text>
                                        <View className="flex-row">
                                            {[1, 2, 3, 4, 5].map((star) => (
                                                <Ionicons
                                                    key={star}
                                                    name={star <= review.rating ? "star" : "star-outline"}
                                                    size={13}
                                                    color="#F59E0B"
                                                    className="mr-0.5"
                                                />
                                            ))}
                                            <Text className="text-[12px] text-white/40 ml-1">({review.rating})</Text>
                                        </View>
                                    </View>
                                </View>
                                <View className="items-end">
                                    <Text className="text-[10px] text-white/40 mb-1">{(review.updatedAt || review.createdAt).split('T')[0]}</Text>
                                    <Text className="text-[10px] text-white/40 mb-2">{(review.updatedAt || review.createdAt).includes('T') ? (review.updatedAt || review.createdAt).split('T')[1].substring(0, 5) : ""}</Text>
                                    {review.isOwner && (
                                        <TouchableOpacity
                                            onPress={() => {
                                                Alert.alert(
                                                    "Review Options",
                                                    "Choose an action for your review:",
                                                    [
                                                        { text: "Cancel", style: "cancel" },
                                                        { text: "Edit", onPress: () => {
                                                            setEditingReviewId(review.id);
                                                            setRating(review.rating);
                                                            setComment(review.comment);
                                                            setIsIdShowingReviewModal(true);
                                                        } },
                                                        { text: "Delete", style: "destructive", onPress: () => handleDeleteReview(review.id) }
                                                    ]
                                                );
                                            }}
                                            activeOpacity={0.7}
                                        >
                                            <Ionicons name="ellipsis-horizontal" size={20} color="#9BA1A6" />
                                        </TouchableOpacity>
                                    )}
                                </View>
                            </View>
                            <Text className="text-[#9BA1A6] leading-5">
                                {review.comment}
                            </Text>
                        </View>
                    ))
                )}

                {/* Infinite Scroll / Load More Trigger */}
                {hasMore && (
                    <TouchableOpacity
                        className="py-4 items-center mb-8 border border-white/10 rounded-lg"
                        onPress={handleLoadMore}
                        disabled={isLoadingMore}
                        activeOpacity={0.7}
                    >
                        {isLoadingMore ? (
                            <ActivityIndicator size="small" color="#00A452" />
                        ) : (
                            <Text className="text-secondary font-medium">Load More Reviews</Text>
                        )}
                    </TouchableOpacity>
                )}
            </View>

            {/* Write Review Button */}
            <Button
                variant="primary"
                fullWidth
                // style={{ height: 52 }}
                size="lg"
                onPress={() => {
                    const token = useAuthStore.getState().accessToken;
                    if (!token) {
                        Alert.alert(
                            "Authentication Required",
                            "Please login to write a review.",
                            [
                                { text: "Cancel", style: "cancel" },
                                { text: "Login", onPress: () => router.push("/auth/login") }
                            ]
                        );
                        return;
                    }
                    setEditingReviewId(null);
                    setRating(0);
                    setComment("");
                    setIsIdShowingReviewModal(true);
                }}
                className="shadow-lg shadow-secondary/20"
            >
                Write a Review
            </Button>

            {/* Review Bottom Sheet */}
            <Modal
                visible={isIdShowingReviewModal}
                onClose={() => {
                    setIsIdShowingReviewModal(false);
                    setEditingReviewId(null);
                    setRating(0);
                    setComment("");
                }}
                title={editingReviewId ? "Edit Review" : "Write a Review"}
                variant="bottom-sheet"
            >
                <ScrollView
                    showsVerticalScrollIndicator={false}
                    keyboardShouldPersistTaps="handled"
                >
                    <View className="gap-y-6 pb-6">
                        <View className="items-center">
                            <Text className="text-[#9BA1A6] text-sm mb-4">
                                How would you rate your experience?
                            </Text>
                            <StarRating
                                rating={rating}
                                onRatingChange={setRating}
                                size={40}
                            />
                        </View>

                        <Input
                            label="Your Comment"
                            placeholder="Share your experience at this station..."
                            multiline
                            numberOfLines={4}
                            value={comment}
                            onChangeText={setComment}
                            variant="outline"
                            inputClassName="h-32 text-start pt-4"
                            textAlignVertical="top"
                        />

                        <Button
                            variant="primary"
                            size="lg"
                            // style={{ height: 52 }}
                            fullWidth
                            onPress={handleSubmitReview}
                            disabled={rating === 0 || isSubmitting}
                            className={rating === 0 || isSubmitting ? "opacity-50" : ""}
                        >
                            {isSubmitting ? (
                                <ActivityIndicator size="small" color="white" />
                            ) : (
                                editingReviewId ? "Update Review" : "Submit Review"
                            )}
                        </Button>
                    </View>
                </ScrollView>
            </Modal>
        </View>
    );
};

export default ReviewsTab;
