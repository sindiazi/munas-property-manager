package com.example.rentalmanager.billing.domain.valueobject;

import java.math.BigDecimal;
import java.util.Currency;

public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount must be non-negative");
        if (currency == null)
            throw new IllegalArgumentException("Currency must not be null");
    }

    public static Money of(BigDecimal amount, String currencyCode) {
        return new Money(amount, Currency.getInstance(currencyCode));
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency))
            throw new IllegalArgumentException("Cannot add amounts in different currencies");
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        if (!currency.equals(other.currency))
            throw new IllegalArgumentException("Cannot subtract amounts in different currencies");
        var result = amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Subtraction results in negative amount");
        return new Money(result, currency);
    }

    @Override public String toString() {
        return "%s %s".formatted(currency.getCurrencyCode(), amount.toPlainString());
    }
}
