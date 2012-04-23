/**
 * 
 */
package com.zenika.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author acogoluegnes
 *
 */
public class TransferRequestRowMapper implements RowMapper<TransferRequest> {

	/* (non-Javadoc)
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public TransferRequest mapRow(ResultSet rs, int rowNum) throws SQLException {
		TransferRequest request = new TransferRequest(
			rs.getLong("id"),
			rs.getLong("account_id"),
			rs.getInt("amount")
		); 
		return request;
	}

}
