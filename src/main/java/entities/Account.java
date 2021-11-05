package entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private final List<Transaction> historyOfTransactions = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @Column(name = "currency")
    @Enumerated(EnumType.ORDINAL)
    private Currency currency;
    private BigDecimal balance = new BigDecimal("0.00");
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    public Account() {
    }

    public Account(Currency currency, Client client) {
        this.currency = currency;
        this.balance = new BigDecimal("0");
        this.client = client;
    }

    public Transaction retainBalance(BigDecimal value) {
        if (balance.subtract(value).compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Not enough money.");
        }

        balance = balance.subtract(value);
        Transaction transaction = new Transaction(this, value, balance);
        historyOfTransactions.add(transaction);
        return transaction;
    }

    public Transaction topUpBalance(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value.");
        }

        balance = balance.add(value);
        Transaction transaction = new Transaction(this, value, balance);
        historyOfTransactions.add(transaction);
        return transaction;
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
