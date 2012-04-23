/**
 * 
 */
package com.zenika.batch;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author acogoluegnes
 *
 */
public class BusinessItemWriter implements ItemWriter<TransferRequest> {
	
	private final JdbcOperations tpl;
	
	public BusinessItemWriter(DataSource ds) {
		tpl = new JdbcTemplate(ds);
	}

	@Override
	public void write(List<? extends TransferRequest> items) throws Exception {
		doCheck(items);
		doBusinessProcessing(items);
		markIncomingItemAsProcessed(items);
	}

	private void doCheck(List<? extends TransferRequest> items) {
		for(TransferRequest request : items) {
			if(request.getAmount() < 0) {
				throw new IllegalArgumentException("We process only credits!");
			}
		}
	}

	private void doBusinessProcessing(final List<? extends TransferRequest> items) {
		tpl.batchUpdate(
			"update account set balance = balance + ? where id = ?",
			new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setInt(1,items.get(i).getAmount());
					ps.setLong(2,items.get(i).getAccountId());
				}
				@Override
				public int getBatchSize() {
					return items.size();
				}
			}
		);
	}
	
	private void markIncomingItemAsProcessed(
			final List<? extends TransferRequest> items) {
		tpl.batchUpdate(
			"update transfer_request set processed = ? where id = ?",
			new BatchPreparedStatementSetter() {
				
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					ps.setBoolean(1,true);
					ps.setLong(2,items.get(i).getId());					
				}
				
				@Override
				public int getBatchSize() {
					return items.size();
				}
			}
		);
	}
	
}
