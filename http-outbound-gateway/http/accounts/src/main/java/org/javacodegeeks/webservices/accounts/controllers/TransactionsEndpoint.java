package org.javacodegeeks.webservices.accounts.controllers;

import java.util.ArrayList;
import java.util.List;

import org.javacodegeeks.webservices.accounts.domain.Income;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionsEndpoint {

	static List<Income> incomeList = new ArrayList<Income>();

	static {
		incomeList.add(new Income("First Income", 1000.0));
	}

	@GetMapping("/incomes")
	public List<Income> getAllIncomes() {
		return incomeList;
	}
}
