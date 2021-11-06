package entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private Currency currency;

    private BigDecimal amount;

    private BigDecimal remained;

    private Date timestamp;

    public Transaction() {
    }

    public Transaction(Account account, BigDecimal amount, BigDecimal remained) {
        this.account = account;
        this.amount = amount;
        this.remained = remained;
        this.currency = account.getCurrency();
        this.timestamp = Date.from(Instant.now());
    }

}
