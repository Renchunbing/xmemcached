
#summary Frequently Asked Questions about Xmemcached.

# Introduction #

Frequently Asked Questions about Xmemcached


# Details #

## What is memcached ##
Memcached is a high-performance, distributed memory object caching system, generic in nature, but intended for use in speeding up dynamic web applications by alleviating database load.More infomation please visit it's homepage  http://memcached.org/

## What is xmemcached? ##

XMemcached is a high performance, easy to use blocking multithreaded memcached client in java.It's nio based (using my opensource nio framework :[yanf4j](http://code.google.com/p/yanf4j/)), and was carefully tuned to get top performance.

## Why another java memcached client? ##
Xmemcached has some special features that other java memcached clients doen's have,including:
  * JMX monitor,user can add/remove server dynamically
  * Client statistics
  * Weighting Server
  * Nio connection pool
  * Talking with kestrel and TokyoTyrant.
  * High performance

## Is Xmemcached compatible with jdk5? ##
Yes,since 1.2.0-RC1,Xmemcached is compatible with jdk5,but not 1.4.

## How to build xmemcached by myself? ##
First,your system must install maven,xmemcached use mavn to build itsefl.Then,download the source code,and unpack it to a fold,then enter the fold and type command 'mvn -Dtest -DfailIfNoTests=false package' to build the project.

## How to run unit tests? ##

The test.properties file under the src/test/resources folder is used for setting memcached test server.
Please set test.memcached.servers property value,Then run the unit test with command 'mvn test'

## Why throw java.util.concurrent.timeoutException ? ##

Xmemcached is java nio based,so so all requests are asynchronous.The user thread must wait the response from memcached server.When time is over the operation timeout vlaue,then xmemcached would throw java.utill.TimeoutExcetipn.The default operation timeout is one second,and all methods have overload methods that have a parameter named 'optimeout' to override the default value.

Also,you can change the default operation timeout vlaue by
```
   memcachedClient.setOpTimeout(5000L); //change to five seconds
```
The unit is milliseconds.

## How to store more than 1MB data to memcached? ##

Since memcached 1.4.2, it allow user to store more than 1MB size data.Xmemcached only allow use storing maximum 1MB of data by default,if you want to store more than that,please use custom transcoder:
```
  //set maximum to 10MB
  memcachedClient.setTranscoder(new SerializingTranscoder(10*1024*1024));
```

## How to listen memcached client state? ##

If you want to be notified when memcached client was startup,shutdown,connecting a memcached server or disconnecting,you could implement [http://xmemcached.googlecode.com/svn/trunk/apidocs/net/rubyeye/xmemcached/MemcachedClientStateListener.html](MemcachedClientStateListener.md) interface and add a instance to memcached client:
```
class MyListener implements MemcachedClientStateListener {

	public void onConnected(MemcachedClient memcachedClient,
			InetSocketAddress inetSocketAddress) {
		System.out.println("Connect to " + inetSocketAddress);
	}

	public void onDisconnected(MemcachedClient memcachedClient,
			InetSocketAddress inetSocketAddress) {
		System.out.println("Disconnect from " + inetSocketAddress);

	}

	public void onException(MemcachedClient memcachedClient, Throwable throwable) {
		throwable.printStackTrace();

	}

	public void onShutDown(MemcachedClient memcachedClient) {
		System.out.println("MemcachedClient has been shutdown");

	}

	public void onStarted(MemcachedClient memcachedClient) {
		System.out.println("MemcachedClient has been started");

	}

}

                        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses(servers));
			// Add my state listener
			builder.addStateListener(new MyListener());
                        MemcachedClient client=builder.build();

```

But you should not call any operations that would block in these callback methods.

## Set items,but can not get items from other machines when using consist hash,why? ##

You set items with some keys in one machine,then try to get items by these keys from other machines using the same xmemcached client configuration,but some keys' value are still null.Why?

In my experience,this is beacause your machines's hostnames are not configure correctly,so the consit hash session locator could not build the sessions finding map correctly,because it use session's remote address toString() method,and it would try to resolve the hostname by default.
If your machines hostnames are not resolved in the same result on different machines,the finding map will be different,and then xmemcached will not find the same session with the same key.

To solve this problem,you should try configure your machines hostnames correctly.Another solution is using consist hash  to be compatible with nginx-upstream-consistent:
```
new KetamaMemcachedSessionLocator(true);
```
And it will use machines raw ip insteadof hostname to build finding map.

中文FAQ和性能调整建议 http://www.blogjava.net/killme2008/archive/2010/07/08/325564.html