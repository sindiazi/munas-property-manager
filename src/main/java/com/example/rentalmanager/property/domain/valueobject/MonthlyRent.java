package com.example.rentalmanager.property.domain.valueobject;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Represents a non-negative monthly rent amount denominated in a given currency.
 */
public record MonthlyRent(BigDecimal amount, Currency currency) {

    public MonthlyRent {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Monthly rent amount must be non-negative");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency must not be null");
        }
    }

    public static MonthlyRent of(BigDecimal amount, String currencyCode) {
        return new MonthlyRent(amount, Currency.getInstance(currencyCode));
    }

    public MonthlyRent increase(BigDecimal percentage) {
        var factor = BigDecimal.ONE.add(percentage.divide(BigDecimal.valueOf(100)));
        return new MonthlyRent(amount.multiply(factor), currency);
    }

    @Override
    public String toString() {
        return "%s %s".formatted(currency.getCurrencyCode(), amount.toPlainString());
    }
}
