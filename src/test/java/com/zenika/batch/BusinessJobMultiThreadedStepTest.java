/**
 * 
 */
package com.zenika.batch;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Random;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author acogoluegnes
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/business-job-multi-threaded-step-context.xml")
@ActiveProfiles("dev")
public class BusinessJobMultiThreadedStepTest {
	
	private static final int NB_ROWS = 500;

	@Autowired JobLauncher jobLauncher;
	
	@Autowired Job job;
	
	@Autowired DataSource ds;
	
	JdbcOperations tpl;
	
	@Before public void setUp() {
		tpl = new JdbcTemplate(ds);
		tpl.update("delete from transfer_request");
		tpl.update("delete from account");
		Random r = new Random(System.currentTimeMillis());
		for(int i=0;i<NB_ROWS;i++) {
			tpl.update(
				"insert into transfer_request (id,account_id,amount,processed) values (?,?,?,?)",
				i,r.nextInt(1000),r.nextInt(500)+1,false
			);
		}
		tpl.update("insert into account (id,balance) select distinct account_id,0 from transfer_request");
	}
	
	@Test public void sunnyDay() throws Exception {
		JobExecution exec = jobLauncher.run(
			job,
			new JobParametersBuilder().addLong("time",System.currentTimeMillis()).toJobParameters()
		);
		assertThat(exec.getStatus(),is(BatchStatus.COMPLETED));
		assertThat(countProcessedRequests(),is(NB_ROWS));
		assertThat(countUnprocessedRequests(),is(0));
		assertThat(countAccountsTotalBalance(),is(countRequestsTotalAmount()));
	}
	
	@Test public void restartOnError() throws Exception {
		insertPoisonPill();
		JobExecution exec = jobLauncher.run(
			job,
			new JobParametersBuilder().addLong("time",System.currentTimeMillis()).toJobParameters()
		);
		assertThat(exec.getStatus(),is(BatchStatus.FAILED));
		removePoisonPill();
		exec = jobLauncher.run(
			job,
			exec.getJobInstance().getJobParameters()
		);
		assertThat(exec.getStatus(),is(BatchStatus.COMPLETED));
		assertThat(countProcessedRequests(),is(NB_ROWS));
		assertThat(countUnprocessedRequests(),is(0));
		assertThat(countAccountsTotalBalance(),is(countRequestsTotalAmount()));
	}
	
	private void insertPoisonPill() {
		int updated = tpl.update("update transfer_request set amount = -amount where id = ?",new Random().nextInt(NB_ROWS));
		assertThat(updated,is(1));
	}
	
	private void removePoisonPill() {
		int updated = tpl.update("update transfer_request set amount = -amount where amount < 0");
		assertThat(updated,is(1));
	}

	private int countProcessedRequests() {
		return tpl.queryForInt("select count(1) from transfer_request where processed = 't'"); 
	}
	
	private int countUnprocessedRequests() {
		return tpl.queryForInt("select count(1) from transfer_request where processed = 'f'"); 
	}
	
	private int countRequestsTotalAmount() {
		return tpl.queryForInt("select sum(amount) from transfer_request");
	}
	
	private int countAccountsTotalBalance() {
		return tpl.queryForInt("select sum(balance) from account");
	}
	
}
