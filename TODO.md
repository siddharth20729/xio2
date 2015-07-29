## Proxy
TODO
====
 
## Server 
 - Add http2
 - Add epoll Socketchannel and Selector 
  - https://github.com/netty/netty/tree/master/transport-native-epoll/src/main/java/io/netty/channel/epoll
  - https://github.com/netty/netty/tree/master/transport-native-epoll/src/main/c

### Server TLS
 - Accept real certs
 - Configure TLS options & Certs
 - Show meaningful error messages when TSL fails
 - Setup and configure truststore & client cert validation
 
## Client 
 - Add http2
 
### Client TLS
 - Make configurable truststore
 - Create Server cert validation

 - Have Server parse & rewrite headers as necessary and use the same bytebuffer to send req
 - Have Client parse & rewrite headers as necessary and use the same bytebuffer to send resp
 - Needs to terminate and re-wrap TLS
 - Needs to be able to sync and asyc proxy multiple client req's
 - Needs to be able to configure LB and client req's for a given route
