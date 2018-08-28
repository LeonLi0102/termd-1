package io.termd.core.telnet;

import io.termd.core.function.Consumer;
import io.termd.core.function.Supplier;
import io.termd.core.tty.TtyConnection;
import io.termd.core.util.Vector;
import org.apache.commons.net.telnet.WindowSizeOptionHandler;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class TelnetTermTest extends TelnetTestBase {

  @Test
  public void testSizeHandler() throws Exception {
    final CountDownLatch latch1 = new CountDownLatch(1);
    final CountDownLatch latch2 = new CountDownLatch(1);
    server.start(new Supplier<TelnetHandler>() {
      @Override
      public TelnetHandler get() {
        final AtomicInteger count = new AtomicInteger();
        return new TelnetTtyConnection(false, false, StandardCharsets.UTF_8, new Consumer<TtyConnection>() {
          @Override
          public void accept(TtyConnection conn) {
            conn.setSizeHandler(new Consumer<Vector>() {
              @Override
              public void accept(Vector size) {
                switch (count.getAndIncrement()) {
                  case 0:
                    assertEquals(20, size.x());
                    assertEquals(10, size.y());
                    latch1.countDown();
                    break;
                  case 1:
                    assertEquals(80, size.x());
                    assertEquals(24, size.y());
                    latch2.countDown();
                    break;
                  case 2:
                    assertEquals(180, size.x());
                    assertEquals(160, size.y());
                    testComplete();
                    break;
                  default:
                    fail("Was not expecting that");
                }
              }
            });
          }
        });
      }
    });
    WindowSizeOptionHandler optionHandler = new WindowSizeOptionHandler(20, 10, false, false, true, false);
    client.setOptionHandler(optionHandler);
    client.connect("localhost", 4000);
    latch1.await(30, TimeUnit.SECONDS);
    client.writeDirectAndFlush(new byte[]{TelnetConnection.BYTE_IAC, TelnetConnection.BYTE_SB, 31, 0, 80, 0, 24, TelnetConnection.BYTE_IAC, TelnetConnection.BYTE_SE});
    latch2.await(30, TimeUnit.SECONDS);
    client.writeDirectAndFlush(new byte[]{TelnetConnection.BYTE_IAC, TelnetConnection.BYTE_SB, 31, 0, (byte) 180, 0, (byte) 160, TelnetConnection.BYTE_IAC, TelnetConnection.BYTE_SE});
    await();
  }
}
