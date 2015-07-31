/*
 * Copyright (C) 2015 Jeff Rose
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.xjeffrose.xio2.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Config {

  private Pattern sectionMatcher = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
  private Pattern kvMatcher = Pattern.compile("\\s*([^=]*)=(.*)");
  public Map<String, Map<String, String>> entries  = new HashMap<>();

  public Config(String path) {
    load(path);
  }

  public void load(String path) {
    BufferedReader br = new BufferedReader(
        new InputStreamReader(getClass().getClassLoader().getResourceAsStream(path)));
    String line;
    String section = null;

    try {
      while ((line = br.readLine()) != null) {
        Matcher m = sectionMatcher.matcher(line);
        if (m.matches()) {
          section = m.group(1).trim();
        } else if (section != null) {
          m = kvMatcher.matcher(line);
          if (m.matches()) {
            String key = m.group(1).trim();
            String value = m.group(2).trim();
            Map<String, String> kv = entries.get(section);
            if (kv == null) {
              entries.put(section, kv = new HashMap<>());
            }
            kv.put(key, value);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public String get(String section, String key) {
    Map<String, String> kv = entries.get(section);
    if (kv == null) {
      return null;
    }
    return kv.get(key);
  }

  public String get(String section, int key) {
    Map<String, String>  kv = entries.get(section);
    if (kv == null) {
      return null;
    }
    return kv.get(Integer.toString(key));
  }
}


