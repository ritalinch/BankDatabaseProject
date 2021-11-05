package entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rates")
public class CurrencyRate {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "currency_from")
    @Enumerated(EnumType.ORDINAL)
    private Currency currencyFrom;

    @Column(name = "currency_to")
    @Enumerated(EnumType.ORDINAL)
    private Currency currencyTo;

    private Float rate;

    private Instant updated;

    public CurrencyRate() { }

    public CurrencyRate(Currency from, Currency to) {
        this.currencyFrom = from;
        this.currencyTo = to;
        this.updated = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrencyFrom() {
        return currencyFrom;
    }

    public Currency getCurrencyTo() {
        return currencyTo;
    }

    public Instant getUpdated() {
        return updated;
    }

}
