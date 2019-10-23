package com.bank.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Jyoti Gahan
 * The dictionary wrapper related to the database table <code>transaction_status</code>
 */
@Getter
@AllArgsConstructor
public enum TransactionStatus {
    PLANNED(1), PROCESSING(2), FAILED(3), SUCCEED(4);

    private int id;

   
    public static TransactionStatus valueOf(int id) {
        for(TransactionStatus e : values()) {
            if(e.id == id) return e;
        }

        return null;
    }
 
}
