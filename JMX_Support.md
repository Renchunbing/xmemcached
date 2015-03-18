# Introduction #

Enable JMX supports,default is false
```
 java -Dxmemcached.jmx.enable=true -Dxmemcached.rmi.port=7077 -Dxmemcached.rmi.name=xmemcachedServer 
```
Access MBean through
```
service:jmx:rmi:///jndi/rmi://[host]:7077/xmemcachedServer 
```

You can add or remove memcached server dynamically and monitor XmemcachedClient's behavior through MBeans.