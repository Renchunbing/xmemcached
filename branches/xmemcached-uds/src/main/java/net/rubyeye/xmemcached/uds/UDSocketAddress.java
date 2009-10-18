package net.rubyeye.xmemcached.uds;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UDSocketAddress extends InetSocketAddress {
	static final long serialVersionUID = 14968406056054605L;
	private String path;

	public String getPath() {
		return this.path;
	}

	@Override
	public String toString() {
		return this.path;
	}

	public UDSocketAddress(String path) {
		super("localhost", 1111);
		this.path = path;

	}

	public UDSocketAddress(InetAddress addr, int port) {
		super(addr, port);
	}

	public UDSocketAddress(int port) {
		super(port);

	}

	public UDSocketAddress(String hostname, int port) {
		super(hostname, port);

	}
}
