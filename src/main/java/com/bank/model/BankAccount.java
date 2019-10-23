package com.bank.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Random;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
 
/**
 * @author Jyoti Gahan
 * Bank Account entity model. Relates to the database table <code>bank_account</code>. Defines the bank account of
 * individual with <code>ownerName</code>. It has <code>balance</code> in specific money <code>currency</code>. Once
 * there is any PLANNED transferring transaction in the system relates to this Bank Account, the transaction amount is
 * reserved in <code>blockedAmount</code> field
 */
@Data
@Builder
public class BankAccount implements AuditId{
    private Long id;
    @NonNull
    private String ownerName;
    @NonNull
    
    private BigDecimal balance;
    @NonNull
    private BigDecimal blockedAmount;
    
    public BankAccount() {
    }

    public BankAccount(String ownerName, BigDecimal balance, BigDecimal blockedAmount ) {
        this(new Random().nextLong(), ownerName, balance, blockedAmount);
    }

    public BankAccount(Long id, String ownerName, BigDecimal balance, BigDecimal blockedAmount ) {
        this.id = id;
        this.ownerName = ownerName;
        this.balance = balance;
        this.blockedAmount = blockedAmount;
     }

    public BankAccount(Long id, String ownerName) {
        this.id = id;
        this.ownerName = ownerName;
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankAccount that = (BankAccount) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    } 
    
  }
