package com.project.evgo.booking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BookingEventsTest {

    @Test
    @DisplayName("Test RequireRefundEvent for coverage")
    void requireRefundEvent_coverage() {
        RequireRefundEvent event1 = new RequireRefundEvent(1L, 2L, BigDecimal.valueOf(100), "Canceled");
        RequireRefundEvent event2 = new RequireRefundEvent(1L, 2L, BigDecimal.valueOf(100), "Canceled");

        assertThat(event1.bookingId()).isEqualTo(1L);
        assertThat(event1.userId()).isEqualTo(2L);
        assertThat(event1.amount()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(event1.reason()).isEqualTo("Canceled");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1.toString()).isNotNull();
    }

    @Test
    @DisplayName("Test BookingCreatedEvent for coverage")
    void bookingCreatedEvent_coverage() {
        BookingCreatedEvent event1 = new BookingCreatedEvent(1L, 2L, BigDecimal.valueOf(100));
        BookingCreatedEvent event2 = new BookingCreatedEvent(1L, 2L, BigDecimal.valueOf(100));

        assertThat(event1.bookingId()).isEqualTo(1L);
        assertThat(event1.userId()).isEqualTo(2L);
        assertThat(event1.totalPrice()).isEqualTo(BigDecimal.valueOf(100));

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
        assertThat(event1.toString()).isNotNull();
    }
}
