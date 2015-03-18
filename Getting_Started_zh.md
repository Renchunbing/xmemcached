
# 简单介绍 #
Xmemcached支持memcached所有的二进制协议（从1.2.0开始）和文本协议，并且支持对Kestrel(一个scala写的MQ)的兼容访问。
更多信息请参考[用户指南](http://code.google.com/p/xmemcached/wiki/User_Guide_zh)

# 依赖包 #


  * Xmemcached 依赖slf4j(http://www.slf4j.org/)



> 如果您使用maven构建，只需要添加依赖即可使用xmemcached:
```
 <dependency>
       <groupId>com.googlecode.xmemcached</groupId>
       <artifactId>xmemcached</artifactId>
       <version>版本号</version>
  </dependency>

```
# 简单例子 #
```
MemcachedClient client=new XMemcachedClient("host",11211);

//同步存储value到memcached，缓存超时为1小时，3600秒。
client.set("key",3600,someObject);
//从memcached获取key对应的value
Object someObject=client.get("key");

//从memcached获取key对应的value,操作超时2秒
someObject=client.get("key",2000);
//更新缓存的超时时间为10秒。
boolean success=client.touch("key",10);

//删除value
client.delete("key");

```

# 设置权重 #
```
   MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   MemcachedClient memcachedClient=builder.build();
```

> 上面的例子将"localhost:12000"节点的权重设置为1，而将"localhost:12001"节点的权重设置为3.当然，你也可以通过JMX调用MBean动态修改权重：
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
# 使用二进制协议 #
> Xmemcached默认使用成熟的文本协议，memcached 1.4.0正式支持启用二进制协议，如果你想使用二进制协议，你只要添加一行代码，设置BinaryCommandFactory即可：
```
   MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setCommandFactory(new BinaryCommandFactory());//use binary protocol 
   MemcachedClient memcachedClient=builder.build();

```

# 使用Kestrel #
> Kestrel是twitter开源的一个scala写的简单高效MQ，它支持memcached文本协议，但是并不完全兼容，例如它不支持flag，导致很多利用flag做序列化的客户端无法正常运作。因此Xmemcached特意提供了KestrelCommandFactory用于支持Kestrel。使用KestrelCommandFactory即可拥有如下好处：
  * 默认关闭get优化，因为kestrel不支持bulk get；
  * 支持kestrel的阻塞获取和可靠获取；
  * 允许向kestrel存储任意java序列化类型。设置KestrelCommandFactory:
```
   MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setCommandFactory(new KestrelCommandFactory());
   MemcachedClient memcachedClient=builder.build();
```

> 关于最后一点需要补充说明，由于kestrel不支持flag，因此xmemcached在存储的数据之前加了4个字节的flag，如果你的全部应用都使用xmemcached，那么没有问题，如果使用其他clients，会有兼容性的问题，因此Xmemcached还允许关闭这个功能，通过
```
  client.setPrimitiveAsString(true);
```

设置为true后，原生类型都将存储为字符串，而序列化类型将无法存储了。

# 动态增删节点 #

> 你可以通过编程或者JMX来动态增加或者移除memcached节点：

```
   MemcachedClient client=new XMemcachedClient(AddrUtil.getAddresses("server1:11211 server2:11211"));
   //Add two new memcached nodes
   client.addServer("server3:11211 server4:11211");
   //Remove memcached servers
   client.removeServer("server1:11211 server2:11211");
```

# Nio连接池 #
> Xmemcached是基于java nio的client实现，默认对一个memcached节点只有一个连接，这在通常情况下已经有非常优异的表现。但是在典型的高并发环境下,nio的单连接也会遇到性能瓶颈。因此XMemcached支持设置nio的连接池，允许建立多个连接到同一个memcached节点，但是请注意，这些连接之间是不同步的，因此你的应用需要自己保证数据更新的同步，启用连接池可以通过下面代码：
```
  MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000"));

  builder.setConnectionPoolSize(5);

```

# 使用CAS原子更新 #

> 典型的CAS操作需要先通过gets得到cas值，然后进行cas替换，用起来相当麻烦。XMemcached提供了CASOperation封装了这两个操作，提供给用户统一的使用接口，并且允许设置最多重试次数。详见下面的例子，启动N个线程原子更新同一个key的value。

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
			return currentValue + 1; // 当前值+1
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