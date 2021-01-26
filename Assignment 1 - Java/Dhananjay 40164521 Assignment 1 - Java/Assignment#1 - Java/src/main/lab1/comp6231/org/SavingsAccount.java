package lab1.comp6231.org;

public class SavingsAccount extends BankAccount {

	public final double DEFAULT_FEE = .02;
	private double interestRate;
	
	public SavingsAccount()
	{
		super();
		interestRate = DEFAULT_FEE;
	}
	
	public SavingsAccount( double startBalance, double startInterestRate ) throws Exception
	{
		super(startBalance);
		setInterestRate(startInterestRate);
	}
	
	public double getInterestRate()
	{
		return interestRate;
	}
	
	public void setInterestRate( double newInterestRate ) throws Exception
	{
		if( newInterestRate >= 0.0 )
			interestRate = newInterestRate;
			
		else
			throw new Exception("Invalid Interest Rate");
			
	}
	
	public void applyInterestRate() 
	{
		balance += (interestRate * balance * 30);
	}
	
	public void applyComputation()
	{
		applyInterestRate();
	}

	//TODO: add the missing required methods
	public String accType()  //Added the accType() method to return the account type as savings
	{
		return "SAVINGS ACCOUNT";
		
	}
	

}
