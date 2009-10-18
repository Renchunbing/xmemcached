package net.rubyeye.xmemcached.uds;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClient;

public class Test {
	public static void main(String []args)throws Exception{
		MemcachedClient client=new XMemcachedClient(new UDSocketAddress("/home/dennis/memcached"));
		client.set("hello", 0, "hello");
		
	}
}
