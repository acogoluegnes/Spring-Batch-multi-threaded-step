/**
 * 
 */
package com.zenika.batch;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.DiscreteDomains;
import com.google.common.collect.Lists;
import com.google.common.collect.Ranges;

/**
 * @author acogoluegnes
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@ActiveProfiles("dev")
public class MultiThreadedStepTest {
	
	@Autowired JobLauncher jobLauncher;
	
	@Autowired Job job;
	
	@Test public void trackingMultithreadedStepExecution() throws Exception {
		JobExecution exec = jobLauncher.run(
			job,
			new JobParametersBuilder().addLong("time",System.currentTimeMillis()).toJobParameters()
		);
		assertEquals(BatchStatus.COMPLETED,exec.getStatus());
	}
	
	@Configuration
	@ImportResource("classpath:/multi-threaded-step-execution-context.xml")
	static class Config {
		
		@Bean public ItemReader<String> reader() {
			final List<Integer> list = Collections.synchronizedList(Lists.newArrayList(Ranges.closed(1,100).asSet(DiscreteDomains.integers())));
			return new ItemReader<String>() {
				@Override
				public String read() throws Exception,
						UnexpectedInputException, ParseException,
						NonTransientResourceException {
					if(list.isEmpty()) {
						return null;
					} else {
						Integer item = list.remove(0);
						LoggerFactory.getLogger("com.zenika").debug("Reading {} from {}",item,Thread.currentThread());
						return item.toString();
					}
				}
			};
		}
		
		@Bean public ItemProcessor<String,String> processor() {
			return new ItemProcessor<String,String>() {
				@Override
				public String process(String item) throws Exception {
					LoggerFactory.getLogger("com.zenika").debug("Processing {} from {}",item,Thread.currentThread());
					return item;
				}
			};
		}
		
		@Bean public ItemWriter<String> writer() {
			return new ItemWriter<String>() {
				@Override
				public void write(List<? extends String> items)
						throws Exception {
					LoggerFactory.getLogger("com.zenika").debug("Writing {} from {}",items,Thread.currentThread());					
				}
			};
		}
		
	}
	
}
