xio2
====

High performance I/O for the JVM


###Usage

Http Server

```java

Server s = Http.newServer();
s.addRoute("/", new RootHandler())

Service awesomeServ = new RateLimitServ().andThen(new BusinessLogicServ());
s.addRoute("sweet", awesomeServ);
s.ssl(true);

s.serve(8443);

// and when you are all done

s.close();
```

Http Client
```java
XioClient c = Http.newClient();
c.ssl(true);
c.connect("localhost:8443");

HttpRequest req = new HttpRequest.Builder()
    .uri("/")
    .build

HttpResponse resp = c.get(req);
System.out.println(resp.getStatus());
```

## For dev on Mac

### Increase ulimit for benchmarking and load testing 
modify `/Library/LaunchDaemons/limit.maxfiles.plist`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
  <plist version="1.0">
    <dict>
      <key>Label</key>
        <string>limit.maxfiles</string>
      <key>ProgramArguments</key>
        <array>
          <string>launchctl</string>
          <string>limit</string>
          <string>maxfiles</string>
          <string>65536</string>
          <string>65536</string>
        </array>
      <key>RunAtLoad</key>
        <true/>
      <key>ServiceIPC</key>
        <false/>
    </dict>
  </plist>
```

modify `/Library/LaunchDaemons/limit.maxproc.plist`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple/DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
  <plist version="1.0">
    <dict>
      <key>Label</key>
        <string>limit.maxproc</string>
      <key>ProgramArguments</key>
        <array>
          <string>launchctl</string>
          <string>limit</string>
          <string>maxproc</string>
          <string>2048</string>
          <string>2048</string>
        </array>
      <key>RunAtLoad</key>
        <true />
      <key>ServiceIPC</key>
        <false />
    </dict>
  </plist>
```

Both plist files must be owned by `root:wheel` and have permissions `-rw-r--r--`. This permissions should be in place by default, but you can ensure that they are in place by running `sudo chmod 644 <filename>`. 

