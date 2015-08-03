package com.xjeffrose.xio2.util;

import com.sun.management.UnixOperatingSystemMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class OS {
  public static long getOpenFileDescriptorCount() {
    OperatingSystemMXBean osStats = ManagementFactory.getOperatingSystemMXBean();

    if (osStats instanceof UnixOperatingSystemMXBean) {
      UnixOperatingSystemMXBean unixStats = (UnixOperatingSystemMXBean)osStats;
      return unixStats.getOpenFileDescriptorCount();
    }
    return 0;
  }

  public static long getMaxFileDescriptorCount() {
    OperatingSystemMXBean osStats = ManagementFactory.getOperatingSystemMXBean();

    if (osStats instanceof UnixOperatingSystemMXBean) {
      UnixOperatingSystemMXBean unixStats = (UnixOperatingSystemMXBean)osStats;
      return unixStats.getMaxFileDescriptorCount();
    }
    return 0;
  }

  public enum OS_TYPE {
    LINUX,
    UNIX
  }

  public static OS_TYPE getOS() {

    String name = System.getProperty("os.name").toLowerCase().trim();
    if (name.startsWith("linux")) {
      return OS_TYPE.LINUX;
    } else if (name.contains("BSD")) {
      return OS_TYPE.UNIX;
    } else {
      throw new RuntimeException("Unsuppoerted OS_TYPE");
    }
  }
}
