
```
  MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses("server1:11211 server2:11211 server3:11211"));
  MemcachedClient mc = builder.build();

```

  * Consistent  Hash(一致性哈希)

```
  MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses("server1:11211 server2:11211 server3:11211"));
  builder.setSessionLocator(new KetamaMemcachedSessionLocator());
  MemcachedClient mc = builder.build();

```

  * Election Hash(选举散列)
```
   MemcachedClientBuilder builder = new XMemcachedClientBuilder(
					AddrUtil.getAddresses("server1:11211 server2:11211 server3:11211"));
  builder.setSessionLocator(new ElectionMemcachedSessionLocator());
  MemcachedClient mc = builder.build();
```