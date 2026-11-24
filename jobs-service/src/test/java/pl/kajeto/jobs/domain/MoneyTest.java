package pl.kajeto.jobs.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoneyTest {

  @ParameterizedTest
  @CsvSource({"10,10.00", "10.5,10.50", "10.50,10.50", "10.000,10.00", "0,0.00", "0.01,0.01"})
  void normalizesToTwoDecimalPlaces(String input, String expected) {
    assertThat(Money.of(input).amount()).isEqualTo(new BigDecimal(expected));
  }

  @Test
  void rejectsMoreThanTwoSignificantDecimals() {
    assertThatThrownBy(() -> Money.of("10.001"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2 decimal places");
  }

  @Test
  void rejectsNegativeAmount() {
    assertThatThrownBy(() -> Money.of("-0.01"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("negative");
  }

  @Test
  void rejectsNull() {
    assertThatThrownBy(() -> new Money(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void zeroIsZeroPointZeroZero() {
    assertThat(Money.zero().amount()).isEqualTo(new BigDecimal("0.00"));
  }

  @Test
  void plusAddsExactly() {
    assertThat(Money.of("0.10").plus(Money.of("0.20"))).isEqualTo(Money.of("0.30"));
  }

  @Test
  void equalAmountsAreEqualRegardlessOfInputScale() {
    assertThat(Money.of("5")).isEqualTo(Money.of("5.00"));
  }
}
