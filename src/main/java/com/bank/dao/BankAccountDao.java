package com.bank.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bank.db.DbUtils;
import com.bank.exceptions.ExceptionType;
import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;


/**
 * @author Jyoti Gahan
 * Encapsulates all logic for Bank Account entity which is related to the database. Implements the singleton pattern.
 */
public class BankAccountDao {

    private static final Logger log = LoggerFactory.getLogger(BankAccountDao.class);
  
    public static final String GET_ALL_ACCOUNTS_SQL = "select * from bank_account";
    public static final String GET_ACCOUNTS_BY_ID_SQL = "select * from bank_account ba where ba.id = ?";
    public static final String GET_BANK_ACCOUNT_BY_ID_SQL = GET_ACCOUNTS_BY_ID_SQL +  " for update";
    public static final String UPDATE_BANK_ACCOUNT_SQL = "update bank_account set owner_name=? where id = ? ";
    public static final String UPDATE_BANK_ACCOUNT_SQL1 = "update bank_account set owner_name=?, balance=?, blocked_amount=?  where id = ? ";
    public static final String INSERT_BANK_ACCOUNT_SQL = "insert into bank_account (owner_name, balance,blocked_amount  ) values (?,?,?)";
 
    public static final Long JYOTI = 1L;
    public static final Long RANJAN = 2L;
    public static final Long GAHAN = 3L;
    
    
    private static final BankAccountDao bas = new BankAccountDao();
    private DbUtils dbUtils = DbUtils.getInstance();

    private BankAccountDao() {
    }

    public static BankAccountDao getInstance() {
        return bas;
    }

    /**
     * @return All Bank Accounts which is exists in the database at the moment
     */
    public Collection<BankAccount> getAllBankAccounts() {
        return dbUtils.executeQuery(GET_ALL_ACCOUNTS_SQL, getBankAccounts -> {
            Collection<BankAccount> bankAccounts = new ArrayList<>();

            try (ResultSet bankAccountsRS = getBankAccounts.executeQuery()) {
                if (bankAccountsRS != null) {
                    while (bankAccountsRS.next()) {
                        bankAccounts.add(extractBankAccountFromResultSet(bankAccountsRS));
                    }
                }
            }

            return bankAccounts;
        }).getResult();
    }

    /**
     * Returns Bank Account object by id specified
     *
     * @param id Bank Account object id
     * @return Bank Account object with id specified
     */
    public BankAccount getBankAccountById(Long id) {
    
        return dbUtils.executeQuery(GET_ACCOUNTS_BY_ID_SQL, getBankAccount -> {
            getBankAccount.setLong(1, id);
            try (ResultSet bankAccountRS = getBankAccount.executeQuery()) {
                if (bankAccountRS != null && bankAccountRS.first()) {
                    return extractBankAccountFromResultSet(bankAccountRS);
                }
            }

            return null;
        }).getResult();
    }

    /**
     * Special form of {@link #getBankAccountById(Long)} method which is not closing the connection once result
     * will be obtained. We are using it only inside the related <code>TransactionDto</code>
     *
     * @param id  Bank Account object id
     * @param con the <code>Connection</code> to be used for this query
     */
    BankAccount getForUpdateBankAccountById(Connection con, Long id) {
 

        return dbUtils.executeQueryInConnection(con, GET_BANK_ACCOUNT_BY_ID_SQL, getBankAccount -> {
            getBankAccount.setLong(1, id);
            try (ResultSet bankAccountRS = getBankAccount.executeQuery()) {
                if (bankAccountRS != null && bankAccountRS.first()) {
                    return extractBankAccountFromResultSet(bankAccountRS);
                }
            }

            return null;
        }).getResult();
    }

