package com.project.evgo.payment.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.evgo.config.ZaloPayConfig;
import com.project.evgo.payment.ZaloPayService;
import com.project.evgo.payment.request.ZaloPayCallbackRequest;
import com.project.evgo.payment.request.ZaloPayOrderRequest;
import com.project.evgo.payment.response.ZaloPayOrderResponse;
import com.project.evgo.payment.response.ZaloPayStatusResponse;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.InvoiceStatus;
import com.project.evgo.sharedkernel.enums.PaymentMethod;
import com.project.evgo.sharedkernel.enums.TransactionStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.context.ApplicationEventPublisher;
import com.project.evgo.payment.PaymentSuccessEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

/**
 * ZaloPay service implementation.
 * Handles order creation, callback verification, and order status queries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloPayServiceImpl implements ZaloPayService {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int ZALOPAY_SUCCESS_CODE = 1;
    private static final DateTimeFormatter APP_TRANS_DATE_FMT = DateTimeFormatter.ofPattern("yyMMdd")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final ZaloPayConfig zaloPayConfig;
    private final InvoiceRepository invoiceRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ZaloPayOrderResponse createOrder(ZaloPayOrderRequest request) {
        Invoice invoice = invoiceRepository.findById(request.invoiceId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        String appTransId = buildAppTransId();
        long appTime = Instant.now().toEpochMilli();
        String embedData = "{}";
        String item = "[]";
        String appUser = String.valueOf(request.userId());
        long amount = request.amount().longValue();

        String macData = String.join("|",
                zaloPayConfig.getAppId(),
                appTransId,
                appUser,
                String.valueOf(amount),
                String.valueOf(appTime),
                embedData,
                item);

        String mac = hmacSha256(zaloPayConfig.getKey1(), macData);

        // log.info("MAC Data: {}", macData);
        // log.info("Generated MAC: {}", mac);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("app_id", zaloPayConfig.getAppId());
        body.add("app_trans_id", appTransId);
        body.add("app_user", appUser);
        body.add("amount", String.valueOf(amount));
        body.add("app_time", String.valueOf(appTime));
        body.add("embed_data", embedData);
        body.add("item", item);
        body.add("description", request.description() != null ? request.description()
                : "EV-Go payment for invoice #" + request.invoiceId());
        body.add("callback_url", zaloPayConfig.getCallbackUrl());
        body.add("mac", mac);

        Map<String, Object> response = webClient.post()
                .uri(zaloPayConfig.getEndpoint() + "/create")
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        if (response == null) {
            throw new AppException(ErrorCode.ZALOPAY_ORDER_CREATION_FAILED);
        }

        int returnCode = ((Number) response.getOrDefault("return_code", 0)).intValue();
        if (returnCode != ZALOPAY_SUCCESS_CODE) {
            log.error("ZaloPay createOrder failed: returnCode={}, msg={}", returnCode,
                    response.get("return_message"));
            throw new AppException(ErrorCode.ZALOPAY_ORDER_CREATION_FAILED);
        }

        String orderUrl = (String) response.get("order_url");
        String zpTransToken = (String) response.get("zp_trans_token");

        // Update invoice payment method (billing concern stays on Invoice)
        invoice.setPaymentMethod(PaymentMethod.ZALOPAY);
        invoiceRepository.save(invoice);

        // Create the pending Transaction – ZaloPay gateway tokens stored here
        Transaction transaction = new Transaction();
        transaction.setInvoice(invoice);
        transaction.setPaymentMethod(PaymentMethod.ZALOPAY);
        transaction.setAppTransId(appTransId);
        transaction.setZpTransToken(zpTransToken);
        transaction.setOrderUrl(orderUrl);
        transaction.setAmount(request.amount());
        transaction.setStatus(TransactionStatus.PENDING);
        transactionRepository.save(transaction);

        log.info("ZaloPay order created: appTransId={}, invoiceId={}", appTransId, request.invoiceId());

        return new ZaloPayOrderResponse(orderUrl, zpTransToken, appTransId);
    }

    @Override
    @Transactional
    public void handleCallback(ZaloPayCallbackRequest callbackRequest) {
        // Step 1: verify MAC from ZaloPay using KEY2
        String expectedMac = hmacSha256(zaloPayConfig.getKey2(), callbackRequest.data());

        log.info("Received ZaloPay callback - data: {}", callbackRequest.data());
        log.info("ZaloPay MAC: {}, expected MAC: {}", callbackRequest.mac(), expectedMac);

        if (!expectedMac.equalsIgnoreCase(callbackRequest.mac())) {
            log.warn("ZaloPay callback MAC mismatch. expected={}, got={}", expectedMac, callbackRequest.mac());
            throw new AppException(ErrorCode.ZALOPAY_INVALID_CALLBACK_MAC);
        }

        // Step 2: parse data JSON
        Map<String, Object> dataMap;
        try {
            dataMap = objectMapper.readValue(callbackRequest.data(),
                    new TypeReference<Map<String, Object>>() {
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ZaloPay callback data", e);
            throw new AppException(ErrorCode.ZALOPAY_INVALID_CALLBACK_MAC);
        }

        String appTransId = (String) dataMap.get("app_trans_id");
        String zpTransId = (String) dataMap.get("zp_trans_id");
        int returnCode = ((Number) dataMap.getOrDefault("return_code", 0)).intValue();
        log.info("ZaloPay callback received: appTransId={}, zpTransId={}, returnCode={}",
                appTransId, zpTransId, returnCode);

        // Step 3: find Transaction by appTransId → navigate to Invoice
        Transaction transaction = transactionRepository.findByAppTransId(appTransId)
                .orElseThrow(() -> new AppException(ErrorCode.ZALOPAY_ORDER_NOT_FOUND));

        Invoice invoice = transaction.getInvoice();
        InvoiceStatus newInvoiceStatus = (returnCode == ZALOPAY_SUCCESS_CODE)
                ? InvoiceStatus.PAID
                : InvoiceStatus.CANCELLED;
        invoice.setStatus(newInvoiceStatus);
        invoiceRepository.save(invoice);

        // Step 4: update Transaction gateway fields
        TransactionStatus newTxStatus = (returnCode == ZALOPAY_SUCCESS_CODE)
                ? TransactionStatus.SUCCESS
                : TransactionStatus.FAILED;
        transaction.setStatus(newTxStatus);
        transaction.setGatewayTransactionId(zpTransId);
        transaction.setReturnCode(returnCode);
        transactionRepository.save(transaction);

        if (returnCode == ZALOPAY_SUCCESS_CODE) {
            eventPublisher.publishEvent(new PaymentSuccessEvent(invoice.getId(), appTransId, zpTransId, invoice.getBookingId(), invoice.getChargingSessionId()));
        }

        log.info("ZaloPay callback processed: appTransId={}, invoiceStatus={}", appTransId, newInvoiceStatus);
    }

    @Override
    @Transactional
    public ZaloPayStatusResponse queryOrderStatus(String appTransId) {
        // Look up transaction then navigate to invoice
        Transaction transaction = transactionRepository.findByAppTransId(appTransId)
                .orElseThrow(() -> new AppException(ErrorCode.ZALOPAY_ORDER_NOT_FOUND));
        Invoice invoice = transaction.getInvoice();

        // Also query ZaloPay gateway for the live status
        String queryMacData = String.join("|",
                zaloPayConfig.getAppId(),
                appTransId,
                zaloPayConfig.getKey1());
        String queryMac = hmacSha256(zaloPayConfig.getKey1(), queryMacData);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("app_id", zaloPayConfig.getAppId());
        body.add("app_trans_id", appTransId);
        body.add("mac", queryMac);

        Map<String, Object> response = webClient.post()
                .uri(zaloPayConfig.getEndpoint() + "/query")
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        if (response == null) {
            throw new AppException(ErrorCode.ZALOPAY_QUERY_FAILED);
        }

        int returnCode = ((Number) response.getOrDefault("return_code", 0)).intValue();
        String message = (String) response.getOrDefault("return_message", "");

        // Sync status with database if there's a final state from gateway
        if (returnCode == ZALOPAY_SUCCESS_CODE && transaction.getStatus() != TransactionStatus.SUCCESS) {
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setReturnCode(returnCode);
            invoice.setStatus(InvoiceStatus.PAID);

            transactionRepository.save(transaction);
            invoiceRepository.save(invoice);
            log.info("queryOrderStatus synced success for appTransId={}", appTransId);
        } else if (returnCode == 2 && transaction.getStatus() != TransactionStatus.FAILED) { // 2 = Failed
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setReturnCode(returnCode);
            invoice.setStatus(InvoiceStatus.CANCELLED);

            transactionRepository.save(transaction);
            invoiceRepository.save(invoice);
            log.info("queryOrderStatus synced failure for appTransId={}", appTransId);
        }

        return new ZaloPayStatusResponse(appTransId, invoice.getStatus(), returnCode, message);
    }

    // ============================
    // Private Helpers
    // ============================

    /**
     * Generates a ZaloPay appTransId in the format {@code yyMMdd_UUID_short}.
     * Example: {@code 260305_a3f2}
     */
    private String buildAppTransId() {
        String datePart = APP_TRANS_DATE_FMT.format(Instant.now());
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return datePart + "_" + uniquePart;
    }

    /**
     * Computes HMAC-SHA256 of {@code data} using the provided {@code key}.
     *
     * @param key  HMAC secret key (ZaloPay KEY1 or KEY2).
     * @param data Input string to sign.
     * @return Hex-encoded lowercase digest.
     */
    private String hmacSha256(String key, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(
                    key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(secretKey);
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            log.error("Failed to compute HMAC-SHA256", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
