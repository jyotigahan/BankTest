package com.bank.test.controller;

import org.glassfish.grizzly.http.server.HttpServer;
import org.hamcrest.Matchers;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.bank.BankTransactionApplication;
import com.bank.controller.BankAccountsController;
import com.bank.dao.BankAccountDao;
import com.bank.model.BankAccount;
 import com.bank.service.BankAccountService;

import io.qameta.allure.Step;
import io.qameta.allure.Description;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotSame;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.Assert.assertNotEquals;

public class BankAccountControllerTest {
    private static HttpServer server;
    private static WebTarget target;

    @BeforeClass
    public static void beforeAll() {
        // start the server
        server = BankTransactionApplication.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        target = c.target(BankTransactionApplication.BASE_URI);
    }

    @AfterClass
    public static void afterAll() {
        server.shutdownNow();
    }

    @Description ( "Test Description: Verify successful creation of new bank account")
	@Test(description="Tests that method will successfully create new bank account in database")
    public void testCreateBankAccount() {
        BankAccountService bankAccountService = BankAccountService.getInstance();
        String OWNER_NAME = "Sergio";

        BankAccount bankAccount = new BankAccount(OWNER_NAME, BigDecimal.ZERO, BigDecimal.ZERO);

        Response response = target.path(BankAccountsController.BASE_URL).request().post(from(bankAccount));

        assertEquals(Response.Status.OK, response.getStatusInfo().toEnum());

        BankAccount returnedAccount = response.readEntity(BankAccount.class);
        BankAccount createdAccount = bankAccountService.getBankAccountById(returnedAccount.getId());

        assertNotNull(returnedAccount);
        assertNotNull(createdAccount);

        assertNotEquals(returnedAccount.getId(), bankAccount.getId());
        assertEquals(returnedAccount.getId(), createdAccount.getId());
        assertEquals(OWNER_NAME, createdAccount.getOwnerName());
    }


    @Description ( "Test Description: Verify successful retrival of a particular bank accounts ")
	@Test (description= "Tests that method will successfully retrive valid bank accounts with valid id from database")
    public void testGetBankAccountById() {
        Response response = getById(BankAccountDao.JYOTI);

        assertEquals(Response.Status.OK, response.getStatusInfo().toEnum());

        BankAccount bankAccount = response.readEntity(BankAccount.class);

        assertEquals(bankAccount.getId(), BankAccountDao.JYOTI);
    }
    
    
    @Description ( "Test Description: Verify successful retrival of all bank accounts")
	@Test (description= "Tests that method will successfully retrive all bank accounts from database")
    public void testGetAllBankAccounts() {
        Response response = target.path(BankAccountsController.BASE_URL).request().get();
        assertEquals(Response.Status.OK, response.getStatusInfo().toEnum());
       
        Collection<BankAccount> bankAccount = response.readEntity(new GenericType<Collection<BankAccount>>(){});
        assertEquals(bankAccount.size(), BankAccountDao.getInstance().getAllBankAccounts().size());
    }


    @Description ( "Test Description: Verify unsuccessful retrival of non-existing (null id ) bank account")
	@Test(description="Tests that method will respond as 'not found' the accounts with blank id from database")
    public void testGetNullBankAccount() {
        Response response = getById(null);
        assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo().toEnum());
    }


    @Description ( "Test Description: Verify unsuccessful retrival of invalid bank account")
	@Test(description="Tests that method will respond as 'not found' the accounts with incorrect id from database")
    public void testNonExistingBankAccountById() {
        Response response = getById(new Random().nextLong());
        assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo().toEnum());
    }


    @Description ( "Test Description: Verify successful update of bank account")
	@Test(description="Tests that method will successfully update bank account in database. Even if it will be attempt to update balance it will not be updated")
    public void testUpdateBankAccount() {
        BankAccountService bankAccountService = BankAccountService.getInstance();
        String newOwner = "New Name";

        BankAccount secondAccount = bankAccountService.getBankAccountById(BankAccountDao.RANJAN);
        secondAccount.setOwnerName(newOwner);
        BigDecimal accountBalance = secondAccount.getBalance();
        secondAccount.setBalance(accountBalance.add(BigDecimal.TEN));

        Response response = target.path(BankAccountsController.BASE_URL).request().put(from(secondAccount));

        assertEquals(Response.Status.OK, response.getStatusInfo().toEnum());

        BankAccount updatedAccount = bankAccountService.getBankAccountById(BankAccountDao.RANJAN);

        assertEquals(newOwner, updatedAccount.getOwnerName());
        assertThat(accountBalance, Matchers.comparesEqualTo(updatedAccount.getBalance()));
    }


    
    @Description ( "Test Description: Verify unsuccessful update of bank account")
	@Test(description="Test that method will unsuccessfully update bank account with invalid account id in database ")
    public void testUpdateNonExistingBankAccount() {
        BankAccount bankAccount = new BankAccount(new Random().nextLong(),"", BigDecimal.ZERO, BigDecimal.ZERO);

        Response response = target.path(BankAccountsController.BASE_URL).request().put(from(bankAccount));

        assertEquals(Response.Status.NOT_FOUND, response.getStatusInfo().toEnum());
    }

    
    @Description ( "Test Description: Verify unsuccessful update of incorrect bank account")
	@Test(description="Tests that method will unsuccessfully update incorrect bank account in database")
    public void testIncorrectUpdateBankAccount() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setId(new Random().nextLong());

        Response response = target.path(BankAccountsController.BASE_URL).request().put(from(bankAccount));

        assertEquals(Response.Status.BAD_REQUEST, response.getStatusInfo().toEnum());
    }


    private Response getById(Long id) {
        return target.path(BankAccountsController.BASE_URL + "/{" + BankAccountsController.GET_BANK_ACCOUNT_BY_ID_PATH + "}")
                .resolveTemplate("id", id == null ? "null" : id)
                .request().get();
    }

    private static Entity from(BankAccount bankAccount) {
        return Entity.entity(bankAccount, MediaType.valueOf(MediaType.APPLICATION_JSON));
    }
}
