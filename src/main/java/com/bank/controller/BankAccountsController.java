package com.bank.controller;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.bank.exceptions.ObjectModificationException;
import com.bank.model.BankAccount;
import com.bank.service.BankAccountService;

/**
 * @author Jyoti Gahan
 * This class is responsible for CRUD operations of Bank Account object
 */
@Path(BankAccountsController.BASE_URL)
@Produces(MediaType.APPLICATION_JSON)
public class BankAccountsController {
    public static final String BASE_URL = "/api/v1/accounts";
    public static final String GET_BANK_ACCOUNT_BY_ID_PATH = "id";

    private final static BankAccountService BANK_ACCOUNT_SERVICE = BankAccountService.getInstance();

    /**
     * @return The full list of Bank Account objects which has been registered at the time.
     *
     */
    @GET
    public Response getAllBankAccounts() {
        Collection<BankAccount> bankAccounts;

        bankAccounts = BANK_ACCOUNT_SERVICE.getAllBankAccounts();

        if (bankAccounts == null) {
            Response.noContent().build();
        }

        return Response.ok(bankAccounts).build();
    }

    /**
     * @param id The ID of Bank Account
     *
     * @return The Bank Account object which has particular ID. This ID has been generated and returned
     * during the Bank Account creation by the <code>POST: /bankAccount</code> endpoint
     */
    @GET
    @Path("{" + GET_BANK_ACCOUNT_BY_ID_PATH + "}")
    public Response getBankAccountById(@PathParam(GET_BANK_ACCOUNT_BY_ID_PATH) Long id) {
        BankAccount bankAccount;


        bankAccount = BANK_ACCOUNT_SERVICE.getBankAccountById(id);

        if (bankAccount == null) {
            throw new WebApplicationException("The bank account is not exists", Response.Status.NOT_FOUND);
        }

        return Response.ok(bankAccount).build();
    }

    /**
     * Updates the particular Bank Account with the parameters provided. The Bank Account which should be
     * updated is searching by the ID which has provided object. You can not update <code>balance</code> and/or
     * <code>blockedAmount</code> fields of the object as it is information maintained only by the system.
     *
     * @param bankAccount the Bank Account object (id should be specified) which will update the data
     *
     * @return updated Bank Account object. In general it should be object with the same parameters as provided had
     */
    @PUT
    public Response updateBankAccount(BankAccount bankAccount) {
    	
    	try {
    	
    	BankAccountService.getInstance().updateBankAccount(bankAccount);
        
    	return Response.ok(bankAccount).build();
        
    	}catch(ObjectModificationException e) {    		
            return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	
        
    }

    /**
     * Creates the Bank Account object with the provided parameters. It doesn't mean if provided object will have
     * an ID specified. This ID will be regenerated and returned in the response object
     *
     * @param bankAccount the Bank Account object to create with parameters specified
     *
     * @return Bank Account object with the ID parameter specified.
     */
    @POST
    public Response createBankAccount(BankAccount bankAccount) throws ObjectModificationException {
        BankAccount createdBankAccount;

        createdBankAccount = BANK_ACCOUNT_SERVICE.createBankAccount(bankAccount);

        return Response.ok(createdBankAccount).build();
    }
}
