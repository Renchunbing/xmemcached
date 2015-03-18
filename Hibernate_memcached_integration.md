# Introduction #
> If you use memcached for hibernate's second level cache,you can use [hiberante-memcached](http://code.google.com/p/hibernate-memcached/).This project use spymemcached by default,xmemcached has implement it's interfaces to support it.

# Configuration #

> Most of configuration is the same with hibernate-memcached which use spymemcached(please see it's wiki page).First,you have to set memcacheClientFactory to XmemcachedClientFactory:

| **Property** | **Value** |
|:-------------|:----------|
|hibernate.memcached.memcacheClientFactory | net.rubyeye.xmemcached.utils.hibernate.XmemcachedClientFactory |


> The cache wide settings have some differences.
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
