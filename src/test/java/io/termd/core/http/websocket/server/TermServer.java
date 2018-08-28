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

package io.termd.core.http.websocket.server;

import io.termd.core.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TermServer {

  private static Thread serverThread;
  private Logger log = LoggerFactory.getLogger(TermServer.class);

  private UndertowBootstrap undertowBootstrap;
  private int port;
  final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

  /**
   * Method returns once server is started.
   *
   * @throws InterruptedException
   */
  public static TermServer start() throws InterruptedException {
    final Semaphore mutex = new Semaphore(1);

    final Runnable onStart = new Runnable() {
      @Override
      public void run() {
        mutex.release();
      }
    };

    final TermServer termServer = new TermServer();

    serverThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          termServer.start("localhost", 0, onStart);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

    mutex.acquire();
    serverThread.start();

    mutex.acquire();
    return termServer;
  }

  public static void stopServer() {
    serverThread.interrupt();
  }


  public void start(final String host, int portCandidate, final Runnable onStart) throws InterruptedException {
    if (portCandidate == 0) {
      portCandidate = findFirstFreePort();
    }
    this.port = portCandidate;

    undertowBootstrap = new UndertowBootstrap(host, port, this);
    undertowBootstrap.bootstrap(new Consumer<Boolean>() {
      @Override
      public void accept(Boolean started) {
        if (started) {
          log.info("Server started on " + host + ":" + port);
          if (onStart != null) {
            onStart.run();
          }
        } else {
          log.info("Could not start server");
        }
      }
    });
  }

  private int findFirstFreePort() {
    ServerSocket s = null;
    try {
      s = new ServerSocket(0);
      return s.getLocalPort();
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not obtain default port, try specifying it explicitly");
    } finally {
      if (s != null) {
        try {
          s.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void stop() {
    undertowBootstrap.stop();
    log.info("Server stopped");
  }

  public int getPort() {
    return port;
  }

}
