package net.rubyeye.xmemcached.uds;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;

import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.buffer.BufferAllocator;
import net.rubyeye.xmemcached.command.Command;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.impl.MemcachedConnector;
import net.rubyeye.xmemcached.impl.ReconnectRequest;
import net.rubyeye.xmemcached.networking.MemcachedSession;
import net.rubyeye.xmemcached.utils.Protocol;

import com.google.code.juds.UnixDomainSocket;
import com.google.code.juds.UnixDomainSocketClient;
import com.google.code.yanf4j.config.Configuration;
import com.google.code.yanf4j.core.Session;
import com.google.code.yanf4j.core.impl.FutureImpl;

public class UDSocketConnector extends MemcachedConnector {

	public UDSocketConnector(Configuration configuration,
			MemcachedSessionLocator locator, BufferAllocator allocator,
			Protocol protocol, int poolSize) {
		super(configuration, locator, allocator, protocol, poolSize);
	}

	@Override
	public Future<Boolean> connect(InetSocketAddress address, int weight)
			throws IOException {
		UDSocketAddress udsocketAddress = (UDSocketAddress) address;
		UnixDomainSocketClient client = new UnixDomainSocketClient(
				udsocketAddress.getPath(), UnixDomainSocket.SOCK_STREAM);
		this.addSession(new UDSocketSession(udsocketAddress.getPath(), weight,
				client, this.getBufferAllocator()));
		FutureImpl<Boolean> result = new FutureImpl<Boolean>();
		result.setResult(true);
		return result;
	}

	@Override
	public void send(Command packet) throws MemcachedException {
		final Session session = findSessionByKey(packet.getKey());
		if (session == null) {
			throw new MemcachedException(
					"There is no avriable session at this moment");
		}
		session.write(packet);
		synchronized (session) {
			session.write(packet);
			try {
				((UDSocketSession) session).readFromInputStream(packet);
			} catch (IOException e) {
				session.close();
				this.addToWatingQueue(new ReconnectRequest(session
						.getRemoteSocketAddress(), 0,
						((MemcachedSession) session).getWeight()));
				throw new MemcachedException(e);
			}
		}

	}
}
