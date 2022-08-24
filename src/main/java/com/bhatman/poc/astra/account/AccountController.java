package com.bhatman.poc.astra.account;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.datastax.oss.driver.api.core.uuid.Uuids;

@RestController
@RequestMapping("/accounts")
public class AccountController {

	@Autowired
	AccountRepo accountRepo;
	
	@Autowired
	MetricRegistry registry;

	@GetMapping
	public ResponseEntity<List<Account>> all() throws Exception {
		List<Account> accounts = new ArrayList<Account>();
		accountRepo.findAll().forEach(accounts::add);

		if (accounts.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}

		Map<String, Metric> metricsMap = registry.getMetrics();
		metricsMap.entrySet().stream().filter(entry -> entry.getKey().contains("speculative-executions")).forEach(entry -> {
			System.out.println("Metric Name: " + entry.getKey());
			System.out.println("Metric Val: " + ((Counter) entry.getValue()).getCount());
		});

		return new ResponseEntity<>(accounts, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<AccountResponse> add(@RequestBody Account newAccount) {
		Account account = accountRepo.save(new Account(Uuids.timeBased(), newAccount.getAccountName()));
		return new ResponseEntity<>(new AccountResponse(account, "Account created!"), HttpStatus.CREATED);
	}

	@GetMapping("/{accountId}")
	public ResponseEntity<AccountResponse> get(@PathVariable UUID accountId) {
		Optional<Account> account = accountRepo.findById(accountId);

		if (account.isPresent()) {
			return new ResponseEntity<>(new AccountResponse(account.get(), "Account found!"), HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PutMapping("/{accountId}")
	public ResponseEntity<AccountResponse> update(@RequestBody Account updateAccount, @PathVariable UUID accountId) {
		Objects.requireNonNull(updateAccount);
		if(!accountId.equals(updateAccount.getAccountId())) {
			// "Account Id provided does not match the value in path"
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
		ResponseEntity<AccountResponse> re = get(accountId);
		//Assert.isTrue(re.getStatusCode().equals(HttpStatus.OK), "No such Account exists for Id " + accountId);
		if(!re.getStatusCode().equals(HttpStatus.OK)) {
			// "Account Id provided does not match the value in path"
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(new AccountResponse(accountRepo.save(updateAccount), "Account updated!"),
				HttpStatus.OK);
	}

	@DeleteMapping("/{accountId}")
	public ResponseEntity<HttpStatus> delete(@PathVariable UUID accountId) {
		accountRepo.deleteById(accountId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
