

# Introduction #
XMemcached supports a easy way to integrate to spring framework since version 1.1.2

# Simple Config #
```
   <bean name="memcachedClient"
		class="net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean" destroy-method="shutdown">
		<property name="servers">
			<value>host1:port1 host2:port2</value>
		</property>
   </bean>
```
Then,you can use memcachedClient in other beans.

# Completed Config #

```
  <bean name="memcachedClient"
		class="net.rubyeye.xmemcached.utils.XMemcachedClientFactoryBean" destroy-method="shutdown">
		<property name="servers">
			<value>host1:port1 host2:port2 host3:port3</value>
		</property>
                <!-- server's weights -->
		<property name="weights">
			<list>
				<value>1</value>
				<value>2</value>
				<value>3</value>
			</list>
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
	</bean>
```

# Spring 3.0 and Builder config #

If you want to use spring 3.0 with xmemcached,there is a exception like "Couldn't find a destroy method named 'shutdown' on bean", it occured beacause spring ioc container has changed the way to process when the destroy method is not found.So you can't use above configuration to intergrating to spring 3.0. Another choice is upgrading your spring version to newer one.

In fact,xmemcached has another way to intergrate to springframework through XMemcachedClientBuilder by factory-method.Here is an example:

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

The server list argument setting is so ugly,so in xmemcached 1.4.0 ,we provide a new constructor that accepts a string server list:
```
<constructor-arg value="host1:port1,host2:port2"/>
```

But it can't set server weights.

# Integrate with spring cache framework #
http://zj0121.iteye.com/blog/1852270