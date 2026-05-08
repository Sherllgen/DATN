package com.project.evgo.payment.internal;

import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.events.RequireRefundEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequireRefundListenerTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private RequireRefundListener listener;

    @Test
    void testOnRequireRefund_InvoiceFound() {
        RequireRefundEvent event = new RequireRefundEvent(1L, 2L, BigDecimal.valueOf(500), "Canceled");
        Invoice invoice = new Invoice();
        invoice.setId(10L);
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.of(invoice));

        listener.onRequireRefund(event);

        assertEquals(InvoiceStatus.REFUNDED, invoice.getStatus());
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void testOnRequireRefund_InvoiceNotFound() {
        RequireRefundEvent event = new RequireRefundEvent(1L, 2L, BigDecimal.valueOf(500), "Canceled");

        when(invoiceRepository.findByBookingId(1L)).thenReturn(Optional.empty());

        listener.onRequireRefund(event);

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }
}