    /**
     * Updates the Bank Account with changed parameters using the id provided by the object passed. Only ownerName
     * parameter will be updated.
     *
     * @param bankAccount - the object to be updated
     * @throws ObjectModificationException if Bank Account with the provided id will not be exists in the database at
     *                                     the moment or object provided is malformed
     */
    public void updateBankAccountSafe(BankAccount bankAccount) throws ObjectModificationException {
     
        if (bankAccount.getId() == null || bankAccount.getOwnerName() == null) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_MALFORMED, "Id and OwnerName fields could not be NULL");
        }

        DbUtils.QueryExecutor<Integer> queryExecutor = updateBankAccount -> {
            updateBankAccount.setString(1, bankAccount.getOwnerName());
            updateBankAccount.setLong(2, bankAccount.getId());

            return updateBankAccount.executeUpdate();
        };

        int result = dbUtils.executeQuery(UPDATE_BANK_ACCOUNT_SQL, queryExecutor).getResult();

        if (result == 0) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_NOT_FOUND);
        }
    }

    /**
     * Updates the Bank Account with changed parameters using the id provided by the object passed.
     * We are using it only inside the related <code>TransactionDto</code>
     *
     * @param bankAccount Bank Account object which will be updated
     * @param con         the <code>Connection</code> to be used for this query
     * @throws ObjectModificationException if Bank Account with the provided id will not be exists in the database at the
     *                                     moment or object provided is malformed
     */
    void updateBankAccount(BankAccount bankAccount, Connection con) throws ObjectModificationException {

     //   verify(bankAccount);

        DbUtils.QueryExecutor<Integer> queryExecutor = updateBankAccount -> {
            fillInPreparedStatement(updateBankAccount, bankAccount);
            updateBankAccount.setLong(4, bankAccount.getId());
            return updateBankAccount.executeUpdate();
        };

        int result;
        if (con == null) {
            result = dbUtils.executeQuery(UPDATE_BANK_ACCOUNT_SQL1, queryExecutor).getResult();
        } else {
            result = dbUtils.executeQueryInConnection(con, UPDATE_BANK_ACCOUNT_SQL1, queryExecutor).getResult();
        }

        if (result == 0) {
            throw new ObjectModificationException(ExceptionType.OBJECT_IS_NOT_FOUND);
        }
    }

    /**
     * Creates the Bank Account object provided in the database. Id of this objects will not be used. It will be
     * generated and returned in the result of the method.
     *
     * @param bankAccount Bank Account object which should be created
     * @return created Bank Account object with ID specified'
     * @throws ObjectModificationException if Bank Account with the provided id will not be exists in the database at the
     *                                     moment or object provided is malformed
     */
    public BankAccount createBankAccount(BankAccount bankAccount) throws ObjectModificationException {


    //    verify(bankAccount);

        bankAccount = dbUtils.executeQuery(INSERT_BANK_ACCOUNT_SQL,
                new DbUtils.CreationQueryExecutor<>(bankAccount, BankAccountDao::fillInPreparedStatement)).getResult();

        if (bankAccount == null) {
            throw new ObjectModificationException(ExceptionType.COULD_NOT_OBTAIN_ID);
        }

        return bankAccount;
    }

    /**
     * The opposite method to {@link #fillInPreparedStatement(PreparedStatement, BankAccount)} which is
     * extracts Bank Account parameters from the result set
     *
     * @param bankAccountsRS result set with parameters of the Bank Account
     * @return extracted Bank Account object
     * @throws SQLException if some parameters in result set will not be found or will have non compatible
     *                      data type
     */
    private BankAccount extractBankAccountFromResultSet(ResultSet bankAccountsRS) throws SQLException {
     	return BankAccount.builder()
    			.id(bankAccountsRS.getLong("id"))
    			.ownerName(bankAccountsRS.getString("owner_name"))
    			.balance(bankAccountsRS.getBigDecimal("balance"))
    			.blockedAmount(bankAccountsRS.getBigDecimal("blocked_amount"))
    			.build();
    }

 

    /**
     * Fills the provided prepared statement with the Bank Account's parameters provided
     *
     * @param preparedStatement prepared statement to be filled in
     * @param bankAccount       the Bank Account object which should be used to fill in
     */
    private static void fillInPreparedStatement(PreparedStatement preparedStatement, BankAccount bankAccount) {
        try {
            preparedStatement.setString(1, bankAccount.getOwnerName());
            preparedStatement.setBigDecimal(2, bankAccount.getBalance());
            preparedStatement.setBigDecimal(3, bankAccount.getBlockedAmount());
        } catch (SQLException e) {
            log.error("BankAccount prepared statement could not be initialized by values", e);
        }
    }
}
