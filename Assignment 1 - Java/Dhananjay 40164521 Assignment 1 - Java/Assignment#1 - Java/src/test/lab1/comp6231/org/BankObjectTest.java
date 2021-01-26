package lab1.comp6231.org;

import static org.junit.Assert.assertEquals;

public class BankObjectTest {

    @org.junit.Test
    public void shouldCreateSavingAccAndReturnName() {

        //First account is checking account
        BankAccount account = new CheckingAccount();
        //Then assert
        assertEquals(account.toString(),"The balance is of the CHECKING ACCOUNT 1 is 0.0");
        //Assert.that(0 == account.balance, "");

    }

    @org.junit.Test
    public void shouldCreateSavingAccAndReturnName2() {

        //Second account is saving account
        BankAccount account2 = new SavingsAccount();
        //Then assert
        assertEquals(account2.toString(),"The balance is of the SAVINGS ACCOUNT 2 is 0.0");
        //Assert.that(0 == account.balance, "");


    }

    @org.junit.Test
    public void shouldCreateSavingAccAndReturnName3() {

        //Third account is saving account
        BankAccount account3 = new SavingsAccount();
        //Then assert
        assertEquals(account3.toString(),"The balance is of the SAVINGS ACCOUNT 3 is 0.0");
        //Assert.that(0 == account.balance, "");

    }
}