package net.projectmythos.argos.framework.exceptions.preconfigured;

public class NegativeBalanceException extends PreConfiguredException {

	public NegativeBalanceException() {
		super("Balances cannot be negative");
	}

}
