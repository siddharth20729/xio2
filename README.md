### Status
[![Build Status](https://travis-ci.org/xjdr/xio2.png)](https://travis-ci.org/xjdr/xio2)  [![Coverage Status](https://coveralls.io/repos/xjdr/xio2/badge.svg?branch=master&service=github)](https://coveralls.io/github/xjdr/xio2?branch=master)

xio2
====

High performance I/O for the JVM

###Usage

Http Server

```java
Server s = Http.newServer();

HttpsHandler awesomeHandler = new HttpsHandler(pathToPrivateKey, pathToX509Cert);
Service awesomeServ = new RateLimitServ().andThen(new BusinessLogicServ());
awesomeHandler.addRoute("/sweet", awesomeServ);

s.serve(8443, awesomeHandler);

// and when you are all done

s.close();
```

Http Client
```java
XioClient c = Http.newTLSClient();
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

Now reboot your computer and run

```shell
$ ulimit -a
```

to verify that the limits have been changed

## TODO

* refactor tls/ssl into HTTPSRequest which wraps HTTPRequest
* FileChannel for serving static content
* StatsHandler for serving up stats
