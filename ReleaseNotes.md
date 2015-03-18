# Introduction #
Release Notes.


# Details #
## Xmemcached 2.0.0 release note ##

  1. Performance tweaks,10% improved for text protocol in my benchmark.
  1. Fixed getStats could not work with 'cache dump',thanks to machao9email.
  1. Fixed deak lock when shutdown client.
  1. Fixed missing unsigned conversion on ONE\_AT\_A\_TIME hash, thanks to spudone.
  1. Cached decoded object for the same key in a batch get,reduce CPU consumption.
  1. Used System.nanoTime instead of currentTimeInMills to generate namespace timestamp.

## Xmemcached 1.4.3 release notes ##

  1. Added 'setConnectTimeout' for XMemcachedClientFactoryBean.Thanks to nick verbeck.
  1. Added environment variable 'xmemcached.heartbeat.max\_threads' to sizing heartbeat thread pool size,it's CPUs count number by default.
  1. Configure heartbeat thread pool,such as daemon,priority etc.Thanks to  profondometer.
  1. Catch all exceptions when removing shutdown hook,make it quiet.
  1. Fixed batch get could not work with namespace.
  1. Fixed NPE when destroying memcached connection.

## Xmemcached 1.4.2 release notes ##

  1. Upgrade slf4j to 1.7.5
  1. Impl namespace in memcached.
  1. Daemonizing reactor threads.
  1. Supports delete item with CAS value that was gets before.
  1. Remove final modifier on TextCommandFactory
  1. Improve KetamaMemcachedSessionLocator using socket address string in a consistent way.
  1. Fixed issues:  setEnableHeartBeat could not work ; Trim server addresses before using them ; delete method cloud not work with kestrel latest version etc.

## Xmemcached 1.4.1 release notes ##
  1. Fixed some log errors
  1. Fixed could not find class when deserializing in some web containers.

## Xmemcached 1.4.0 release notes ##

  1. Added `setOpTimeout` for client builder.
  1. Monitor timeout request in exception message.
  1. Added `RandomMemcachedSessionLocaltor` for kestrel protocol
  1. When timeout exception happens in a connection continuously,we will close it,then it will be tried to reconnect.The maximum number of timeout exception can be set by `setTimeoutExceptionThreshold` method in client,default value is 1000.
  1. Added a new constructor for client builder that accepts a string server list.Thanks @bmahe.

## Xmemcached 1.3.9 release notes ##

  1. Added `resetStats` method for statistics MBean to reset statstics.
  1. Added `setEnableHealSession` method to client/builder to enable/disable healing session when disconnected.
  1. Optimize SET operations for binary protocol,36% performance increased in test.
  1. Fixed incr could not work with custom `KeyProvider`.
  1. Fixed client statistics error.
  1. Reduce memory consumption on high load.
  1. Added a new environment variable `xmemcached.selector.pool.size` to set reactor pool size.
  1. Limit key length to 250 both in text and binary protocol.

## Xmemcached 1.3.8 release notes ##
  1. Implemented touch command for text protocol.
  1. Added a new interface `KeyProvider` to pre-process keys before sending them to memcached.And added a new method `setKeyProvider` to client builder and spring factory bean.
  1. Added flow control for noreply operations and added a new method `setMaxQueuedNoReplyOperations` to to client builder and spring factory bean.It's default value is based on your jvm maximum memory.
  1. Removed `time` field in delete command,it's not valid at all even you pass it.
  1. Changed default operation timeout to five seconds.
  1. Fixed KetamaMemcachedSessionLocator issue when set it to be compatible with nginx upstream consistent hash using memcached default port.Thanks wolfg1969.

## Xmemcached 1.3.7 release notes ##

  1. Send heartbeat when connection is in read idle instead of sending it periodically.
  1. Added an environment variable `xmemcached.heartbeat.max.fail.times` to set max heartbeat failure times,default is 3 times.


