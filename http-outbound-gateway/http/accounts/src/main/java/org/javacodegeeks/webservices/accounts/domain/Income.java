package org.javacodegeeks.webservices.accounts.domain;

public class Income extends AccountsTransaction {

	public Income(String description, Double amount) {
		this.description = description;
		this.amount = amount;
	}
}
