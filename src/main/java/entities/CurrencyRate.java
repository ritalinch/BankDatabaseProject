package entities;

import javax.persistence.*;

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

    public CurrencyRate() {
    }

    public CurrencyRate(Currency currency, Float rate) {
        this.currency = currency;
        this.rate = rate;
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

}
