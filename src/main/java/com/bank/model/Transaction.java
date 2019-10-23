package com.bank.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * @author Jyoti Gahan
 * Transaction entity model. Relates to the database table <code>transaction</code>. Defines the transferring transaction
 * which is initialized by <code>fromBankAccount</code> who wants to transfer money to the <code>toBankAccount</code>
 * of <code>amount</code> in <code>currency</code> currency. Additionally this class controls the creation and last
 * update dates alongside with the actual {@link TransactionStatus} <code>status</code>  and <code>failMessage</code> in case of FAIL status.
 */
@Data
@AllArgsConstructor
@Builder
public class Transaction  implements AuditId {
    
	private Long id;
	@NonNull
    private Long fromBankAccountId;
	@NonNull
    private Long toBankAccountId;
	@NonNull
    private BigDecimal amount;
	@NonNull
    private Date creationDate;
	@NonNull
    private Date updateDate;
	@NonNull
    private TransactionStatus status;
    private String failMessage;
    
    public Transaction() {
        this.creationDate = new Date();
        this.updateDate = new Date();
        this.status = TransactionStatus.PLANNED;
        this.failMessage = "";
    }
    
    public Transaction(Long fromBankAccountId, Long toBankAccountId, BigDecimal amount ) {
        this();
        this.fromBankAccountId = fromBankAccountId;
        this.toBankAccountId = toBankAccountId;
        this.amount = amount;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
