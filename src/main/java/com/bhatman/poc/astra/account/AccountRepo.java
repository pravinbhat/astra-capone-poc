package com.bhatman.poc.astra.account;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.Repository;

public interface AccountRepo extends Repository<Account, UUID> {
	<S extends Account> S save(S entity);

	Optional<Account> findById(UUID primaryKey);

	Iterable<Account> findAll();

	long count();

	void delete(Account entity);

	void deleteById(UUID accountId);

	boolean existsById(UUID primaryKey);
}
