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

    private Double rate;

    public CurrencyRate() {
    }

    public CurrencyRate(Currency currency, Double rate) {
        this.currency = currency;
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Double getRate() {
        return rate;
    }

}
