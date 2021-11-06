package entities;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    private BigDecimal amount;

    private BigDecimal remained;

    public Transaction() {
    }

    public Transaction(Account account, BigDecimal amount, BigDecimal remained) {
        this.account = account;
        this.amount = amount;
        this.remained = remained;
    }

}
