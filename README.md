                       Money Transfer Application

A Java RESTful API for money transfers between users accounts

Application starts a Grizzly server on localhost port 8080 with H2 in memory database initialized with some sample users account data to view.
This API guaranties the data consistency in any case. Even if it will be a huge amount concurrent user. This ability was achieved by using of select ... for update database feature which helps to lock the object until all related objects will be updated/created
It uses just to entities:
* transaction - the money transfer transaction used to initialize the transaction
* bank account - the bank account which has balance in the specified currency
Required Technologies
* Java 8
* Maven
* JAX-RS API
* H2 in memory database
* Log4j
* Grizzly Container (for Test and Demo app)
* Apache HTTP Client
* Junit
* TestNG
* Allure 
* Jacoco

How to start
Once the application is fetched from git it can be built with maven
Command : mvn clean install test allure:serve allure:report 
This will fetch dependencies and run all tests. It will compile and run all Junit test cases and open Allure report automatically in browser.
Overall Report

Test Suite Report


Overall Package Unit Test Report
 

TestNG Report: Bank\target\surefire-reports\index.html

TestNG Report: Bank\target\surefire-reports\emailable-report.html.html

Jacoco Code Coverage Report : Bank\target\site\jacoco\index.html

Jacoco Session Report : Bank\target\site\jacoco\ jacoco-sessions.html



To run only application to execute:
Command : java -jar target\Bank-0.0.1-jar-with-dependencies.jar
The application will start on the localhost and will be listening to the port 8080

API Definition
Bank Account
The bank account entity which has balance and could transfer the money if there is enough fund.
Structure
{
    "id": <number>,
    "ownerName": <string>,
    "balance": <double>,
    "blockedAmount": <double>,
}

Create Bank Account
The following creates bank account and returns the created entity with ID specified
POST http://localhost:8080/api/v1/accounts

Get All Bank Accounts
The following gets all the bank accounts that exist in the system.
Note : First three account created during application started.
GET http://localhost:8080/api/v1/accounts

Get A Particular Bank Account details
The following gets the particular account if it exists in the system
GET http://localhost:8080/api/v1/accounts/1

Update A Particular Bank Account details
The following updates the details of the particular account if it exists in the system You can not update any field except "ownerName"
PUT http://localhost:8080/api/v1/accounts

 

Transaction
The money transfer transaction used to initialize the transaction. Once created will be executed automatically. If transaction can not be created by some reason the Error(HTTP 500 Internal Error) will be returned with details in the body. You can not update transaction object as it is controversial to the logic that transaction can not be modified once created.
Structure
{
    "id": <number>,
    "fromBankAccountId": <number>,
    "toBankAccountId": <number>,
    "amount": <double>,
    "creationDate": <timestamp>,
    "updateDate": <timestamp>,
    "status": <string - one from "PLANNED", "PROCESSING", "FAILED", "SUCCEED">,
    "failMessage": <string>
}
Create a transaction
POST http://localhost:8080/api/v1/transactions
The following creates a new transaction if possible (valid Bank Accounts and parameters should be provided). Once id, creationDate, updateDate or status provided they will be ignored. You can obtain the generated values of these fields in the response of this call.

Get all transactions 
GET http://localhost:8080/api/v1/transactions
You can see the transaction status : SUCCEED
Get a specific transaction by its ID
GET http://localhost:8080/api/v1/transactions/1
 

Balance Check
You can see amount 25.5 transferred from account id# 1 to 2

Let Return Amount 25.5 from Account id# 2 to 1 by submitting a transaction request

You can see that Amount 25.5 returned from account id #2 to 1

Exception Handing
If any error will be thrown by some reason the Error (HTTP 500 Internal Error) will be returned with details in the body.
Example response:
HTTP 500 Internal Error
{
    "type": "OBJECT_IS_NOT_FOUND",
    "name": "The entity with provided ID has not been found",
    "message": "Some details",
}    

