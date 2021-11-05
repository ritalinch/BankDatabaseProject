package entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "currency_rates")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "currency")
    @Enumerated(EnumType.ORDINAL)
    private Currency currency;

    private Float rate;

    private Instant updated;

    public CurrencyRate() { }

    public CurrencyRate(Currency currency, Float rate) {
        this.currency = currency;
        this.rate = rate;
        this.updated = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Float getRate() {
        return rate;
    }

    public Instant getUpdated() {
        return updated;
    }
}
