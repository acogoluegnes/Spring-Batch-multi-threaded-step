/**
 * 
 */
package com.zenika.batch;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * @author acogoluegnes
 *
 */
public class SynchronizedItemReader<T> implements ItemReader<T>,ItemStream {
	
	private final ItemReader<T> delegate;
	
	public SynchronizedItemReader(ItemReader<T> delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public synchronized T read() throws Exception, UnexpectedInputException, ParseException,
			NonTransientResourceException {
		return delegate.read();
	}
	
	@Override
	public void open(ExecutionContext executionContext)
			throws ItemStreamException {
		if(delegate instanceof ItemStream) {
			((ItemStream) delegate).open(executionContext);
		}
	}
	
	@Override
	public void update(ExecutionContext executionContext)
			throws ItemStreamException {
		if(delegate instanceof ItemStream) {
			((ItemStream) delegate).update(executionContext);
		}
	}
	
	@Override
	public void close() throws ItemStreamException {
		if(delegate instanceof ItemStream) {
			((ItemStream) delegate).close();
		}
	}
	
	
	
}
