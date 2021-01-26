package lab1.comp6231.org;

public abstract class BankAccount {

	protected int accNum;
	protected double balance;
	protected static int serialNum = 0;
	
	/** Default constructor 
	 * 
	 */
	public BankAccount()
	{
        // TODO
	    // check the balance
		balance=0.0;       // Assigning the initial balance as 0 when the object is created without any parameters
        // check the account Number
		serialNum+=1;     // incrementing the account number when created
		accNum=serialNum;
	}
	
	/** Overloaded constructor
	 */
	public BankAccount( double startBalance) throws Exception
	{
        // TODO
	    //deposit the balance
		balance=startBalance;   // // Assigning the initial balance with the value passed as parameter when the object is created
        //check the account number
		serialNum+=1;  // for incrementing the account number when created
		accNum=serialNum;
	}
	
	/** accessor for balance
	 * 
	 */
	public double getBalance()
	{
        // TODO
        // get the balance
		return balance;
    }
	
	/* accessor for account number
	 * 
	 */
	public int getAccNum()
	{
		return accNum;
	}
	
	/** Deposit amount to account
	 * 
	 */
	public void deposit( double amount ) throws Exception
	{
        // TODO
        // deposit amount of money, if it is legal/valid amount
		if(amount>0.0)             // balance is updated only when the deposit amount is greater than 0
			balance+=amount;
		else
			throw new Exception("Invalid Amount (Zero and Negative Values not Accepted)");
		
		
	}
	
	/** withdraw amount from account
	 * 
	 */
	public void withdraw( double amount ) throws Exception
	{
		if(amount >= 0.0 && amount <= balance)
			balance -= amount;
		
		else
			throw new Exception("Insufficient Balance");
	}

	/**Override toString()
	 *
	 */
	public String toString()
	{    
		
		String ss=String.format("The balance is of the %s %d is %.1f",accType(),accNum,getBalance());
		return ss;
		
	}
	
	public abstract void applyComputation();
	public abstract String accType();
}