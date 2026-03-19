package com.project.evgo.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.evgo.config.ZaloPayConfig;
import com.project.evgo.payment.internal.Invoice;
import com.project.evgo.payment.internal.InvoiceRepository;
import com.project.evgo.payment.internal.Transaction;
import com.project.evgo.payment.internal.TransactionRepository;
import com.project.evgo.payment.internal.ZaloPayServiceImpl;
import com.project.evgo.payment.request.ZaloPayCallbackRequest;
import com.project.evgo.payment.request.ZaloPayOrderRequest;
import com.project.evgo.payment.response.ZaloPayOrderResponse;
import com.project.evgo.payment.response.ZaloPayStatusResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.InvoicePurpose;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.enums.TransactionStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ZaloPayServiceImpl.
 *
 * <p>
 * Design note: Transaction is the entity that owns ZaloPay gateway fields
 * (appTransId, zpTransToken, orderUrl). Invoice only stores the billing status.
 */
@ExtendWith(MockitoExtension.class)
class ZaloPayServiceTest {

    // --- Mocks ---
    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ZaloPayServiceImpl zaloPayService;
    private ZaloPayConfig config;
    private ObjectMapper objectMapper;

    private static final Long INVOICE_ID = 1L;
    private static final Long USER_ID = 42L;
    private static final BigDecimal AMOUNT = BigDecimal.valueOf(50000);

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        config = new ZaloPayConfig();
        config.setAppId("2553");
        config.setKey1("sdngKKJmqEMzvh5QQcdD2A9XBSKUNaYn");
        config.setKey2("trMrHtvjo6myautxDUiAcYsVtaeQ8nhf");
        config.setEndpoint("https://sb-openapi.zalopay.vn/v2");
        config.setCallbackUrl("https://example.ngrok-free.app/api/v1/zalopay/callback");

