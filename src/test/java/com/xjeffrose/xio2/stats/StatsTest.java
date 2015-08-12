/*
 *  Copyright (C) 2015 Jeff Rose
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

package com.xjeffrose.xio2.stats;

import com.xjeffrose.xio2.Server;
import com.xjeffrose.xio2.http.Http;
import com.xjeffrose.xio2.http.server.FileHandler;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatsTest {

  @Test
  public void testHandleGet() throws Exception {
    Server s = Http.newServer();
    s.bind(8081, new FileHandler("src/main/resources/"));
    s.serve();

    Thread.sleep(5000000);
  }
}