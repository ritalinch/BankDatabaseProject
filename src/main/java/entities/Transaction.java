package entities;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "transactions")
@XmlRootElement(name = "transaction")
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

    @XmlAttribute
    public void setId(Long id) {
        this.id = id;
    }

    @XmlTransient
    public void setAccount(Account account) {
        this.account = account;
    }

    @XmlElement
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @XmlElement
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @XmlElement
    public void setRemained(BigDecimal remained) {
        this.remained = remained;
    }

    @XmlElement
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", account=" + account +
                ", currency=" + currency +
                ", amount=" + amount +
                ", remained=" + remained +
                ", timestamp=" + timestamp +
                '}';
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getRemained() {
        return remained;
    }

    public Date getTimestamp() {
        return timestamp;
    }

}
