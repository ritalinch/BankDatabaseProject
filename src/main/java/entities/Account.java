package entities;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name="currency")
    @Enumerated(EnumType.ORDINAL)
    private Currency currency;

    private BigDecimal balance = new BigDecimal("0.00");

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Transaction> historyOfTransactions = new ArrayList<>();

    public Account() {}

    public Account(Currency currency) {
        this.currency = currency;
        this.balance = new BigDecimal("0");
    }

    public void changeBalance(BigDecimal value) {
        if(balance.subtract(value).compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Not enough money.");
        }

        balance = balance.subtract(value);
        historyOfTransactions.add(new Transaction(this, value, balance));
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
