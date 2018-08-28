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

package examples.screencast;

import io.termd.core.function.Consumer;
import io.termd.core.ssh.netty.NettySshTtyBootstrap;
import io.termd.core.tty.TtyConnection;

import java.awt.*;
import java.util.concurrent.TimeUnit;

public class SshScreencastingExample {

  public synchronized static void main(String[] args) throws Throwable {
    NettySshTtyBootstrap bootstrap = new NettySshTtyBootstrap().
        setPort(5000).
        setHost("localhost");
    final Robot robot = new Robot();
    bootstrap.start(new Consumer<TtyConnection>() {
      @Override
      public void accept(TtyConnection conn) {
        new Screencaster(robot, conn).handle();
      }
    }).get(10, TimeUnit.SECONDS);
    System.out.println("SSH started on localhost:5000");
    SshScreencastingExample.class.wait();
  }
}
