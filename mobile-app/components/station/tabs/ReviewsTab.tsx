import React, { useState } from "react";
import { View, Text, TouchableOpacity, ScrollView } from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { mockStationReviews, mockReviewsSummary } from "@/data/stationReviews";
import Button from "@/components/ui/Button";
import Modal from "@/components/ui/Modal";
import StarRating from "@/components/ui/StarRating";
import Input from "@/components/ui/Input";
import Avatar from "@/components/ui/Avatar";
import { StationReview } from "@/types/station.types";

interface ReviewsTabProps {
    stationId: number;
}

const ReviewsTab = ({ stationId }: ReviewsTabProps) => {
    const [isIdShowingReviewModal, setIsIdShowingReviewModal] = useState(false);
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState("");

    const handleSubmitReview = () => {
        // Mock submission logic
        console.log("Submitting review:", { stationId, rating, comment });

        // Reset and close
        setRating(0);
        setComment("");
        setIsIdShowingReviewModal(false);
    };

    return (
        <View className="pb-4">
            {/* Rating Summary Card */}
            <View className="bg-white/5 rounded-lg p-6 border border-white/10 mb-8 flex-row items-center">
                <View className="items-center justify-center mr-8">
                    <Text className="text-5xl font-bold text-white mb-2">
                        {mockReviewsSummary.averageRating.toFixed(1)}
                    </Text>
                    <View className="flex-row mb-1">
                        {[1, 2, 3, 4, 5].map((star) => (
                            <Ionicons
                                key={star}
                                name={star <= Math.floor(mockReviewsSummary.averageRating) ? "star" : "star-half"}
                                size={16}
                                color="#F59E0B"
                            />
                        ))}
                    </View>
                    <Text className="text-xs text-[#9BA1A6]">
                        ({mockReviewsSummary.totalReviews} reviews)
                    </Text>
                </View>

                {/* Rating Distribution Bars */}
                <View className="flex-1 gap-y-1.5">
                    {[5, 4, 3, 2, 1].map((ratingItem) => {
                        const count = mockReviewsSummary.ratingDistribution[ratingItem as keyof typeof mockReviewsSummary.ratingDistribution];
                        const percentage = (count / mockReviewsSummary.totalReviews) * 100;
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

            {/* Sort by Header */}
            <View className="flex-row justify-between items-center mb-6">
                <Text className="text-lg font-bold text-white">Sort by</Text>
                <TouchableOpacity className="flex-row items-center" activeOpacity={0.7}>
                    <Text className="text-secondary font-medium mr-2">Newest</Text>
                    <Ionicons name="swap-vertical" size={18} color="#00A452" />
                </TouchableOpacity>
            </View>

            {/* Reviews List - Render items directly within the outer ScrollView */}
            <View className="border-t border-white/10 pt-4">
                {mockStationReviews.map((review: StationReview) => (
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
                                <Text className="text-[10px] text-white/40 mb-1">{review.createdAt.split(' ')[0]}</Text>
                                <Text className="text-[10px] text-white/40">{review.createdAt.split(' ').slice(1).join(' ')}</Text>
                            </View>
                        </View>
                        <Text className="text-[#9BA1A6] leading-5">
                            {review.comment}
                        </Text>
                    </View>
                ))}
            </View>

            {/* Write Review Button */}
            <Button
                size="lg"
                variant="primary"
                fullWidth
                style={{ height: 52 }}
                onPress={() => setIsIdShowingReviewModal(true)}
                className="shadow-lg shadow-secondary/20"
            >
                Write a Review
            </Button>

            {/* Review Bottom Sheet */}
            <Modal
                visible={isIdShowingReviewModal}
                onClose={() => setIsIdShowingReviewModal(false)}
                title="Write a Review"
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
                            style={{ height: 52 }}
                            size="lg"
                            fullWidth
                            onPress={handleSubmitReview}
                            disabled={rating === 0}
                            className={rating === 0 ? "opacity-50" : ""}
                        >
                            Submit Review
                        </Button>
                    </View>
                </ScrollView>
            </Modal>
        </View>
    );
};

export default ReviewsTab;
