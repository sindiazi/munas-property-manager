package com.example.rentalmanager.billing.domain.aggregate;

import com.example.rentalmanager.billing.domain.valueobject.MpesaTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain entity tracking the lifecycle of a single M-Pesa STK Push request.
 * Not an aggregate root — owned by the billing context.
 */
public class MpesaTransaction {

    private final String                 checkoutRequestId;
    private final String                 merchantRequestId;
    private final UUID                   paymentTransactionId;
    private final UUID                   tenantId;
    private final String                 phoneNumber;
    private final BigDecimal             amount;
    private       MpesaTransactionStatus status;
    private       String                 mpesaReceiptNumber;
    private final Instant                initiatedAt;
    private       Instant                completedAt;

    public MpesaTransaction(String checkoutRequestId, String merchantRequestId,
                            UUID paymentTransactionId, UUID tenantId, String phoneNumber,
                            BigDecimal amount, MpesaTransactionStatus status,
                            String mpesaReceiptNumber, Instant initiatedAt, Instant completedAt) {
        this.checkoutRequestId    = checkoutRequestId;
        this.merchantRequestId    = merchantRequestId;
        this.paymentTransactionId = paymentTransactionId;
        this.tenantId             = tenantId;
        this.phoneNumber          = phoneNumber;
        this.amount               = amount;
        this.status               = status;
        this.mpesaReceiptNumber   = mpesaReceiptNumber;
        this.initiatedAt          = initiatedAt;
        this.completedAt          = completedAt;
    }

    public String                 getCheckoutRequestId()    { return checkoutRequestId; }
    public String                 getMerchantRequestId()    { return merchantRequestId; }
    public UUID                   getPaymentTransactionId() { return paymentTransactionId; }
    public UUID                   getTenantId()             { return tenantId; }
    public String                 getPhoneNumber()          { return phoneNumber; }
    public BigDecimal             getAmount()               { return amount; }
    public MpesaTransactionStatus getStatus()               { return status; }
    public String                 getMpesaReceiptNumber()   { return mpesaReceiptNumber; }
    public Instant                getInitiatedAt()          { return initiatedAt; }
    public Instant                getCompletedAt()          { return completedAt; }

    void setStatus(MpesaTransactionStatus status)           { this.status = status; }
    void setMpesaReceiptNumber(String mpesaReceiptNumber)   { this.mpesaReceiptNumber = mpesaReceiptNumber; }
    void setCompletedAt(Instant completedAt)                { this.completedAt = completedAt; }
}
