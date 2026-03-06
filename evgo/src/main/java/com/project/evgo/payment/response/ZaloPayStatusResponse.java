package com.project.evgo.payment.response;

import com.project.evgo.sharedkernel.enums.InvoiceStatus;

/**
 * Response DTO for querying the current status of a ZaloPay order.
 *
 * @param appTransId    Our unique transaction ID used to look up the order.
 * @param invoiceStatus Current status of the associated invoice (PENDING / PAID
 *                      / CANCELLED).
 * @param returnCode    ZaloPay return_code from the last gateway response (1 =
 *                      success).
 * @param message       Human-readable description of the current status.
 */
public record ZaloPayStatusResponse(
                String appTransId,
                InvoiceStatus invoiceStatus,
                Integer returnCode,
                String message) {
}
