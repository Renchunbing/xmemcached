package net.rubyeye.xmemcached.uds;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.XMemcachedClient;
import net.rubyeye.xmemcached.buffer.BufferAllocator;
import net.rubyeye.xmemcached.networking.Connector;
import net.rubyeye.xmemcached.utils.Protocol;

import com.google.code.yanf4j.config.Configuration;
import com.google.code.yanf4j.util.SystemUtils;

public class UDSocketMemcachedClient extends XMemcachedClient {

	public UDSocketMemcachedClient() throws IOException {
		super();
	}

	public UDSocketMemcachedClient(InetSocketAddress inetSocketAddress,
			int weight) throws IOException {
		super(inetSocketAddress, weight);
	}

	public UDSocketMemcachedClient(InetSocketAddress inetSocketAddress)
			throws IOException {
		super(inetSocketAddress);

	}

	public UDSocketMemcachedClient(List<InetSocketAddress> addressList)
			throws IOException {
		super(addressList);

	}

	public UDSocketMemcachedClient(String server, int port, int weight)
			throws IOException {
		super(server, port, weight);
	}

	@Override
	protected InetSocketAddress newSocketAddress(final String server,
			final int port) {
		return new UDSocketAddress(server);
	}

	@Override
	protected void checkSocketAddress(InetSocketAddress address) {
		if (!(address instanceof UDSocketAddress)) {
			throw new IllegalArgumentException(
					"Please use UDSocketAddress instead");
		}
	}

	public UDSocketMemcachedClient(String server, int port) throws IOException {
		super(server, port);
	}

	{
		// default pool size
		this.connectionPoolSize = SystemUtils.getSystemThreadCount();
	}

	@Override
	public void setConnectionPoolSize(int poolSize) {
		if (poolSize <= 0)
			throw new IllegalArgumentException("poolSize<=0");
		this.connectionPoolSize = poolSize;
		this.connector.setConnectionPoolSize(poolSize);
	}

	@Override
	protected Connector newConnector(BufferAllocator bufferAllocator,
			Configuration configuration,
			MemcachedSessionLocator memcachedSessionLocator, Protocol protocol,
			int i) {
		return new UDSocketConnector(configuration, memcachedSessionLocator,
				bufferAllocator, protocol, i);
	}

}
