package net.rubyeye.xmemcached.uds;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.buffer.BufferAllocator;
import net.rubyeye.xmemcached.command.Command;
import net.rubyeye.xmemcached.networking.MemcachedSession;

import com.google.code.juds.UnixDomainSocketClient;
import com.google.code.yanf4j.core.Session;
import com.google.code.yanf4j.core.CodecFactory.Decoder;
import com.google.code.yanf4j.core.CodecFactory.Encoder;

@SuppressWarnings("unchecked")
public class UDSocketSession implements Session, MemcachedSession {
	private final UnixDomainSocketClient socketClient;
	private final InputStream in;
	private final DataOutputStream out;
	private volatile boolean closed;
	private final ByteBuffer readBuffer = ByteBuffer
			.allocate(MemcachedClient.DEFAULT_SESSION_READ_BUFF_SIZE);
	private final UDSocketAddress remotingAddress;
	private final BufferAllocator allocator;
	private final int weight;
	private final Lock lock = new ReentrantLock();
	final byte[] buffer = new byte[MemcachedClient.DEFAULT_SESSION_READ_BUFF_SIZE / 2];

	public int getWeight() {
		return this.weight;
	}

	private final ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

	public Lock getLock() {
		return this.lock;
	}

	public UDSocketSession(String path, int weight,
			UnixDomainSocketClient client, BufferAllocator bufferAllocator) {
		this.socketClient = client;
		this.weight = weight;
		this.in = this.socketClient.getInputStream();
		this.out = new DataOutputStream(this.socketClient.getOutputStream());
		this.remotingAddress = new UDSocketAddress(path);
		this.allocator = bufferAllocator;
	}

	public Future<Boolean> asyncWrite(Object packet) {
		throw new UnsupportedOperationException();
	}

	public void clearAttributes() {
		attributes.clear();

	}

	public void close() {
		if (this.closed)
			return;
		closed = true;
		this.socketClient.close();
		this.attributes.clear();
	}

	public void flush() {
		try {
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public Decoder getDecoder() {
		return null;
	}

	public Encoder getEncoder() {
		return null;
	}

	public long getLastOperationTimeStamp() {
		return 0;
	}

	public ByteOrder getReadBufferByteOrder() {
		return this.readBuffer.order();
	}

	public InetSocketAddress getRemoteSocketAddress() {
		return this.remotingAddress;
	}

	public long getScheduleWritenBytes() {
		return 0;
	}

	public long getSessionIdleTimeout() {
		return 0;
	}

	public long getSessionTimeout() {
		return this.socketClient == null ? 0 : this.socketClient.getTimeout();
	}

	public boolean isClosed() {
		return this.closed;
	}

	public boolean isExpired() {
		return false;
	}

	public boolean isHandleReadWriteConcurrently() {

		return false;
	}

	public boolean isLoopbackConnection() {

		return false;
	}

	public boolean isUseBlockingRead() {

		return false;
	}

	public boolean isUseBlockingWrite() {
		return false;
	}

	public void removeAttribute(String key) {
		this.attributes.remove(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);

	}

	public Object setAttributeIfAbsent(String key, Object value) {
		return this.attributes.putIfAbsent(key, value);
	}

	public void setDecoder(Decoder decoder) {

	}

	public void setEncoder(Encoder encoder) {

	}

	public void setHandleReadWriteConcurrently(
			boolean handleReadWriteConcurrently) {

	}

	public void setReadBufferByteOrder(ByteOrder readBufferByteOrder) {
		this.readBuffer.order(readBufferByteOrder);

	}

	public void setSessionIdleTimeout(long sessionIdleTimeout) {

	}

	public void setSessionTimeout(long sessionTimeout) {

	}

	public void setUseBlockingRead(boolean useBlockingRead) {

	}

	public void setUseBlockingWrite(boolean useBlockingWrite) {

	}

	public void start() {

	}

	public void readFromInputStream(Command command) throws IOException {
		while (true) {
			int readCount = in.read(buffer);
			if (readCount > 0) {
				this.readBuffer.put(buffer, 0, readCount);
				this.readBuffer.flip();
				if (command.decode(null, this.readBuffer))
					break;
				this.readBuffer.compact();
			}
		}
		this.readBuffer.clear();

	}

	public void write(Object packet) {
		Command command = (Command) packet;
		command.encode(this.allocator);
		ByteBuffer buffer = command.getIoBuffer().getByteBuffer();
		final byte[] data = buffer.array();
		try {
			out.write(data, 0, data.length);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException("Write command error", e);
		}
	}

}
