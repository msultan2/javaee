package com.ssl.bluetruth.receiver.v2.misc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class RequestParserFactory {
	
	@Autowired
	ApplicationContext applicationContext;
	
	@SuppressWarnings("unchecked")
	public <T> RequestParser<T> getRequestParser(Class<T> type) {
		return (RequestParser<T>)applicationContext.getBean("requestParser", (Object)type);
	}
}