## Xmemcached 1.3.6 release notes ##

  1. Fixed issues,includes:[issue 161](https://code.google.com/p/xmemcached/issues/detail?id=161),[issue 163](https://code.google.com/p/xmemcached/issues/detail?id=163),[issue 165](https://code.google.com/p/xmemcached/issues/detail?id=165),[issue 169](https://code.google.com/p/xmemcached/issues/detail?id=169),[issue 172](https://code.google.com/p/xmemcached/issues/detail?id=172),[issue 173](https://code.google.com/p/xmemcached/issues/detail?id=173),[issue 176](https://code.google.com/p/xmemcached/issues/detail?id=176),[issue 179](https://code.google.com/p/xmemcached/issues/detail?id=179),[issue 180](https://code.google.com/p/xmemcached/issues/detail?id=180)
  1. Adds new methods for MemcachedClientBuilder:setConnectTimeout and setSanitizeKeys
  1. Make heartbeats as an independent task,not just occur when connections are idle.
  1. Disable nagle algorithm by default.
  1. Decrease default merge factor to 50.
  1. Adds CompressionMode for Transcoder,default is ZIP,but you can choose GZIP mode.

## Xmemcached 1.3.5 release notes ##

  1. Fixed [issue 154](https://code.google.com/p/xmemcached/issues/detail?id=154),[issue 155](https://code.google.com/p/xmemcached/issues/detail?id=155)
  1. Thanks ilkinulas and MrRubato.

## Xmemcached 1.3.4 release notes ##
  1. Enable nagle algorithm by default.
  1. Check result returned by inc/dec protocols is number.
  1. Make opTimeout can be configured by XMemcachedClientFactoryBean
  1. Added RoundRobinMemcachedSessionLocator for kestrel or memcacheq cluster
  1. Fixed bug which could cause connection disconnect when decode binary command with error message from memcached.
  1. Add a option in KetamaMemcachedSessionLocator to be be compatible with nginx-upstream-consistent.
  1. Fixed bugs,include [issue 132](https://code.google.com/p/xmemcached/issues/detail?id=132),[issue 142](https://code.google.com/p/xmemcached/issues/detail?id=142),[issue 133](https://code.google.com/p/xmemcached/issues/detail?id=133),[issue 139](https://code.google.com/p/xmemcached/issues/detail?id=139),[issue 142](https://code.google.com/p/xmemcached/issues/detail?id=142),[issue 145](https://code.google.com/p/xmemcached/issues/detail?id=145),[issue 150](https://code.google.com/p/xmemcached/issues/detail?id=150).

Recommend user using xmemcached binary protocol to upgrade.

## xmemcached 1.3.3 release notes ##

  1. Implements touch and GAT(get and touch) commands for memcached 1.6.x,adds new methods to MemcachedClient,includes:
```
         boolean touch(String key, int exp, long opTimeout);
         getAndTouch(String key, int newExp, long opTimeout);
```
  1. Method setLoggingLevelVerbosity works with binary protocol.
  1. Make exception infomation more friendly.
  1. Deprecated KeyIterator and getKeyIterator.
  1. Bug fixed,include:[issue 126](https://code.google.com/p/xmemcached/issues/detail?id=126),[issue 127](https://code.google.com/p/xmemcached/issues/detail?id=127),[issue 128](https://code.google.com/p/xmemcached/issues/detail?id=128),[issue 129](https://code.google.com/p/xmemcached/issues/detail?id=129).
  1. Some changes for future version to implement memcached 1.6.x new commands.

## xmemcached 1.3.2 release notes ##

  1. Bug fixed,include:[issue 113](https://code.google.com/p/xmemcached/issues/detail?id=113),[issue 112](https://code.google.com/p/xmemcached/issues/detail?id=112)
  1. Performance turning, 5% performance increase in store commands(add/repleace/set/append/prepend/cas).
  1. Modify pom.xml to make it work on other machines.
  1. Use github as source repository instead of googlecode svn.Xmemcached source has benn moved to [https://github.com/killme2008/xmemcached](https://github.com/killme2008/xmemcached)

## xmemcached 1.3.1 release notes ##

  1. Keep memcached servers in input order.
  1. Add a new response status 0x06 for binary protocol,which means incr/decr non-number value.

## xmemcached 1.3.0 release notes ##

  1. Add failure mode for MemcachedClient.
  1. When client is in failure mode,you could also set a standby memcached node for every memcached server,and if a memcached server was down,then further requests to that server will transform to the standby node until server comes back.More information about failure mode and standby node see [FailureMode\_StandbyNode](FailureMode_StandbyNode.md)
  1. Fixed issues etc.