        zaloPayService = new ZaloPayServiceImpl(config, invoiceRepository, transactionRepository,
                objectMapper, webClient, eventPublisher);
    }

    // ===========================
    // createOrder tests
    // ===========================

    @Nested
    @DisplayName("createOrder Tests")
    class CreateOrderTests {

        @Test
        @DisplayName("Should create ZaloPay order and persist gateway tokens on Transaction")
        void createOrder_ValidRequest_ReturnsOrderResponseAndPersistsOnTransaction() {
            // Given
            ZaloPayOrderRequest request = new ZaloPayOrderRequest(
                    INVOICE_ID, USER_ID, AMOUNT, "Test payment");

            Invoice invoice = buildPendingInvoice();
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

            stubWebClient(
                    "{\"return_code\":1,\"order_url\":\"https://zalopay.vn/pay\",\"zp_trans_token\":\"token123\"}");

            // When
            ZaloPayOrderResponse result = zaloPayService.createOrder(request);

            // Then – response
            assertThat(result).isNotNull();
            assertThat(result.orderUrl()).isEqualTo("https://zalopay.vn/pay");
            assertThat(result.zpTransToken()).isEqualTo("token123");
            assertThat(result.appTransId()).isNotBlank();

            // Then – gateway tokens saved on Transaction (not Invoice)
            verify(transactionRepository).save(argThat(tx -> tx.getAppTransId() != null
                    && "https://zalopay.vn/pay".equals(tx.getOrderUrl())
                    && "token123".equals(tx.getZpTransToken())));

            verify(invoiceRepository).save(any(Invoice.class));
        }

        @Test
        @DisplayName("Should throw INVOICE_NOT_FOUND when invoice does not exist")
        void createOrder_InvoiceNotFound_ThrowsException() {
            ZaloPayOrderRequest request = new ZaloPayOrderRequest(INVOICE_ID, USER_ID, AMOUNT, null);
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> zaloPayService.createOrder(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVOICE_NOT_FOUND));

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw INVOICE_ALREADY_PAID when invoice is already paid")
        void createOrder_InvoiceAlreadyPaid_ThrowsException() {
            ZaloPayOrderRequest request = new ZaloPayOrderRequest(INVOICE_ID, USER_ID, AMOUNT, null);

            Invoice invoice = buildPendingInvoice();
            invoice.setStatus(InvoiceStatus.PAID);
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

            assertThatThrownBy(() -> zaloPayService.createOrder(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.INVOICE_ALREADY_PAID));

            verify(transactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ZALOPAY_ORDER_CREATION_FAILED when gateway returns error code")
        void createOrder_GatewayReturnsError_ThrowsException() {
            ZaloPayOrderRequest request = new ZaloPayOrderRequest(INVOICE_ID, USER_ID, AMOUNT, null);
            Invoice invoice = buildPendingInvoice();
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

            stubWebClient("{\"return_code\":-1,\"return_message\":\"Invalid app_id\"}");

            assertThatThrownBy(() -> zaloPayService.createOrder(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ZALOPAY_ORDER_CREATION_FAILED));
        }

        @Test
        @DisplayName("Should throw ZALOPAY_ORDER_CREATION_FAILED when gateway returns null body")
        void createOrder_NullGatewayResponse_ThrowsException() {
            ZaloPayOrderRequest request = new ZaloPayOrderRequest(INVOICE_ID, USER_ID, AMOUNT, null);
            Invoice invoice = buildPendingInvoice();
            when(invoiceRepository.findById(INVOICE_ID)).thenReturn(Optional.of(invoice));

            stubWebClientWithNull();

            assertThatThrownBy(() -> zaloPayService.createOrder(request))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ZALOPAY_ORDER_CREATION_FAILED));

            verify(transactionRepository, never()).save(any());
        }
    }

    // ===========================
    // handleCallback tests
    // ===========================

    @Nested
    @DisplayName("handleCallback Tests")
    class HandleCallbackTests {

        @Test
        @DisplayName("Valid callback: Invoice → PAID, Transaction → SUCCESS (looked up via Transaction.appTransId)")
        void handleCallback_ValidMac_UpdatesStatusToPaid() throws Exception {
            String appTransId = "260305_abc1def2";
            String dataJson = objectMapper.writeValueAsString(
                    java.util.Map.of("app_trans_id", appTransId,
                            "zp_trans_id", "zp_99999",
                            "return_code", 1));
            String mac = hmacSha256(config.getKey2(), dataJson);

            Invoice invoice = buildPendingInvoice();
            Transaction tx = buildPendingTransaction(invoice, appTransId);

            when(transactionRepository.findByAppTransId(appTransId)).thenReturn(Optional.of(tx));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

            zaloPayService.handleCallback(new ZaloPayCallbackRequest(dataJson, mac, 1));

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(tx.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
            assertThat(tx.getGatewayTransactionId()).isEqualTo("zp_99999");
            
            verify(eventPublisher).publishEvent(any(PaymentSuccessEvent.class));
        }

        @Test
        @DisplayName("Failed callback: Invoice → CANCELLED, Transaction → FAILED")
        void handleCallback_ReturnCodeFailed_UpdatesStatusToFailed() throws Exception {
            String appTransId = "260305_failtest";
            String dataJson = objectMapper.writeValueAsString(
                    java.util.Map.of("app_trans_id", appTransId,
                            "zp_trans_id", "",
                            "return_code", -1));
            String mac = hmacSha256(config.getKey2(), dataJson);

            Invoice invoice = buildPendingInvoice();
            Transaction tx = buildPendingTransaction(invoice, appTransId);

            when(transactionRepository.findByAppTransId(appTransId)).thenReturn(Optional.of(tx));
            when(invoiceRepository.save(any(Invoice.class))).thenReturn(invoice);
            when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

            zaloPayService.handleCallback(new ZaloPayCallbackRequest(dataJson, mac, 1));

            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
            assertThat(tx.getStatus()).isEqualTo(TransactionStatus.FAILED);
        }

        @Test
        @DisplayName("Should throw ZALOPAY_INVALID_CALLBACK_MAC when MAC does not match")
        void handleCallback_InvalidMac_ThrowsException() {
            ZaloPayCallbackRequest callbackRequest = new ZaloPayCallbackRequest(
                    "{\"app_trans_id\":\"fake\"}", "INVALID_MAC", 1);

            assertThatThrownBy(() -> zaloPayService.handleCallback(callbackRequest))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ZALOPAY_INVALID_CALLBACK_MAC));

            verify(transactionRepository, never()).findByAppTransId(anyString());
        }
    }

    // ===========================
    // queryOrderStatus tests
    // ===========================

    @Nested
    @DisplayName("queryOrderStatus Tests")
    class QueryOrderStatusTests {

        @Test
        @DisplayName("Should return PAID invoiceStatus when looked up via Transaction.appTransId")
        void queryOrderStatus_PaidInvoice_ReturnsPaidStatus() {
            String appTransId = "260305_paid";

            Invoice invoice = buildPendingInvoice();
            invoice.setStatus(InvoiceStatus.PAID);
            Transaction tx = buildPendingTransaction(invoice, appTransId);

            when(transactionRepository.findByAppTransId(appTransId)).thenReturn(Optional.of(tx));
            stubWebClient("{\"return_code\":1,\"return_message\":\"Giao dich thanh cong\"}");

            ZaloPayStatusResponse result = zaloPayService.queryOrderStatus(appTransId);

            assertThat(result.invoiceStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(result.returnCode()).isEqualTo(1);
            assertThat(result.appTransId()).isEqualTo(appTransId);
        }

        @Test
        @DisplayName("Should throw ZALOPAY_ORDER_NOT_FOUND when appTransId does not exist")
        void queryOrderStatus_NotFound_ThrowsException() {
            when(transactionRepository.findByAppTransId(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> zaloPayService.queryOrderStatus("260305_nonexistent"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getErrorCode())
                            .isEqualTo(ErrorCode.ZALOPAY_ORDER_NOT_FOUND));
        }
    }

    // ===========================
    // Test helpers
    // ===========================

    private Invoice buildPendingInvoice() {
        Invoice invoice = new Invoice();
        invoice.setId(INVOICE_ID);
        invoice.setUserId(USER_ID);
        invoice.setTotalCost(AMOUNT);
        invoice.setStatus(InvoiceStatus.PENDING);
        invoice.setPurpose(InvoicePurpose.BOOKING);
        return invoice;
    }

    /**
     * Builds a PENDING transaction linked to the given invoice with appTransId set.
     */
    private Transaction buildPendingTransaction(Invoice invoice, String appTransId) {
        Transaction tx = new Transaction();
        tx.setInvoice(invoice);
        tx.setAppTransId(appTransId);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setAmount(AMOUNT);
        return tx;
    }

    /** Stubs the full WebClient chain to return parsed JSON body. */
    @SuppressWarnings({ "unchecked" })
    private void stubWebClient(String responseJson) {
        try {
            java.util.Map<String, Object> parsed = objectMapper.readValue(responseJson,
                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {
                    });
            when(webClient.post()).thenReturn(requestBodyUriSpec);
            doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
            doReturn(requestHeadersSpec).when(requestBodySpec).body(any());
            doReturn(responseSpec).when(requestHeadersSpec).retrieve();
            when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
                    .thenReturn(Mono.just(parsed));
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Test setup failed: invalid JSON", e);
        }
    }

    /** Stubs the WebClient chain to return Mono.empty() → block() returns null. */
    @SuppressWarnings("unchecked")
    private void stubWebClientWithNull() {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(anyString());
        doReturn(requestHeadersSpec).when(requestBodySpec).body(any());
        doReturn(responseSpec).when(requestHeadersSpec).retrieve();
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(Mono.empty());
    }

    /** Replicates ZaloPayServiceImpl.hmacSha256 for generating valid test MACs. */
    private String hmacSha256(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
