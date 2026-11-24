package pl.kajeto.jobs.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** A non-negative amount of PLN with exactly two decimal places (matches NUMERIC(12,2)). */
public record Money(BigDecimal amount) {

  private static final int SCALE = 2;

  public Money {
    Objects.requireNonNull(amount, "amount");
    if (amount.signum() < 0) {
      throw new IllegalArgumentException("Amount must not be negative");
    }
    if (amount.stripTrailingZeros().scale() > SCALE) {
      throw new IllegalArgumentException("Amount must have at most 2 decimal places");
    }
    amount = amount.setScale(SCALE);
  }

  public static Money of(String value) {
    return new Money(new BigDecimal(value));
  }

  public static Money zero() {
    return new Money(BigDecimal.ZERO);
  }

  public Money plus(Money other) {
    return new Money(amount.add(other.amount));
  }
}
