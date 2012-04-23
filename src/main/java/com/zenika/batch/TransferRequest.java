/**
 * 
 */
package com.zenika.batch;

/**
 * @author acogoluegnes
 *
 */
public class TransferRequest {

	private final Long id;
	
	private final boolean processed = false;
	
	private final Long accountId;
	
	private final Integer amount;
	
	public TransferRequest(Long id, Long accountId, Integer amount) {
		super();
		this.id = id;
		this.accountId = accountId;
		this.amount = amount;
	}

	public Long getId() {
		return id;
	}

	public boolean isProcessed() {
		return processed;
	}

	public Long getAccountId() {
		return accountId;
	}

	public Integer getAmount() {
		return amount;
	}

		
}
