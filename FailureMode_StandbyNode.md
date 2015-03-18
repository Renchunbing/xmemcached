# Failure Mode #
Xmemcached 1.3 introduce failure mode for client.When client is in failure mode,if a memcached server was down,then further requests to that server will throw an exception until the server comes back,they did not transform to next available memcached server.Configure failre mode:

```
  MemcachedClientBuilder builder=……
  builder.setFailureMode(true);
```




# Standby Node #

When you configure client in failure mode,you could also set a standby memcached node for every memcached server,and if a memcached server was down,then further requests to that server will transform to the standby node until server comes back.Configure a standby node by
```
 MemcachedClient builder=new XmemcachedClientBuilder(AddrUtil.getAddressMap("localhost:11211,localhost:11212 host2:11211,host2:11212"));
```

Here,we configure localhost:11212 to be a standby node for localhost:11211,and host2:11212 to be a standby node for host2:11211.

The server map string is in the form of "mainHost1:port,standbyHost1:port mainHost2:port,standbyHost2:port".It also could be used in spring configuration.