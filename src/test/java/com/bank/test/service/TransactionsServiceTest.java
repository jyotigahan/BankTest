package com.bank.test.service;

 
 import org.testng.annotations.Test;
 import com.bank.dao.BankAccountDao;
import com.bank.dao.TransactionDao;
import com.bank.exceptions.ObjectModificationException;
import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;
import com.bank.service.TransactionsService;

import io.qameta.allure.Description;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertArrayEquals;

public class TransactionsServiceTest {
    private static final TransactionsService staticTransactionService = TransactionsService.getInstance();
    
    @Description ( "Test Description: Verify successful retrival of all bank transactions")
 	@Test (description= "Tests that method will successfully retrive all bank transactions from database")
       public void testAllTransactionsRetrieval(){
        TransactionDao transactionDto = mock(TransactionDao.class);
        TransactionsService transactionsService = new TransactionsService(transactionDto);

        Collection<Transaction> testList = Arrays.asList(
                new Transaction(
                        BankAccountDao.JYOTI,
                        BankAccountDao.RANJAN,
                        BigDecimal.ZERO ),
                new Transaction(
                		BankAccountDao.RANJAN,
                		BankAccountDao.GAHAN,
                        BigDecimal.ZERO )
        );


        when(transactionDto.getAllTransactions()).thenReturn(testList);

        Collection<Transaction> transactions = transactionsService.getAllTransactions();

        assertNotNull(transactions);
        assertArrayEquals(testList.toArray(), transactions.toArray());
    }


    @Description ( "Test Description: Verify unsuccessful creation of bank transactions without sender's account")
   	@Test (description= "Tests that method will throw exception while creating transactions without sender's account", expectedExceptions=ObjectModificationException.class)    
     public void testCreateTransactionWithNullFrom() throws ObjectModificationException {
        staticTransactionService.createTransaction(new Transaction(
                null, 2L, BigDecimal.TEN
        ));
    }

    @Description ( "Test Description: Verify unsuccessful creation of bank transactions without receiver's account")
   	@Test (description= "Tests that method will throw exception while creating transactions without receiver's account", expectedExceptions=ObjectModificationException.class)    
    public void testCreateTransactionWithNullTo() throws ObjectModificationException {
        staticTransactionService.createTransaction(new Transaction(
                1L, null, BigDecimal.TEN
        ));
    }

   
    @Description ( "Test Description: Verify unsuccessful creation of bank transactions with same account")
   	@Test (description= "Tests that method will throw exception while creating transactions where sender's and receiver's having same account id", expectedExceptions=ObjectModificationException.class)    
     public void testCreateTransactionWithSameAccounts() throws ObjectModificationException {
        staticTransactionService.createTransaction(new Transaction(
                BankAccountDao.JYOTI,
                BankAccountDao.JYOTI,
                BigDecimal.TEN
        ));
    }

  
    @Description ( "Test Description: Verify unsuccessful creation of bank transactions with with zero amount")
   	@Test (description= "Tests that method will throw exception while creating transactions with zero amount", expectedExceptions=ObjectModificationException.class)    
    public void testCreateTransactionWithZeroAmount() throws ObjectModificationException {
        staticTransactionService.createTransaction(new Transaction(
                BankAccountDao.JYOTI,
                BankAccountDao.RANJAN,
                BigDecimal.ZERO
        ));
    }

    @Description ( "Test Description: Verify successful creation of bank transaction and execution")
   	@Test (description= "Tests that method will successfully create transaction and execution with the help of job scheduler" )    
     public void testCreateTransaction() throws ObjectModificationException {
        Long TRANSACTION_ID = 123L;

        TransactionDao transactionDto = mock(TransactionDao.class);

        Transaction transaction = new Transaction(
                BankAccountDao.JYOTI,
                BankAccountDao.RANJAN,
                BigDecimal.TEN
        );
        transaction.setId(TRANSACTION_ID);

        when(transactionDto.createTransaction(any())).thenReturn(transaction);

        when(transactionDto.getAllTransactionIdsByStatus(any())).thenReturn(
                Collections.singletonList(transaction.getId())
        );

        doAnswer(invocation -> {
            transaction.setStatus(TransactionStatus.SUCCEED);
            return null;
        }).when(transactionDto).executeTransaction(anyLong());

        TransactionsService transactionsService = new TransactionsService(transactionDto);
        Transaction createdTransaction = transactionsService.createTransaction(transaction);

        assertEquals(createdTransaction, transaction);
        assertEquals(createdTransaction.getStatus(), TransactionStatus.PLANNED);

        transactionsService.executeTransactions();

        assertEquals(transaction.getStatus(), TransactionStatus.SUCCEED);
    }
}
