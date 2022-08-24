package com.bhatman.poc.astra.account;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountResponse {
	private Account account;
	private String message;
}
