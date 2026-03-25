import React from "react";
import { Calendar } from "react-native-calendars";
import Card from "@/components/ui/Card";
import { AppColors } from "@/constants/theme";

export interface BookingCalendarProps {
    selectedDate?: string;
    onDateSelect?: (date: string) => void;
    statusMap?: Record<string, 'AVAILABLE' | 'FULL' | 'UNAVAILABLE'>;
}

export default function BookingCalendar({
    selectedDate = new Date().toLocaleDateString('en-CA'),
    onDateSelect,
    statusMap = {}
}: BookingCalendarProps) {
    // Determine the max date to prevent infinite scrolling
    const today = new Date();
    const maxDate = new Date(today.getFullYear(), today.getMonth() + 2, 0).toLocaleDateString('en-CA'); // Max 2 months ahead

    // Build marked dates object combining selectedDate and statusMap
    const markedDatesData: any = {};
    
    // Add Status Dots
    Object.keys(statusMap).forEach(date => {
        const s = statusMap[date];
        if (s === 'AVAILABLE') {
            markedDatesData[date] = { marked: true, dotColor: AppColors.secondary };
        } else if (s === 'FULL') {
            markedDatesData[date] = { marked: true, dotColor: '#EF4444' }; // Red
        } else if (s === 'UNAVAILABLE') {
            markedDatesData[date] = { disableTouchEvent: true, disabled: true };
        }
    });

    if (selectedDate) {
        if (!markedDatesData[selectedDate]) markedDatesData[selectedDate] = {};
        markedDatesData[selectedDate] = {
            ...markedDatesData[selectedDate],
            selected: true,
            disableTouchEvent: true,
            selectedColor: AppColors.secondary,
            selectedTextColor: "#ffffff",
        };
    }

    return (
        <Card className="bg-surface-dark border border-border p-2 rounded-3xl mt-4">
            <Calendar
                current={selectedDate}
                minDate={today.toLocaleDateString('en-CA')}
                maxDate={maxDate}
                onDayPress={(day: any) => {
                    const status = statusMap[day.dateString];
                    if (status === 'UNAVAILABLE') return; // Prevent clicking unavailable directly just in case
                    onDateSelect?.(day.dateString);
                }}
                markedDates={markedDatesData}
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
