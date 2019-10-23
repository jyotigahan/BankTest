package com.bank.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.dao.TransactionDao;
import com.bank.exceptions.ExceptionType;
import com.bank.exceptions.ObjectModificationException;
import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
 
/**
 * Right now the proxy service under the {@link TransactionDao}. Should be used to abstract the presentation layer
 * from the persistence layer.
 *
 * Additionally it schedule the transaction execution service.
 *
 * TODO: make TransactionDao as an interface and pass it into the constructor. Use DI.
 */
public class TransactionsService {
    private static final Logger log = LoggerFactory.getLogger(TransactionsService.class);

    private static TransactionsService trnService;
    private TransactionDao transactionDao;
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    /**
     * Constructor made just for testing purpose
     */
   // public TransactionsService() {}

    public TransactionsService(TransactionDao transactionDao) {
        this.transactionDao = transactionDao;
       executorService.scheduleAtFixedRate(() -> trnService.executeTransactions(),0, 5, TimeUnit.SECONDS);
        log.info("Transaction Executor planned");
    }

    public static TransactionsService getInstance() {
        if(trnService == null){
            synchronized (TransactionsService.class) {
                if(trnService == null){
                	trnService = new TransactionsService(TransactionDao.getInstance());
                }
            }
        }
        return trnService;
    }

    public Collection<Transaction> getAllTransactions() {
        return transactionDao.getAllTransactions();
    }

    private Collection<Long> getAllTransactionIdsByStatus(TransactionStatus transactionStatus) {
        return transactionDao.getAllTransactionIdsByStatus(transactionStatus);
    }

    public Transaction getTransactionById(Long id) {
        return transactionDao.getTransactionById(id);
    }

    /**
     * Make it possible to create money transfer from one account to another.
     * The result of execution is created transaction with actual status. Usually it is "IN PROGRESS"
     *
     * The transaction <code>fromBankAccount</code> and <code>toBankAccount</code> may have not specified any
     * fields except id
     *
     * @return transaction object with the actual ID
     */
    public Transaction createTransaction(Transaction transaction) throws ObjectModificationException {
        if (transaction.getFromBankAccountId() == null || transaction.getToBankAccountId() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "The transaction has not provided from Bank Account or to Bank Account values");
        }
        if (transaction.getFromBankAccountId().equals(transaction.getToBankAccountId())) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "The sender and recipient should not be same");
        }
        if (transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED,
                    "The amount should be more than 0");
        }

        return transactionDao.createTransaction(transaction);
    }

    /**
     * Here we are taking all PLANNED transactions and executing them.
     * After execution the transaction status will be changed
     */
    public void executeTransactions() {
        log.info("Starting of Transaction executor");
        Collection<Long> plannedTransactionIds = getAllTransactionIdsByStatus(TransactionStatus.PLANNED);

        for (Long transactionId : plannedTransactionIds) {
            try {
                transactionDao.executeTransaction(transactionId);
            } catch (ObjectModificationException e) {
                log.error("Could not execute transaction with id %d", transactionId, e);
            }
        }
        log.info("Transaction executor ended");
    }
}
