
# Introduction #
Supports all memcached text based protocols and binary base protocols currently, include  get/gets、set、add、replace、delete、append、prepend、cas、multi get/gets、incr、decr、version、stats、flush\_all etc.

# Dependencies #

> Xmemcached use [slf4j](http://www.slf4j.org/) for logging.

> If you are using maven to build your project,you would just add a dependence with xmemcached:
```
  <dependency>
       <groupId>com.googlecode.xmemcached</groupId>
       <artifactId>xmemcached</artifactId>
       <version>${version}</version>
  </dependency>
```

# Simple Example #
```
MemcachedClient client=new XMemcachedClient("host",11211);

//store a value for one hour(synchronously).
client.set("key",3600,someObject);
//Retrieve a value.(synchronously).
Object someObject=client.get("key");
//Retrieve a value.(synchronously),operation timeout two seconds.
someObject=client.get("key",2000);

//Touch cache item ,update it's expire time to 10 seconds.
boolean success=client.touch("key",10);

//delete value
client.delete("key");

```

# Weighted Server #
```
   MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   MemcachedClient memcachedClient=builder.build();
```

It sets "localhost:12000" weight to 1,and "localhost:12000" weight to 3.You can change the weight dynamically through JMX
```
   public interface XMemcachedClientMBean{
             ....
         /**
	 * Set a memcached server's weight
	 * 
	 * @param server
	 * @param weight
	 */
	public void setServerWeight(String server, int weight);
   }
```
# Key Iterator #
This feature is only avaliable on xmemcached 1.2.2 when using text protocol.You can get a KeyIterator through:
```
MemcachedClient client=...
KeyIterator it=client.getKeyIterator(AddrUtil.getOneAddress("localhost:11211"));
while(it.hasNext())
{
   String key=it.next();
}
```

# SASL authentication #

Memcached 1.4.3 has supported SASL authentication for client.It is only valid for binary protocol.Xmemcached 1.2.5 has supported this feature.If the memcached server enable SASL and use CRAM-MD5 or PLAIN mechanisms, set username as "cacheuser" and password as "123456",then you can use xmemcached like this:
```
   MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses(server));
   builder.addAuthInfo(AddrUtil.getOneAddress(server), AuthInfo
					.typical("cacheuser", "password"));
    // Must use binary protocol
    builder.setCommandFactory(new BinaryCommandFactory());
    MemcachedClient client=builder.build();
```

# Use Counter to incr/decr #

You can use MemcachedClient's incr/decr methods to increase or decrease counter,but xmemcached has a counter which encapsulate the incr/decr methods,you can use counter just like AtomicLong:
```
Counter counter=client.getCounter("counter",0);
counter.incrementAndGet();
counter.decrementAndGet();
counter.addAndGet(-10);
```

# Use binary protocol #
If you use memcached 1.4.0 or later version,you may want to use binary protocol insteadof text protocol which is used by xmemcached default.You can set it by
```
   MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setCommandFactory(new BinaryCommandFactory());//use binary protocol 
   MemcachedClient memcachedClient=builder.build();

```

Then xmemcached will use binary protocol to encode/decode commands.

# Talk with Kestrel #
> Kestrel is open source MQ server written in scala.It supports memcached text protocol,but not all compatible.For example,it does'n support "flag",you can not store java serializable object when use many java memcached clients.XMemcached add a KestrelCommandFactory to support Kestrel,If you use it,you could have those benefit:

  * Disable get optimiezed by default,because Kestrel doesn't support bulk get.
  * Support blocking fetch、reliable fetch and transaction
  * Allow to store any java serializable object to Kestrel.

> To use KestrelCommandFactory :

```
   MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setCommandFactory(new KestrelCommandFactory());
   MemcachedClient memcachedClient=builder.build();
```
> Additional about store java serializable object,because kestrel doesn't support "flag",so XMemcached add 4-bytes as flag before stored value.If your applications all use xmemcached as client,it is no problem;If not,There is a compatible problem,so xmemcached allow you to disable this feature by
```
  client.setPrimitiveAsString(true);
```


Set it true means xmemcached will store all java primitive type as string.

# Talking with Tokyo Tyrant #

Using TokyoTyrantTranscoder when you want to talk with TokyoTyrant,Just like Kestrel,TokyoTyrant doesn't support "flag",So TokyoTyrantTranscoder add 4-bytes flag before the value to store in TokyoTyrant.
```
  builder.setTranscoder(new TokyoTyrantTranscoder());
```

# Add/Remove memcached servers dynamically #

> Xmemcached supports add or remove memcached servers dynamically through programming or JMX MBeans.Add or remove memcached servers example

```
   MemcachedClient client=new XMemcachedClient(AddrUtil.getAddresses("server1:11211 server2:11211"));
   //Add two new memcached nodes
   client.addServer("server3:11211 server4:11211");
   //Remove memcached servers
   client.removeServer("server1:11211 server2:11211");
```

# Nio connection pool #
> In a high concurrent enviroment,you may want to pool memcached clients.But a xmemcached client has to start a reactor thread and some thread pools,if you create too many clients,the cost is very large.Xmemcached supports connection pool instreadof client pool.you can create more connections to one or more memcached servers,and these connections share the same reactor and thread pools.Note,Your application must ensure updating data is synchronized between connections which connected to the same memcached server.Seting pool size:
```
  MemcachedClientBuilder builder = new       XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000"));

  builder.setConnectionPoolSize(5); //set connection pool size to five
```

# Using CAS #

> Using CASOperation instead of using gets and cas methods.Here is a simple example,start NUM threads to increase the value of key "a"
```
     class CASThread extends Thread {
	static final class IncrmentOperation implements CASOperation<Integer> {
		/*
                 *Max repeat times.if repeat times is great than this value,
                 *xmemcached will throw a TimeoutException.
                 */
                @Override
		public int getMaxTries() {
			return Integer.MAX_VALUE; 
		}

                //increase current value                
		@Override
		public Integer getNewValue(long currentCAS, Integer currentValue) {
			return currentValue + 1; // current value + 1
		}
	}

	private XMemcachedClient mc;
	private CountDownLatch cd;

	public CASThread(XMemcachedClient mc, CountDownLatch cdl) {
		super();
		this.mc = mc;
		this.cd = cdl;

	}

	public void run() {
		try {
                        //do the cas operation
			if (mc.cas("a", 0, new IncrmentOperation()))
				this.cd.countDown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public class CASTest {

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.err.println("Usage:java CASTest [threadNum] [server]");
		    System.exit(1);
		}
                //threads num
		int NUM = Integer.parseInt(args[0]);
		XMemcachedClient mc = new XMemcachedClient(AddrUtil.getAddresses(args[1]));
		//initial value is 0
		mc.set("a", 0, 0);
		CountDownLatch cdl = new CountDownLatch(NUM);
		long start = System.currentTimeMillis();
		//start NUM threads to increment the value
		for (int i = 0; i < NUM; i++)
			new CASThread(mc, cdl).start();

		cdl.await();
		System.out.println("test cas,timed:"
				+ (System.currentTimeMillis() - start));
		System.out.println("result=" + mc.get("a"));
		mc.shutdown();
	}
}

   
```

# Intergrating Spring framework #

Please see [Spring\_Integration](Spring_Integration.md)