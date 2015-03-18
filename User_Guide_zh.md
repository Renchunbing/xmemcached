

# 变更历史 #

2010-06-22  添加客户端分布和SASL验证两节，更新spring配置一节。
2010-06-23  添加maven依赖说明
2010-10-17  1.2.6 released
2011-01-04  1.3 released。添加failure模式和standby节点。


# XMemcached简介 #

XMemcached是一个新java memcached client。也许你还不知道memcached是什么？可以先看看[这里](http://code.google.com/p/memcached/)。简单来说，Memcached 是一个高性能的分布式内存对象的key-value缓存系统，用于动态Web应用以减轻数据库负载，现在也有很多人将它作为内存式数据库在使用，memcached通过它的自定义协议与客户端交互，而XMemcached就是它的一个java客户端实现。

Memcached的java客户端已经存在两个了：官方提供的基于传统阻塞io由[Greg Whalin维护的客户端](http://www.whalin.com/memcached/)、Dustin Sallings实现的基于java nio的[Spymemcached](http://code.google.com/p/spymemcached)。另外还有一些在此基础上的改进版本。相比于这些客户端，XMemcached有什么优点呢？或者说，它的主要特性有哪些？


# XMemcached的主要特性 #

## 高性能 ##
XMemcached同样是基于java nio的客户端，java nio相比于传统阻塞io模型来说，有效率高（特别在高并发下）和资源耗费相对较少的优点。传统阻塞IO为了提高效率，需要创建一定数量的连接形成连接池，而nio仅需要一个连接即可（当然,nio也是可以做池化处理），相对来说减少了线程创建和切换的开销，这一点在高并发下特别明显。因此XMemcached与Spymemcached在性能都非常优秀，在某些方面（存储的数据比较小的情况下）Xmemcached比Spymemcached的表现更为优秀，具体可以看这个[Java Memcached Clients Benchmark](http://xmemcached.googlecode.com/svn/trunk/benchmark/benchmark.html)。

## 支持完整的协议 ##
Xmemcached支持所有的memcached协议，包括1.4.0正式开始使用的[二进制协议](http://code.google.com/p/memcached/wiki/MemcacheBinaryProtocol)。

## 支持客户端分布 ##
Memcached的分布只能通过客户端来实现，XMemcached实现了此功能，并且提供了一致性哈希(consistent hash)算法的实现。

## 允许设置节点权重 ##

XMemcached允许通过设置节点的权重来调节memcached的负载，设置的权重越高，该memcached节点存储的数据将越多，所承受的负载越大。

## 动态增删节点 ##
XMemcached允许通过JMX或者代码编程实现节点的动态添加或者移除，方便用户扩展和替换节点等。

## 支持JMX ##

XMemcached通过JMX暴露的一些接口，支持client本身的监控和调整，允许动态设置调优参数、查看统计数据、动态增删节点等。

## 与Spring框架和Hibernate-memcached的集成 ##

鉴于很多项目已经使用Spring作为IOC容器，因此XMemcached也提供了对Spring框架的集成支持。[Hibernate-memcached](http://code.google.com/p/hibernate-memcached/)是一个允许将memcached作为hibernate的二级缓存的开源项目，默认是使用Spymemcached，Xmemcached提供了对这个项目的支持，允许替换Spymemcached.

## 客户端连接池 ##

刚才已经提到java nio通常对一个memcached节点使用一个连接，而XMemcached同样提供了设置连接池的功能，对同一个memcached可以创建N个连接组成连接池来提高客户端在高并发环境下的表现，而这一切对使用者来说却是透明的。启用连接池的前提条件是保证数据之间的独立性或者数据更新的同步，对同一个节点的各个连接之间是没有做更新同步的，因此应用需要保证数据之间是相互独立的或者全部采用CAS更新来保证原子性。

## 可扩展性 ##

XMemcached是基于java nio框架[yanf4j](http://code.google.com/p/yanf4j/)实现的，因此在实现上结构相对清楚，分层比较明晰，在xmemcached 1.2.5之后已经将yanf4j合并到xmemcached，因此不再需要依赖yanf4j，下面是XMemcached的主要类的UML图:

![http://www.blogjava.net/images/blogjava_net/killme2008/main.cld.jpg](http://www.blogjava.net/images/blogjava_net/killme2008/main.cld.jpg)

# 使用指南 #

在简单介绍完XMemcached的主要特性之后，我们将进入XMemcached的使用环节，这里将按照从简单到复杂的顺序讲解一些例子，以方便用户深入了解XMemcached的使用。
## 依赖包 ##

Xmemcached依赖[slf4j](http://www.slf4j.org/)

在测试下面讲到的代码之前，请先自行下载依赖包或者下载包含了依赖包的xmemcached。

## 如果你使用maven ##

如果你使用maven构建你的项目，那么只要添加dependency即可使用xmemcached（仅对1.2.5及以后版本有效）
```
 <dependency>
       <groupId>com.googlecode.xmemcached</groupId>
       <artifactId>xmemcached</artifactId>
       <version>{版本号}</version>
  </dependency>
```

## 简单例子 ##

对于用户来说，最主要的功能是存取数据，假设我们有一个memcached节点IP地址或者域名是host，端口是11211，一个简单的存取数据的例子如下：
```
    MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses("localhost:11211"));
    MemcachedClient memcachedClient = builder.build();
    try {
		memcachedClient.set("hello", 0, "Hello,xmemcached");
		String value = memcachedClient.get("hello");
		System.out.println("hello=" + value);
		memcachedClient.delete("hello");
		value = memcachedClient.get("hello");
		System.out.println("hello=" + value);
    } catch (MemcachedException e) {
		System.err.println("MemcachedClient operation fail");
		e.printStackTrace();
    } catch (TimeoutException e) {
		System.err.println("MemcachedClient operation timeout");
		e.printStackTrace();
    } catch (InterruptedException e) {
		// ignore
    }
    try {
              //close memcached client
      		memcachedClient.shutdown();
    } catch (IOException e) {
        	System.err.println("Shutdown MemcachedClient fail");
		e.printStackTrace();
    }

```
为了节省篇幅，本文的所有代码示例都没有给出完整的package名，具体包名请查询javadoc或者使用IDE工具帮助引入。

因为XMemcachedClient的创建有比较多的可选项，因此提供了一个XMemcachedClientBuilder类用于构建MemcachedClient。MemcachedClient是主要接口，操作memcached的主要方法都在这个接口，XMemcachedClient是它的一个实现。传入的memcached节点列表要求是类似**"host1:port1 host2:port2 …"**这样的字符串，通过AddrUtil.getAddresses方法获取实际的IP地址列表。

存储数据是通过set方法，它有三个参数，第一个是存储的key名称，第二个是expire时间（单位秒），超过这个时间,memcached将这个数据替换出去，0表示永久存储（默认是一个月），第三个参数就是实际存储的数据，可以是任意的java可序列化类型。获取存储的数据是通过get方法，传入key名称即可。如果要删除存储的数据，这是通过delete方法，它也是接受key名称作为参数。XMemcached由于是基于nio，因此通讯过程本身是异步的，client发送一个请求给memcached，你是无法确定memcached什么时候返回这个应答，客户端此时只有等待，因此还有个等待超时的概念在这里。客户端在发送请求后，开始等待应答，如果超过一定时间就认为操作失败，这个等待时间默认是5秒(1.3.8开始改为5秒，之前是1秒），上面例子展现的3个方法调用的都是默认的超时时间，这三个方法同样有允许传入超时时间的重载方法，例如
```
   value=client.get(“hello”,3000);
```

就是等待3秒超时，如果3秒超时就跑出TimeutException，用户需要自己处理这个异常。因为等待是通过调用CountDownLatch.await(timeout)方法，因此用户还需要处理中断异常InterruptException。最后的MemcachedException表示Xmemcached内部发生的异常，如解码编码错误、网络断开等等异常情况。

## touch更新数据超时时间 ##

经常有这样的需求，就是希望更新缓存数据的超时时间(expire time)，在没有touch协议之前，你需要整体的get-set一次：
```
    value=client.get("a");
    client.set("a",new-expire-time,value);
```

两次操作，加上value的序列化/反序列化、网络传输，这个开销可不小。幸好，现在memcached已经支持touch协议，只需要传递key就更新缓存的超时时间：
```
    client.touch(key,new-expire-time);
```

xmemcached 1.3.6开始支持二进制协议的touch命令，1.3.8开始支持文本协议的touch命令。

有时候你希望获取缓存数据并更新超时时间，这时候可以用getAndTouch方法（仅二进制协议支持）：
```
   client.getAndTouch(key,new-expire-time);
```


## 客户端分布 ##

Memcached的分布是通过客户端实现的，客户端根据key的哈希值得到将要存储的memcached节点，并将对应的value存储到相应的节点。

XMemcached同样支持客户端的分布策略，默认分布的策略是按照key的哈希值模以连接数得到的余数，对应的连接就是将要存储的节点。如果使用默认的分布策略，你不需要做任何配置或者编程。

XMemcached同样支持[一致性哈希](http://en.wikipedia.org/wiki/Consistent_hashing)（consistent hash)，通过编程设置：
```
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil
				.getAddresses(properties.getProperty("test.memcached.servers"))
        builder.setSessionLocator(new KetamaMemcachedSessionLocator());
        MemcachedClient client=builder.build();
```

配置的方式请见spring配置一节。

XMemcached还提供了额外的一种哈希算法——选举散列,在某些场景下可以替代一致性哈希
```
  MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                                        AddrUtil.getAddresses("server1:11211 server2:11211 server3:11211"));
  builder.setSessionLocator(new ElectionMemcachedSessionLocator());
  MemcachedClient mc = builder.build();
```

## CAS操作 ##

Memcached是通过cas协议实现原子更新，所谓原子更新就是compare and set，原理类似乐观锁，每次请求存储某个数据同时要附带一个cas值，memcached比对这个cas值与当前存储数据的cas值是否相等，如果相等就让新的数据覆盖老的数据，如果不相等就认为更新失败，这在并发环境下特别有用。XMemcached提供了对CAS协议的支持（无论是文本协议还是二进制协议），CAS协议其实是分为两个步骤：获取CAS值和尝试更新，因此一个典型的使用场景如下：
```
  GetsResponse<Integer> result = client.gets("a");
  long cas = result.getCas(); 
  //尝试将a的值更新为2
  if (!client.cas("a", 0, 2, cas)) {
	System.err.println("cas error");
   }

```
首先通过gets方法获取一个GetsResponse，此对象包装了存储的数据和cas值，然后通过cas方法尝试原子更新，如果失败打印”cas error”。显然，这样的方式很繁琐，并且如果你想尝试多少次原子更新就需要一个循环来包装这一段代码，因此XMemcached提供了一个\*CASOperation\*接口包装了这部分操作，允许你尝试N次去原子更新某个key存储的数据，无需显式地调用gets获取cas值,上面的代码简化为:
```
 client.cas("a", 0, new CASOperation<Integer>() {
         		public int getMaxTries() {
				return 1;
			}

			public Integer getNewValue(long currentCAS, Integer currentValue) {
					return 2;
			}
	});

```

CASOpertion接口只有两个方法，一个是设置最大尝试次数的getMaxTries方法，这里是尝试一次，如果尝试超过这个次数没有更新成功将抛出一个TimeoutException，如果你想无限尝试（理论上），可以将返回值设定为Integer.MAX\_VALUE；另一个方法是根据当前获得的GetsResponse来决定更新数据的getNewValue方法，如果更新成功，这个方法返回的值将存储成功，这个方法的两个参数是最新一次gets返回的GetsResponse结果。



## 更全面的例子 ##

一些更全面的例子，展现了MemcachedClient接口的主要方法：

```
  MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses(“localhost:12000”));
  MemcachedClient client = builder.build();
			client.flushAll();
  if (!client.set("hello", 0, "world")) {
		System.err.println("set error");
  }
  if (client.add("hello", 0, "dennis")) {
		System.err.println("Add error,key is existed");
  }
  if (!client.replace("hello", 0, "dennis")) {
		System.err.println("replace error");
  }
  client.append("hello", " good");
  client.prepend("hello", "hello ");
  String name = client.get("hello", new StringTranscoder());
  System.out.println(name);
  client.deleteWithNoReply(“hello”);

```

首先存储了hello对应的world字符串，然后调用add和replace方法去尝试添加和替换，因为数据已经存在，因此add会失败，同样replace在数据存在的情况才会成功，也就是将hello对应的数据更新为dennis，然后通过append和prepend方法在dennis前后加上了字符串hello和good，因此通过get返回的结果是hello dennis good。而删除数据则是通过deleteWithNoReply方法，这个方法删除数据并且告诉memcached不用返回应答，因此这个方法不会等待应答直接返回，特别适合于批量处理；同样地，set、add、replace等方法也有相应的withNoReply重载版本，具体请看API文档。

## 迭代所有key ##

Memcached本身并没有提供迭代所有key的方法，但是通过"stats items"和"stats cachedump"统计协议可以做到迭代所有的key，这个迭代过程是低效，因此如无必要，并不推荐使用此方法。XMemcached仅提供了文本协议的迭代支持，其他协议暂未支持。

想迭代所有的key，你只需要获取一个KeyIterator即可：
```
MemcachedClient client=...
KeyIterator it=client.getKeyIterator(AddrUtil.getOneAddress("localhost:11211"));
while(it.hasNext())
{
   String key=it.next();
}
```

## Incr/Decr ##

下面这个例子展现了incr/decr操作的使用，两个操作类似Java中的原子类如AtomicIntger，用于原子递增或者递减变量数值：
```
 assert(1==this.memcachedClient.incr("a", 5, 1));
 assert(6==this.memcachedClient.incr("a", 5));
 assert(10==this.memcachedClient.incr("a", 4));
 assert(9==this.memcachedClient.decr("a", 1));
 assert(7==this.memcachedClient.deccr("a", 2));

```

incr和decr都有三个参数的方法，第一个参数指定递增的key名称，第二个参数指定递增的幅度大小，第三个参数指定当key不存在的情况下的初始值。两个参数的重载方法省略了第三个参数，默认指定为0。

Xmemcached还提供了一个称为计数器的封装，它封装了incr/decr方法，使用它就可以类似AtomicLong那样去操作计数：
```
  Counter counter=client.getCounter("counter",0);
  counter.incrementAndGet();
  counter.decrementAndGet();
  counter.addAndGet(-10);
```

其中getCounter的第二个参数是计数器的初始值。

## 命名空间 ##

从1.4.2开始，xmemcached提供了memcached命名空间的封装使用，你可以将一组缓存项放到同一个命名空间下，可以让整个命名空间下所有的缓存项同时失效，例子：

```
String ns = "namespace" ;
this.memcachedClient.withNamespace(ns,
				new MemcachedClientCallable<Void>() {

					public Void call(MemcachedClient client)
							throws MemcachedException, InterruptedException,
							TimeoutException {
                                                 //a,b,c都在namespace下
						client.set("a",1);
                                                client.set("b",1);
                                                client.set("c",1);
                                                return null;
					}
				});
//获取命名空间内的a对应的值
Integer aValue = this.memcachedClient.withNamespace(ns,
				new MemcachedClientCallable<Integer>() {

					public Integer call(MemcachedClient client)
							throws MemcachedException, InterruptedException,
							TimeoutException {
                                                  return client.get("a");
					}
				});

//使得命名空间失效
this.memcachedClient.invalidateNamespace(ns);
```


## 查看统计信息 ##
Memcached提供了统计协议用于查看统计信息：
```
   Map<InetSocketAddress,Map<String,String>> result=client.getStats();
```

getStats方法返回一个map，其中存储了所有已经连接并且有效的memcached节点返回的统计信息，你也可以统计具体的项目，如统计items项目：
```
   Map<InetSocketAddress,Map<String,String>> result=client.getStatsByItem("items");
```

只要向getStatsByItem传入需要统计的项目名称即可。

## SASL验证 ##

Memcached 1.4.3开始支持SASL验证客户端，在服务器配置启用SASL之后，客户端需要通过授权验证才可以跟memcached继续交互，否则将被拒绝请求。XMemcached 1.2.5开始支持这个特性。假设memcached设置了SASL验证，典型地使用CRAM-MD5或者PLAIN的文本用户名和密码的验证机制，假设用户名为cacheuser，密码为123456，那么编程的方式如下：
```
	MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses("localhost:11211"));
	builder.addAuthInfo(AddrUtil.getOneAddress("localhost:11211"), AuthInfo
					.typical("cacheuser", "123456"));
	// Must use binary protocol
	builder.setCommandFactory(new BinaryCommandFactory());
	MemcachedClient client=builder.build();

```

请注意，授权验证仅支持二进制协议。

如果采用Spring配置，请参见spring配置一节。


## 高级主题 ##

### 与Spring框架集成 ###

通过XMemcachedClientFactoryBean类，即可与spring框架集成，简单的配置如下：

```
<bean name="memcachedClient"
                class="net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean" destroy-method="shutdown">
                <property name="servers">
                        <value>host1:port host2:port2</value>
                </property>
   </bean>
```

那么你就可以在需要使用MemcachedClient的地方引用这个bean.

更完整的配置例子，设置备份节点、协议类型、一致性哈希、权重、连接池大小甚至SASL验证信息(xmemcached 1.2.5支持)，具体请看注释：
```
<bean name="server1" class="java.net.InetSocketAddress">
		<constructor-arg>
			<value>host1</value>
		</constructor-arg>
		<constructor-arg>
			<value>port1</value>
		</constructor-arg>
</bean>
<bean name="memcachedClient"
                class="net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean" destroy-method="shutdown">
                <property name="servers">
                        <value>host1:port,host2:port host3:port,host4:port</value>
                </property>
                <!-- server's weights -->
                <property name="weights">
                        <list>
                                <value>1</value>
                                <value>2</value>
                                <value>3</value>
                        </list>
                </property>
                <!-- AuthInfo map,only valid on 1.2.5 or later version -->
		<property name="authInfoMap">
			<map>
			        <entry key-ref="server1">
					<bean class="net.rubyeye.xmemcached.auth.AuthInfo"
						factory-method="typical">
						<constructor-arg index="0">
							<value>cacheuser</value>
						</constructor-arg>
						<constructor-arg index="1">
							<value>123456</value>
						</constructor-arg>
					</bean>
				</entry>
			</map>
		</property>
                <!-- nio connection pool size -->
                <property name="connectionPoolSize" value="2"></property>
                 <!-- Use binary protocol,default is TextCommandFactory -->
                <property name="commandFactory">
                   <bean class="net.rubyeye.xmemcached.command.BinaryCommandFactory"></bean>
                </property>
                <!-- Distributed strategy -->
                <property name="sessionLocator">
                        <bean class="net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator"></bean>
                </property>
                <!-- Serializing transcoder -->
                <property name="transcoder">
                        <bean class="net.rubyeye.xmemcached.transcoders.SerializingTranscoder" />
                </property>
                 <!-- ByteBuffer allocator -->
                <property name="bufferAllocator">
                        <bean class="net.rubyeye.xmemcached.buffer.SimpleBufferAllocator"></bean>
                </property>
               <!-- Failure mode -->
               <property name="failureMode" value="false"/>
        </bean>
```

配置选项参数表:

| 属性名 | 值 |
|:----------|:----|
| servers | memcached节点列表，形如“主节点1:port,备份节点1:port   主节点2:port,备份节点2:port“的字符串，可以不设置备份节点，主备节点逗号隔开，不同分组空格隔开。 |
|weights |与servers对应的节点的权重 |
| authInfoMap | 授权验证信息，仅在xmemcached 1.2.5及以上版本有效 |
| connectionPoolSize | nio连接池大小，默认为1  |
|commandFactory |协议工厂，net.rubyeye.xmemcached.command.BinaryCommandFactory,TextCommandFactory(默认),KestrelCommandFactory |
| sessionLocator | 分布策略，一致性哈希net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator或者ArraySessionLocator(默认) |

|transcoder | 序列化转换器，默认使用net.rubyeye.xmemcached.transcoders.SerializingTranscoder，更多选项参见javadoc |
|:----------|:----------------------------------------------------------------------------------------------------------------------|
| bufferAllocator | IoBuffer分配器，默认为net.rubyeye.xmemcached.buffer.SimpleBufferAllocator，可选CachedBufferAllocator(不推荐) |
| failureMode   | 是否启用failure模式，true为启用，默认不启用 |

#### Spring 3.0和Builder配置 ####

Spring 3.0修改了查找destroy method的方式，因此如果还是采用上面的配置来集成xmemcached话，会在启动的时候抛出一个异常，信息类似“Couldn't find a destroy method named 'shutdown' on bean”，这种情况下xmemcached就无法正常工作，spring的IOC容器也无法正常启动。有没有解决办法呢？答案是有的，暂时可以通过XmemcachedClientBuilder的工厂方法方式来创建MemcachedClient，也就是通过factory-bean加上factory-method指定的方式，一个示范配置如下:
```
   <bean name="memcachedClientBuilder" class="net.rubyeye.xmemcached.XMemcachedClientBuilder">
                <!-- XMemcachedClientBuilder have two arguments.First is server list,and second is weights array. -->
                <constructor-arg>
                        <list>
                                <bean class="java.net.InetSocketAddress">
                                        <constructor-arg>
                                                <value>localhost</value>
                                        </constructor-arg>
                                        <constructor-arg>
                                                <value>12000</value>
                                        </constructor-arg>
                                </bean>
                                <bean class="java.net.InetSocketAddress">
                                        <constructor-arg>
                                                <value>localhost</value>
                                        </constructor-arg>
                                        <constructor-arg>
                                                <value>12001</value>
                                        </constructor-arg>
                                </bean>
                        </list>
                </constructor-arg>
                <constructor-arg>
                        <list>
                                <value>1</value>
                                <value>2</value>
                        </list>
                </constructor-arg>
                <property name="authInfoMap">
			<map>
				<entry key-ref="server1">
					<bean class="net.rubyeye.xmemcached.auth.AuthInfo"
						factory-method="typical">
						<constructor-arg index="0">
							<value>cacheuser</value>
						</constructor-arg>
						<constructor-arg index="1">
							<value>123456</value>
						</constructor-arg>
					</bean>
				</entry>
			</map>
		</property>
                <property name="connectionPoolSize" value="2"></property>
                <property name="commandFactory">
                        <bean class="net.rubyeye.xmemcached.command.TextCommandFactory"></bean>
                </property>
                <property name="sessionLocator">
                        <bean class="net.rubyeye.xmemcached.impl.KetamaMemcachedSessionLocator"></bean>
                </property>
                <property name="transcoder">
                        <bean class="net.rubyeye.xmemcached.transcoders.SerializingTranscoder" />
                </property>
        </bean>
        <!-- Use factory bean to build memcached client -->
        <bean name="memcachedClient3" factory-bean="memcachedClientBuilder"
                factory-method="build" destroy-method="shutdown"/>
```

### 设置节点权重 ###

如果是通过spring配置，请看上一节，如果需要编程设置，通过下面代码：
```
 MemcachedClientBuilder builder = new     
XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   MemcachedClient memcachedClient=builder.build();
```

传入一个int数组，里面的元素就是节点对应的权重值，比如这里设置"localhost:12000"节点的权重为1，而"localhost:12001"的权重为3。注意，xmemcached的权重是通过复制连接的多个引用来实现的，比如权重为3，那么就复制3个同一个连接的引用放在集合中让MemcachedSessionLocator查找。

改变节点权重，可以通过setServerWeight方法：
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

### 使用二进制协议 ###

如果使用spring配置，请参见与spring集成一节。

Memcached 1.4开始正式启用二进制协议，xmemcached 1.2开始支持二进制协议，启用这一特性也非常简单，设置相应的CommandFactory即可：
```
MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setCommandFactory(new BinaryCommandFactory());//use binary protocol 
   MemcachedClient memcachedClient=builder.build();
```

默认使用的TextCommandFactory，也就是文本协议。

### JMX支持 ###

可以通过JMX查看xmemcached的状态，在jvm启动参数中添加：
```
 java -Dxmemcached.jmx.enable=true
```
即可通过JMX监控xmemcached状态，xmemcached通过RMI暴露服务接口：
```
 service:jmx:rmi:///jndi/rmi://[host]:7077/xmemcachedServer 
```
你可以在jconsole中查看这些MBean。
提供的MBean包括:
| MBean | 描述 |
|:------|:-------|
|net.rubyeye.xmemcached.monitor.StatisticsHandlerMBean | 用于查看Client统计信息 |
|net.rubyeye.xmemcached.impl.OptimizerMBean | 用于调整性能参数 |
|net.rubyeye.xmemcached.XMemcachedClientMBean |动态添加或者删除节点，查看有效服务器等信息|

JMX的更多选项:
| 选项 | 描述|
|:-------|:------|
|-Dxmemcached.rmi.port | RMI端口 |
|-Dxmemcached.rmi.name | RMI服务名 |

### 动态添加/删除节点 ###

在JMX支持一节提到的JMX方式操作外，还可以通过编程方式：
```
    MemcachedClient client=new XMemcachedClient(AddrUtil.getAddresses("server1:11211 server2:11211"));
   //Add two new memcached nodes
   client.addServer("server3:11211 server4:11211");
   //Remove memcached servers
   client.removeServer("server1:11211 server2:11211");
```


### Nio连接池 ###
Xmemcached是基于java nio的client实现，默认对一个memcached节点只有一个连接，这在通常情况下已经有非常优异的表现。但是在典型的高并发环境下,nio的单连接也会遇到性能瓶颈。因此XMemcached支持设置nio的连接池，允许建立多个连接到同一个memcached节点，但是请注意，这些连接之间是不同步的，因此你的应用需要自己保证数据更新的同步，启用连接池可以通过下面代码：
```
  MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000"));

  builder.setConnectionPoolSize(5);
```

如果采用Spring配置，请参见与Spring集成一节。

### Failure模式和standby节点 ###

从1.3版本开始，xmemcached支持failure模式。所谓failure模式是指，当一个memcached节点down掉的时候，发往这个节点的请求将直接失败，而不是发送给下一个有效的memcached节点。具体可以看memcached的文档。默认不启用failure模式，启用failure模式可以通过下列代码：
```
   MemcachedClientBuilder builder=……
   builder.setFailureMode(true);
```

不仅如此，xmemcached还支持主辅模式，你可以设置一个memcached的节点的备份节点，当主节点down掉的情况下，会将本来应该发往主节点的请求转发给standby备份节点。使用备份节点的前提是启用failure模式。备份节点设置如下：
```
   MemcachedClient builder=new XmemcachedClientBuilder(AddrUtil.getAddressMap("localhost:11211,localhost:11212 host2:11211,host2:11212"));
```

上面的例子，将localhost:11211的备份节点设置为localhost:11212,而将host2:11211的备份节点设置为host2:11212

形如“host:port,host:port"的字符串也可以使用在spring配置中，完全兼容1.3之前的格式。


### 与Kestrel交互 ###

Kestrel是twitter开源的一个scala写的简单高效MQ，它支持 memcached文本协议，但是并不完全兼容，例如它不支持flag，导致很多利用flag做序列化的客户端无法正常运作。因此Xmemcached特意提供了KestrelCommandFactory?用于支持Kestrel。使用KestrelCommandFactory?即可拥有如下好处：

**默认关闭get优化，因为kestrel不支持bulk get；**

**支持kestrel的阻塞获取和可靠获取；**

**允许向kestrel存储任意java序列化类型。设置KestrelCommandFactory:**

```
 MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setCommandFactory(new KestrelCommandFactory());
   MemcachedClient memcachedClient=builder.build();
```

关于最后一点需要补充说明，由于kestrel不支持flag，因此xmemcached在存储的数据之前加了4个字节的flag，如果你的全部应用都使用xmemcached，那么没有问题，如果使用其他clients，会有兼容性的问题，因此Xmemcached还允许关闭这个功能，通过
```
client.setPrimitiveAsString(true);
```

设置为true后，原生类型都将存储为字符串，而序列化类型将无法存储了。

### 与tokyotyrant交互 ###

通过使用TokyoTyrantTranscoder就可以跟TokyoTyrant进行交互，但是由于TokyoTyrant对memcached文本协议的flag,exptime不支持，因此内部TokyoTyrantTranscoder加了4个字节的flag在value前面，如果你的全部应用都使用xmemcached，那么没有问题，如果使用其他clients，会有兼容性的问题，这一点与跟kestrel交互相同。

```
 MemcachedClientBuilder builder = new    XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:12000 localhost:12001"),new int[]{1,3});
   builder.setTranscoder(new TokyoTyrantTranscoder());
   MemcachedClient memcachedClient=builder.build();
```

### 与Hibernate-memcached集成 ###

大多数配置与采用spymemcahed一样，具体请看hibernate-memcached的wiki页。如果使用
xmemcached，首先需要将memcacheClientFactory 设置为XmemcachedClientFactory。

| **Property** | **Value** |
|:-------------|:----------|
|hibernate.memcached.memcacheClientFactory | net.rubyeye.xmemcached.utils.hibernate.XmemcachedClientFactory |


其他一般选项有很大不同：
| **Property** | **Value** |
|:-------------|:----------|
|hibernate.memcached.servers | localhost:11211 localhost:11212|
|ibernate.memcached.cacheTimeSeconds | 300|
|hibernate.memcached.keyStrategy    | HashCodeKeyStrategy |
|hibernate.memcached.readBufferSize |DEFAULT\_SESSION\_READ\_BUFF\_SIZE |
|hibernate.memcached.operationTimeout |DEFAULT\_OP\_TIMEOUT |
|hibernate.memcached.hashAlgorithm  | NATIVE\_HASH,KETAMA\_HASH etc.|
|hibernate.memcached.commandFactory | TextCommandFactory , BinaryCommandFactory |
|hiberante.memcached.sessionLocator | ArrayMemcachedSessionLocator,KetamaMemcachedSessionLocator|

### 压缩、sanitizeKeys等杂项 ###

#### 数据压缩 ####

memcached存储大数据的效率是比较低的，当数据比较大的时候xmemcached会帮你压缩在存储，取出来的时候自动解压并反序列化，这个大小阀值默认是16K，可以通过Transcoder接口的setCompressionThreshold(1.2.1引入)方法修改阀值，比如设置为1K：
```
memcachedClient.getTranscoder()).setCompressionThreshold(1024);
```

这个方法是在1.2.1引入到Transcoder接口，在此之前，你需要通过强制转换来设置：
```
 ((SerializingTranscoder)memcachedClient.getTranscoder()).setCompressionThreshold(1024);
```

#### packZeros ####

XMemcached的序列化转换器在序列化数值类型的时候有个特殊处理，如果前面N个字节都是0，那么将会去除这些0，缩减后的数据将更小，例如数字3序列化是0x0003，那么前面3个0将去除掉成一个字节0x3。反序列化的时候将自动在前面根据数值类型补0。这一特性默认是开启的，如果考虑到与其他client兼容的话需要关闭此特性可以通过：
```
memcachedClient.getTranscoder()).setPackZeros(false);
```

#### sanitizeKeys ####

在官方客户端有提供一个sanitizeKeys选项，当选择用URL当key的时候，MemcachedClient会自动将URL encode再存储。默认是关闭的，想启用可以通过：
```
 memcachedClient.setSanitizeKeys(true);
```
这一特性将在1.2.1中引入。