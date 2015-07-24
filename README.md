xio2
====

High performance I/O for the JVM


###Usage
```java

Server s = new Server();
s.addRoute("/", new RootHandler())

Service awesomeServ = new RateLimitServ().andThen(new BusinessLogicServ());
s.addRoute("sweet", awesomeServ);

s.serve(8080);
s.serve(8443);

// and when you are all done

s.close();
```