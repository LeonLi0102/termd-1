/*
 * Copyright 2015 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.termd.core.tty;

import io.termd.core.function.Consumer;
import io.termd.core.http.netty.NettyWebsocketTtyBootstrap;

import java.util.concurrent.TimeUnit;


/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class NettyWebsocketTtyTest extends WebsocketTtyTestBase {

  private NettyWebsocketTtyBootstrap bootstrap;

  @Override
  protected void server(Consumer<TtyConnection> onConnect) {
    if (bootstrap != null) {
      throw failure("Server already started");
    }
    bootstrap = new NettyWebsocketTtyBootstrap().setHost("localhost").setPort(8080);
    try {
      bootstrap.start(onConnect).get(10, TimeUnit.SECONDS);
    } catch (Throwable e) {
      throw failure(e);
    }
  }

  public void after() throws Exception {
    if (bootstrap != null) {
      try {
        bootstrap.stop().get(10, TimeUnit.SECONDS);
      } catch (Throwable t) {
        t.printStackTrace();
      }
      bootstrap = null;
    }
  }
}
