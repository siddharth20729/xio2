package com.xjeffrose.xio2.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

  private Pattern sectionMatcher  = Pattern.compile( "\\s*\\[([^]]*)\\]\\s*" );
  private Pattern kvMatcher = Pattern.compile( "\\s*([^=]*)=(.*)" );
  public Map<String, Map<String, String>> entries  = new HashMap<>();

//  public Config() throws IOException {
//    load("default/path");
//  }

  public Config(String path) throws IOException {
    load(path);
  }

  public void load(String path) throws IOException {
    BufferedReader br = new BufferedReader(
        new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path)));
    String line;
    String section = null;

    while((line = br.readLine()) != null) {
      Matcher m = sectionMatcher.matcher(line);
      if (m.matches()) {
        section = m.group(1).trim();
      } else if (section != null) {
        m = kvMatcher.matcher(line);
        if (m.matches()) {
          String key   = m.group( 1 ).trim();
          String value = m.group( 2 ).trim();
          Map< String, String > kv = entries.get(section);
          if( kv == null ) {
            entries.put( section, kv = new HashMap<>());
          }
          kv.put( key, value );
        }
      }
    }
  }

  public String get( String section, String key) {
    Map< String, String > kv = entries.get( section );
    if( kv == null ) {
      return null;
    }
    return kv.get(key);
  }

  public String get(String section, int key) {
    Map< String, String > kv = entries.get( section );
    if( kv == null ) {
      return null;
    }
    return kv.get(Integer.toString(key));
  }
}

