package com.bank.test.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.bank.dao.BankAccountDao;
import com.bank.dao.TransactionDao;
import com.bank.db.DbUtils;
import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;
import com.bank.model.Transaction;
import com.bank.model.TransactionStatus;

import io.qameta.allure.Description;
 
public class TransactionDaoTest {
	
    private TransactionDao transactionDao;
    private Collection<Transaction> testList;
 
    private static final Long TRANSACTION_1_ID = 1L;
    private static final Long TRANSACTION_2_ID = 2L;

    private Transaction transaction1;
    private Transaction transaction2;

    @BeforeClass
    public void initTestData() {
        DbUtils dbUtils = mock(DbUtils.class);
        transactionDao = new TransactionDao(dbUtils);

        transaction1 = new Transaction(
                BankAccountDao.JYOTI,
                BankAccountDao.RANJAN,
                BigDecimal.ONE);
        transaction1.setId(TRANSACTION_1_ID);

        transaction2 = new Transaction(
                BankAccountDao.RANJAN,
                BankAccountDao.GAHAN,
                BigDecimal.TEN);
        transaction2.setId(TRANSACTION_2_ID);

        testList = Arrays.asList(transaction1, transaction2);

        when(dbUtils.executeQuery(eq(TransactionDao.GET_ALL_TRANSACTIONS_SQL), any())).thenReturn(
                new DbUtils.QueryResult<>(testList)
        );

        when(dbUtils.executeQuery(eq(TransactionDao.GET_TRANSACTIONS_BY_STATUS_SQL), any())).thenReturn(
                new DbUtils.QueryResult<>(testList.stream().map(Transaction::getId).collect(Collectors.toList()))
        );

        when(dbUtils.executeQueryInConnection(any(), eq(TransactionDao.GET_TRANSACTIONS_FOR_UPDATE_BY_ID_SQL), any()))
                .thenReturn(new DbUtils.QueryResult<>(testList));
    }

  
    @Description ( "Test Description: Verify successful retrival of all bank transactions from database")
    @Test(description = "Tests that all transactions will return from database")
    public void testGetAllTransactions() {
        Collection<Transaction> resultList = transactionDao.getAllTransactions();

        assertNotNull(resultList);
        assertEquals(testList, resultList);
    }
 
    @Description ( "Test Description: Verify successful retrival of all bank transactions of a particular transaction status from database")
    @Test(description = "Tests that all transactions with particular status will be return from database")
      public void testGetAllTransactionIdsByStatus() {
        Collection<Long> resultTransactionIds = transactionDao.getAllTransactionIdsByStatus(TransactionStatus.PLANNED);

        assertNotNull(resultTransactionIds);
        assertEquals(resultTransactionIds.size(), 2);
        assertTrue(resultTransactionIds.contains(BankAccountDao.JYOTI));
        assertTrue(resultTransactionIds.contains(BankAccountDao.RANJAN));
    }

    @Description ( "Test Description: Verify successful creation of bank transaction in database")
    @Test(description = "Tests that bank transaction will create successfully in database")
    public void testTransactionCreation() throws ObjectModificationException {
        TransactionDao transactionDao = TransactionDao.getInstance();
        BankAccountDao bankAccountDao = BankAccountDao.getInstance();

        BankAccount sergey = bankAccountDao.getBankAccountById(BankAccountDao.JYOTI);
        BankAccount nikolay = bankAccountDao.getBankAccountById(BankAccountDao.RANJAN);

        BigDecimal sergeyInitialBalance = sergey.getBalance();
        BigDecimal sergeyInitialBlocked = sergey.getBlockedAmount();
        BigDecimal nikolayInitialBalance = nikolay.getBalance();
        BigDecimal nikolayInitialBlocked = nikolay.getBlockedAmount();

        Transaction resultTransaction = transactionDao.createTransaction(transaction1);

        assertEquals(resultTransaction.getStatus(), TransactionStatus.PLANNED);

        sergey = bankAccountDao.getBankAccountById(BankAccountDao.JYOTI);
        nikolay = bankAccountDao.getBankAccountById(BankAccountDao.RANJAN);

        assertThat(sergeyInitialBalance, Matchers.comparesEqualTo(sergey.getBalance()));
        assertThat(sergeyInitialBlocked.add(transaction1.getAmount()),Matchers.comparesEqualTo(sergey.getBlockedAmount()));
        assertThat(nikolayInitialBalance, Matchers.comparesEqualTo(nikolay.getBalance()));
        assertThat(nikolayInitialBlocked, Matchers.comparesEqualTo(nikolay.getBlockedAmount()));
    }

    @Description ( "Test Description: Verify successful execution of bank transaction in database")
    @Test(description = "Tests that bank transaction will execute successfully with status 'Succeed' in database")
    public void testTransactionExecution() throws ObjectModificationException {
        TransactionDao transactionDao = TransactionDao.getInstance();
        BankAccountDao bankAccountDao = BankAccountDao.getInstance();

        BankAccount nikolay = bankAccountDao.getBankAccountById(BankAccountDao.RANJAN);
        BankAccount vlad = bankAccountDao.getBankAccountById(BankAccountDao.GAHAN);

        BigDecimal nikolayInitialBalance = nikolay.getBalance();
        BigDecimal nikolayInitialBlocked = nikolay.getBlockedAmount();
        BigDecimal vladInitialBalance = vlad.getBalance();
        BigDecimal vladInitialBlocked = vlad.getBlockedAmount();

        Transaction resultTransaction = transactionDao.createTransaction(transaction2);
        transactionDao.executeTransaction(resultTransaction.getId());

        resultTransaction = transactionDao.getTransactionById(resultTransaction.getId());
        nikolay = bankAccountDao.getBankAccountById(transaction2.getFromBankAccountId());
        vlad = bankAccountDao.getBankAccountById(transaction2.getToBankAccountId());
        
        BigDecimal needToWithdraw =  transaction2.getAmount();
        BigDecimal needToTransfer =  transaction2.getAmount();

        assertEquals(resultTransaction.getStatus(), TransactionStatus.SUCCEED);
        assertThat(nikolayInitialBalance.subtract(needToWithdraw), Matchers.comparesEqualTo(nikolay.getBalance()));

        assertThat(nikolayInitialBlocked, Matchers.comparesEqualTo(nikolay.getBlockedAmount()));

        assertThat(vladInitialBalance.add(needToTransfer), Matchers.comparesEqualTo(vlad.getBalance()));

        assertThat(vladInitialBlocked, Matchers.comparesEqualTo(vlad.getBlockedAmount()));
    }

    @Description ( "Test Description: Verify unsuccessful creation of wrong transaction in database")
    @Test(description = "Tests that wrong bank transaction will fail in database with ObjectModificationException", expectedExceptions = ObjectModificationException.class)
     public void testWrongTransactionCreation() throws ObjectModificationException {
        TransactionDao transactionDao = TransactionDao.getInstance();

        Transaction transaction = new Transaction(
                BankAccountDao.RANJAN,
                BankAccountDao.GAHAN,
                BigDecimal.valueOf(10000)              
        );

        transactionDao.createTransaction(transaction);
    }
}
