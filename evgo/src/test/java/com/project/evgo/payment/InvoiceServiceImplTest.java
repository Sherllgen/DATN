package com.project.evgo.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.project.evgo.payment.internal.Invoice;
import com.project.evgo.payment.internal.InvoiceDtoConverter;
import com.project.evgo.payment.internal.InvoiceRepository;
import com.project.evgo.payment.internal.InvoiceServiceImpl;
import com.project.evgo.payment.internal.TransactionRepository;
import com.project.evgo.payment.request.InvoiceCreatedRequest;
import com.project.evgo.payment.response.InvoiceResponse;
import com.project.evgo.payment.response.InvoiceStatsResponse;
import com.project.evgo.sharedkernel.dto.PageResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private InvoiceDtoConverter invoiceDtoConverter;

    @Mock
    private TransactionRepository transactionRepository;

    private static final Long INVOICE_ID = 1L;
    private static final Long BOOKING_ID = 100L;
    private static final Long SESSION_ID = 200L;
    private static final Long USER_ID = 5L;

    private Invoice testInvoice;
    private InvoiceResponse testResponse;

    @BeforeEach
    void setUp() {
        testInvoice = new Invoice();
        testInvoice.setId(INVOICE_ID);
        testInvoice.setBookingId(BOOKING_ID);
        testInvoice.setChargingSessionId(SESSION_ID);
        testInvoice.setUserId(USER_ID);
        testInvoice.setTotalCost(BigDecimal.valueOf(50000));
        testInvoice.setStatus(InvoiceStatus.PENDING);
        testInvoice.setPurpose(InvoicePurpose.BOOKING);

        testResponse = InvoiceResponse.builder().id(INVOICE_ID).status(InvoiceStatus.PENDING).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Find Invoice Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Find Invoice Tests")
    class FindInvoiceTests {

        @Test
        @DisplayName("findByBookingId should return invoice")
        void findByBookingId_Success() {
            when(invoiceRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testInvoice));
            when(invoiceDtoConverter.convert(testInvoice)).thenReturn(testResponse);

            InvoiceResponse result = invoiceService.findByBookingId(BOOKING_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(INVOICE_ID);
        }

        @Test
        @DisplayName("findByBookingId should throw when not found")
        void findByBookingId_NotFound() {
            when(invoiceRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.findByBookingId(BOOKING_ID))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVOICE_NOT_FOUND);
        }

        @Test
        @DisplayName("findByChargingSessionId should return invoice")
        void findByChargingSessionId_Success() {
            when(invoiceRepository.findByChargingSessionId(SESSION_ID)).thenReturn(Optional.of(testInvoice));
            when(invoiceDtoConverter.convert(testInvoice)).thenReturn(testResponse);

            InvoiceResponse result = invoiceService.findByChargingSessionId(SESSION_ID);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("findById should return invoice")
        void findById_Success() {
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(testInvoice));
            when(invoiceDtoConverter.convert(testInvoice)).thenReturn(testResponse);

            InvoiceResponse result = invoiceService.findById(INVOICE_ID);

            assertThat(result).isNotNull();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Create Invoice Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Create Invoice Tests")
    class CreateInvoiceTests {

        @Test
        @DisplayName("Should create invoice successfully")
        void createInvoice_Success() {
            InvoiceCreatedRequest request = new InvoiceCreatedRequest(BOOKING_ID, USER_ID, BigDecimal.valueOf(1000));
            when(invoiceRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.empty());

            invoiceService.createInvoice(request);

            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw exception if invoice for booking already exists")
        void createInvoice_AlreadyExists() {
            InvoiceCreatedRequest request = new InvoiceCreatedRequest(BOOKING_ID, USER_ID, BigDecimal.valueOf(1000));
            when(invoiceRepository.findByBookingId(BOOKING_ID)).thenReturn(Optional.of(testInvoice));

            assertThatThrownBy(() -> invoiceService.createInvoice(request))
                    .isInstanceOf(AppException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVOICE_ALREADY_EXIST);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getMyInvoices — purpose filter Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getMyInvoices — purpose filter Tests")
    class GetMyInvoicesTests {

        @Test
        @DisplayName("getMyInvoices with null purpose should call findByUserIdAndStatus (no filter)")
        void getMyInvoices_NullPurpose_CallsUnfilteredRepository() {
            Page<Invoice> page = new PageImpl<>(List.of(testInvoice));
            when(invoiceRepository.findByUserIdAndStatus(
                    eq(USER_ID), eq(InvoiceStatus.PENDING), any(Pageable.class)))
                    .thenReturn(page);
            when(invoiceDtoConverter.convert(testInvoice)).thenReturn(testResponse);

            PageResponse<InvoiceResponse> result = invoiceService.getMyInvoices(
                    USER_ID, InvoiceStatus.PENDING, null, 0, 10);

            assertThat(result.content()).hasSize(1);
            verify(invoiceRepository).findByUserIdAndStatus(
                    eq(USER_ID), eq(InvoiceStatus.PENDING), any(Pageable.class));
        }

        @Test
        @DisplayName("getMyInvoices with BOOKING purpose should call findByUserIdAndStatusAndPurpose")
        void getMyInvoices_BookingPurpose_CallsFilteredRepository() {
            Page<Invoice> page = new PageImpl<>(List.of(testInvoice));
            when(invoiceRepository.findByUserIdAndStatusAndPurpose(
                    eq(USER_ID), eq(InvoiceStatus.PENDING), eq(InvoicePurpose.BOOKING), any(Pageable.class)))
                    .thenReturn(page);
            when(invoiceDtoConverter.convert(testInvoice)).thenReturn(testResponse);

            PageResponse<InvoiceResponse> result = invoiceService.getMyInvoices(
                    USER_ID, InvoiceStatus.PENDING, InvoicePurpose.BOOKING, 0, 10);

            assertThat(result.content()).hasSize(1);
            verify(invoiceRepository).findByUserIdAndStatusAndPurpose(
                    eq(USER_ID), eq(InvoiceStatus.PENDING), eq(InvoicePurpose.BOOKING), any(Pageable.class));
        }

        @Test
        @DisplayName("getMyInvoices with IDLE_FEE purpose should call findByUserIdAndStatusAndPurpose")
        void getMyInvoices_IdleFeePurpose_CallsFilteredRepository() {
            Invoice idleFeeInvoice = new Invoice();
            idleFeeInvoice.setId(2L);
            idleFeeInvoice.setUserId(USER_ID);
            idleFeeInvoice.setPurpose(InvoicePurpose.IDLE_FEE);
            idleFeeInvoice.setStatus(InvoiceStatus.PENDING);
            idleFeeInvoice.setTotalCost(BigDecimal.valueOf(5000));

            InvoiceResponse idleFeeResponse = InvoiceResponse.builder()
                    .id(2L).status(InvoiceStatus.PENDING).build();

            Page<Invoice> page = new PageImpl<>(List.of(idleFeeInvoice));
            when(invoiceRepository.findByUserIdAndStatusAndPurpose(
                    eq(USER_ID), eq(InvoiceStatus.PENDING), eq(InvoicePurpose.IDLE_FEE), any(Pageable.class)))
                    .thenReturn(page);
            when(invoiceDtoConverter.convert(idleFeeInvoice)).thenReturn(idleFeeResponse);

            PageResponse<InvoiceResponse> result = invoiceService.getMyInvoices(
                    USER_ID, InvoiceStatus.PENDING, InvoicePurpose.IDLE_FEE, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).getId()).isEqualTo(2L);
            verify(invoiceRepository).findByUserIdAndStatusAndPurpose(
                    eq(USER_ID), eq(InvoiceStatus.PENDING), eq(InvoicePurpose.IDLE_FEE), any(Pageable.class));
        }

        @Test
        @DisplayName("getMyInvoices with CHARGING_SESSION purpose should call findByUserIdAndStatusAndPurpose")
        void getMyInvoices_ChargingSessionPurpose_CallsFilteredRepository() {
            Invoice sessionInvoice = new Invoice();
            sessionInvoice.setId(3L);
            sessionInvoice.setUserId(USER_ID);
            sessionInvoice.setPurpose(InvoicePurpose.CHARGING_SESSION);
            sessionInvoice.setStatus(InvoiceStatus.PAID);
            sessionInvoice.setTotalCost(BigDecimal.valueOf(30000));

            InvoiceResponse sessionResponse = InvoiceResponse.builder()
                    .id(3L).status(InvoiceStatus.PAID).build();

            Page<Invoice> page = new PageImpl<>(List.of(sessionInvoice));
            when(invoiceRepository.findByUserIdAndStatusAndPurpose(
                    eq(USER_ID), eq(InvoiceStatus.PAID), eq(InvoicePurpose.CHARGING_SESSION), any(Pageable.class)))
                    .thenReturn(page);
            when(invoiceDtoConverter.convert(sessionInvoice)).thenReturn(sessionResponse);

            PageResponse<InvoiceResponse> result = invoiceService.getMyInvoices(
                    USER_ID, InvoiceStatus.PAID, InvoicePurpose.CHARGING_SESSION, 0, 10);

            assertThat(result.content()).hasSize(1);
            verify(invoiceRepository).findByUserIdAndStatusAndPurpose(
                    eq(USER_ID), eq(InvoiceStatus.PAID), eq(InvoicePurpose.CHARGING_SESSION), any(Pageable.class));
        }

        @Test
        @DisplayName("getMyInvoices with null purpose and empty result returns empty page")
        void getMyInvoices_NullPurpose_EmptyResult() {
            Page<Invoice> emptyPage = new PageImpl<>(List.of());
            when(invoiceRepository.findByUserIdAndStatus(
                    eq(USER_ID), eq(InvoiceStatus.PAID), any(Pageable.class)))
                    .thenReturn(emptyPage);

            PageResponse<InvoiceResponse> result = invoiceService.getMyInvoices(
                    USER_ID, InvoiceStatus.PAID, null, 0, 10);

            assertThat(result.content()).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Miscellaneous Queries
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Miscellaneous Queries")
    class MiscQueriesTests {

        @Test
        @DisplayName("hasUnpaidInvoices should return true")
        void hasUnpaidInvoices_ReturnsTrue() {
            when(invoiceRepository.existsByUserIdAndStatus(USER_ID, InvoiceStatus.PENDING)).thenReturn(true);

            boolean result = invoiceService.hasUnpaidInvoices(USER_ID);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("hasUnpaidInvoices should return false when no unpaid invoices")
        void hasUnpaidInvoices_ReturnsFalse() {
            when(invoiceRepository.existsByUserIdAndStatus(USER_ID, InvoiceStatus.PENDING)).thenReturn(false);

            boolean result = invoiceService.hasUnpaidInvoices(USER_ID);

            assertThat(result).isFalse();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Statistics Tests
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("getStatsByBookingIds should return sum")
        void getStatsByBookingIds_ValidList() {
            List<Long> ids = List.of(1L, 2L);
            when(invoiceRepository.sumAmountByBookingIdInAndStatus(ids, InvoiceStatus.PAID))
                    .thenReturn(BigDecimal.valueOf(1000));

            InvoiceStatsResponse result = invoiceService.getStatsByBookingIds(ids);

            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.valueOf(1000));
        }

        @Test
        @DisplayName("getStatsByBookingIds should return zero for empty list")
        void getStatsByBookingIds_EmptyList() {
            InvoiceStatsResponse result = invoiceService.getStatsByBookingIds(List.of());

            assertThat(result.getTotalRevenue()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("getMonthlyRevenueByBookingIds should return mapped revenue")
        void getMonthlyRevenueByBookingIds_ValidList() {
            List<Long> ids = List.of(1L, 2L);
            Object[] row = new Object[]{5, BigDecimal.valueOf(2500)};
            when(invoiceRepository.sumMonthlyRevenueByBookingIdsAndYear(ids, InvoiceStatus.PAID, 2024))
                    .thenReturn(List.<Object[]>of(row));

            Map<Integer, BigDecimal> result = invoiceService.getMonthlyRevenueByBookingIds(ids, 2024);

            assertThat(result).containsEntry(5, BigDecimal.valueOf(2500));
        }

        @Test
        @DisplayName("getMonthlyRevenueByBookingIds should return empty map for empty list")
        void getMonthlyRevenueByBookingIds_EmptyList() {
            Map<Integer, BigDecimal> result = invoiceService.getMonthlyRevenueByBookingIds(List.of(), 2024);

            assertThat(result).isEmpty();
        }
    }
}
