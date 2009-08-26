package net.rubyeye.xmemcached.test.unittest.utils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import junit.framework.TestCase;
import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class XMemcachedClientFactoryBeanUnitTest extends TestCase {

	ApplicationContext ctx;

	@Override
	public void setUp() throws Exception {
		ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
	}

	public void testSimpleConfig() throws Exception {
		MemcachedClient memcachedClient = (MemcachedClient) ctx
				.getBean("memcachedClient1");
		validateClient(memcachedClient);
	}

	public void testAllConfig() throws Exception {
		MemcachedClient memcachedClient = (MemcachedClient) ctx
				.getBean("memcachedClient2");
		validateClient(memcachedClient);
	}

	private void validateClient(MemcachedClient memcachedClient)
			throws TimeoutException, InterruptedException, MemcachedException,
			IOException {
		assertNotNull(memcachedClient);
		assertTrue(memcachedClient.getConnector().isStarted());
		assertFalse(memcachedClient.isShutdown());
		memcachedClient.set("test", 0, 1);
		assertEquals(1, memcachedClient.get("test"));
		memcachedClient.shutdown();
	}

}