package lab1.comp6231.org;

public class CheckingAccount extends BankAccount{

	public final double DEFAULT_FEE = 5.00;
	private double monthlyFee;
	
	public CheckingAccount()
	{
		super();
		monthlyFee = DEFAULT_FEE;
	}
	
	public CheckingAccount( double startBalance, double startMonthlyFee ) throws Exception
	{
		super(startBalance);
		setMonthlyFee(startMonthlyFee);
	}
	
	public double getMonthlyFee()
	{
		return monthlyFee;
	}
	
	public void setMonthlyFee( double newMonthlyFee ) throws Exception
	{
		if( monthlyFee >= 0.0 )
			monthlyFee = newMonthlyFee;
			
		else
			throw new Exception("Invalid Monthly Fee");
			
	}
	
	public void applyMonthlyFee() throws Exception
	{
		balance -= monthlyFee;
		
		if(balance < 0.0)
			throw new Exception("Account is overdrawn");
	}
	
	public void applyComputation()
	{
		try {
			// TODO: call the applyMonthlyFee() method
			applyMonthlyFee();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String accType()
	{
		return "CHECKING ACCOUNT";
		
	}
}
