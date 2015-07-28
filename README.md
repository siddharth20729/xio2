xio2
====

High performance I/O for the JVM


###Usage

Server

```java

Server s = new Server();
s.addRoute("/", new RootHandler())

Service awesomeServ = new RateLimitServ().andThen(new BusinessLogicServ());
s.addRoute("sweet", awesomeServ);
s.ssl(true);

s.serve(8443);

// and when you are all done

s.close();
```

Client 
```java
XioClient c = new Client();
c.ssl(true);
c.connect("localhost", 8443);

HttpRequest req = new HttpRequest.Builder()
    .uri("/")
    .build
    
HttpResponse resp = c.get(req);
System.out.println(resp.getStatus());
```