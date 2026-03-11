import React from "react";
import { Calendar } from "react-native-calendars";
import Card from "@/components/ui/Card";
import { AppColors } from "@/constants/theme";

export interface BookingCalendarProps {
    selectedDate?: string;
    onDateSelect?: (date: string) => void;
}

export default function BookingCalendar({
    selectedDate = "2024-12-17",
    onDateSelect,
}: BookingCalendarProps) {
    return (
        <Card className="bg-surface-dark border border-border p-2 rounded-3xl mt-4">
            <Calendar
                current={selectedDate}
                onDayPress={(day) => {
                    onDateSelect?.(day.dateString);
                }}
                markedDates={{
                    [selectedDate]: {
                        selected: true,
                        disableTouchEvent: true,
                        selectedColor: AppColors.secondary,
                        selectedTextColor: "#ffffff",
                    },
                }}
                theme={{
                    backgroundColor: AppColors.surfaceDark,
                    calendarBackground: AppColors.surfaceDark,
                    textSectionTitleColor: "#ffffff",
                    selectedDayBackgroundColor: AppColors.secondary,
                    selectedDayTextColor: "#ffffff",
                    todayTextColor: AppColors.secondary,
                    dayTextColor: AppColors.textSecondary,
                    textDisabledColor: AppColors.borderGray,
                    dotColor: AppColors.secondary,
                    selectedDotColor: "#ffffff",
                    arrowColor: AppColors.secondary,
                    disabledArrowColor: AppColors.borderGray,
                    monthTextColor: "#ffffff",
                    indicatorColor: AppColors.secondary,
                    textDayFontWeight: "500",
                    textMonthFontWeight: "600",
                    textDayHeaderFontWeight: "500",
                    textDayFontSize: 16,
                    textMonthFontSize: 18,
                    textDayHeaderFontSize: 14,
                }}
                enableSwipeMonths={true}
            />
        </Card>
    );
}
