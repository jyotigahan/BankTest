package com.bank.service;

import java.util.Collection;

import com.bank.dao.BankAccountDao;
import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;
 
/**
 * Right now the proxy service under the {@link BankAccountDto}. Should be used to abstract the presentation layer
 * from the persistence layer
 *
 * TODO: needs to move business logic from BankAccountDto. Validations for example.
 * TODO: Use DI to abstract from persistence layer
 */
public class BankAccountService {
	
    private static final BankAccountService actService = new BankAccountService();

    public static BankAccountService getInstance() {
        return actService;
    }

    public Collection<BankAccount> getAllBankAccounts() {
        return BankAccountDao.getInstance().getAllBankAccounts();
    }

    public BankAccount getBankAccountById(Long id) {
        return BankAccountDao.getInstance().getBankAccountById(id);
    }

    public void updateBankAccount(BankAccount bankAccount) throws ObjectModificationException {
    	BankAccountDao.getInstance().updateBankAccountSafe(bankAccount);
    }

    public BankAccount createBankAccount(BankAccount bankAccount) throws ObjectModificationException {
        return BankAccountDao.getInstance().createBankAccount(bankAccount);
    }
}
