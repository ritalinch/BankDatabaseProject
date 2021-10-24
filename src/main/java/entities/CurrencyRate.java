package entities;

import javax.persistence.*;
import java.time.Instant;
import java.util.Currency;

@Entity
@Table(name = "rates")
public class CurrencyRate {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    private Currency from;

    @Enumerated(EnumType.ORDINAL)
    private Currency to;

    private Double rate;

    private Instant updated;

    public CurrencyRate() { }

    public CurrencyRate(Currency from, Currency to) {
        this.from = from;
        this.to = to;
        this.updated = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Currency getFrom() {
        return from;
    }

    public Currency getTo() {
        return to;
    }

    public Instant getUpdated() {
        return updated;
    }
}
