package lab1.comp6231.org;

import java.util.ArrayList;


public class Client {

	public static void main(String args[]) {

	// TODO: create 3 account objects (one checking and two saving accounts): ca, sa1, sa2
		CheckingAccount ca = new CheckingAccount(); //created the 3 objects of classtypes as specified
		SavingsAccount sa1 = new SavingsAccount();
		SavingsAccount sa2 = new SavingsAccount();
		
	//TODO: create a generic list called: accounts
             ArrayList<BankAccount> accounts = new ArrayList<BankAccount>(); // Created the arraylist of BankAccount class type
        
	//TODO: add all the three acounts to the list "accounts"
	accounts.add(ca); 
	accounts.add(sa1);
	accounts.add(sa2);

	//TODO: print the information of all the three accounts
	for (BankAccount ba: accounts)           //Using enhanced for loop to print the information of all the 3 objects
        System.out.println(ba.toString()); 
		
	}
}
