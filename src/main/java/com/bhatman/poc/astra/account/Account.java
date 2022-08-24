package com.bhatman.poc.astra.account;

import java.util.UUID;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Table
public class Account {
	@PrimaryKey(value = "account_id")
	private UUID accountId;

	@Column(value = "account_name")
	private String accountName;
}
