package io.termd.core.readline;

import io.termd.core.TestBase;
import io.termd.core.function.BiConsumer;
import io.termd.core.function.Consumer;
import io.termd.core.function.Supplier;
import io.termd.core.tty.TtyEvent;
import io.termd.core.util.Vector;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ReadlineTest extends TestBase {

  @Test
  public void testPrompt() {
    TestTerm term = new TestTerm(this);
    term.readline(new Consumer<String>() {
      @Override
      public void accept(String s) {
        testComplete();
      }
    });
    term.assertScreen("% ");
    term.assertAt(0, 2);
  }

  @Test
  public void testEnter() {
    TestTerm term = new TestTerm(this);
    term.readline(new Consumer<String>() {
      @Override
      public void accept(String s) {
        testComplete();
      }
    });
    term.read('\r');
    term.assertScreen("% ");
    term.assertAt(1, 0);
    await();
  }

  @Test
  public void cancel() {
    TestTerm term = new TestTerm(this);
    assertFalse(term.readline.cancel());
    term.readline(new Consumer<String>() {
      @Override
      public void accept(String s) {
       assertNull(s);
       testComplete();
      }
    });
    assertTrue(term.readline.cancel());
    assertFalse(term.readline.cancel());
    await();
  }

  @Test
  public void testInsertChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('A');
    term.assertScreen("% A");
    term.assertAt(0, 3);
  }

  @Test
  public void testInsertCharEnter() throws Exception {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    term.read('A');
    term.read('\r');
    term.assertScreen("% A");
    term.assertAt(1, 0);
    assertEquals("A", line.get());
  }

  @Test
  public void testEscapeCR() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('\\');
    term.assertScreen("% \\");
    term.read('\r');
    term.assertScreen(
        "% \\",
        "> "
    );
    term.assertAt(1, 2);
  }

  @Test
  public void testBackwardDeleteChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('A');
    term.read(BACKWARD_DELETE_KEY);
    term.assertScreen("% ");
    term.assertAt(0, 2);
  }

  @Test
  public void testBackwardDelete() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read(BACKWARD_DELETE_KEY);
    term.assertScreen(
        "% "
    );
    term.assertAt(0, 2);
  }

  @Test
  public void testBackwardDeleteLastChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('A');
    term.read('B');
    term.read(8);
    term.assertScreen(
        "% A"
    );
    term.assertAt(0, 3);
  }

  @Test
  public void testBackwardCharBackwardDeleteChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('A');
    term.read('B');
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_DELETE_KEY);
    term.assertScreen(
        "% B"
    );
    term.assertAt(0, 2);
  }

  @Test
  public void testBackwardDeleteEscape() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('\\');
    term.assertScreen("% \\");
    term.read(BACKWARD_DELETE_KEY);
    term.assertScreen(
        "% "
    );
    term.assertAt(0, 2);
  }

  @Test
  public void testBackwardChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read(BACKWARD_KEY);
    term.assertScreen("% ");
    term.assertAt(0, 2);
  }

  @Test
  public void testInsertCharBackwardChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('A');
    term.read(BACKWARD_KEY);
    term.assertScreen("% A");
    term.assertAt(0, 2);
  }

  @Test
  public void testForwardChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read(FORWARD_KEY);
    term.assertScreen("% ");
    term.assertAt(0, 2);
  }

  @Test
  public void testInsertCharForwardChar() {
    TestTerm term = new TestTerm(this);
    term.readlineFail();
    term.read('A');
    term.read(BACKWARD_KEY);
    term.read(FORWARD_KEY);
    term.assertScreen("% A");
    term.assertAt(0, 3);
  }

  @Test
  public void testQuotedMultiline() {
    TestTerm term = new TestTerm(this);
    Supplier<String> a = term.readlineComplete();
    term.read('A');
    term.read('"');
    term.read('\r');
    assertNull(a.get());
    term.assertScreen(
        "% A\"",
        "> ");
    term.read('B');
    term.read('\r');
    term.assertScreen(
        "% A\"",
        "> B",
        "> ");
    assertNull(a.get());
    term.read('C');
    term.read('"');
    term.read('\r');
    term.assertScreen(
        "% A\"",
        "> B",
        "> C\"");
    term.assertAt(3, 0);
    assertEquals("A\"\nB\nC\"", a.get());
  }

  @Test
  public void testPreserveOriginalHandlers() {
    TestTerm term = new TestTerm(this);
    Consumer<int[]> readHandler = new Consumer<int[]>() {
      @Override
      public void accept(int[] ints) {
        // no op
      }
    };
    Consumer<Vector> sizeHandler = new Consumer<Vector>() {
      @Override
      public void accept(Vector vector) {
        // no op
      }
    };
    term.readHandler = readHandler;
    term.sizeHandler = sizeHandler;
    term.readlineComplete();
    assertFalse(term.readHandler == readHandler);
    assertFalse(term.sizeHandler == sizeHandler);
    term.read('\r');
    assertEquals(term.readHandler, readHandler);
    assertEquals(term.sizeHandler, sizeHandler);
  }

  @Test
  public void testBuffering() {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    term.read('h', 'e', 'l', 'l', 'o', '\r', 'b', 'y', 'e', '\r');
    assertEquals("hello", line.get());
    term.assertScreen("% hello");
    term.assertAt(1, 0);
    line = term.readlineComplete();
    term.executeTasks();
    assertEquals("bye", line.get());
    term.assertScreen("% hello", "% bye");
    term.assertAt(2, 0);
  }

  @Test
  public void testHistory() {
    TestTerm term = new TestTerm(this);
    term.readlineComplete();
    term.read('0', '\r');
    term.readlineComplete();
    term.read('1', '\r');
    term.readlineComplete();
    term.read('2', '\r');
    term.assertScreen("% 0", "% 1", "% 2");
    term.readlineComplete();
    term.read('3');
    term.assertScreen("% 0", "% 1", "% 2", "% 3");
    term.read(UP_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 2");
    term.read(UP_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 1");
    term.read(UP_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 0");
    term.read(UP_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 0");
    term.read(DOWN_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 1");
    term.read('_');
    term.assertScreen("% 0", "% 1", "% 2", "% 1_");
    term.read(DOWN_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 2");
    term.read(UP_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 1");
    term.read(DOWN_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 2");
    term.read(DOWN_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 3");
    term.read(DOWN_KEY);
    term.assertScreen("% 0", "% 1", "% 2", "% 3");
  }

  @Test
  public void testEndOfLine() {
    TestTerm term = new TestTerm(this);
    term.readlineComplete();
    term.read('a', 'b', 'c', 'd');
    term.read(CTRL_A_KEY);
    term.assertScreen("% abcd");
    term.assertAt(0, 2);
    term.read(CTRL_A_KEY);
    term.assertScreen("% abcd");
    term.assertAt(0, 2);
  }

  @Test
  public void testBeginningOfLine() {
    TestTerm term = new TestTerm(this);
    term.readlineComplete();
    term.read('a', 'b', 'c', 'd');
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.assertScreen("% abcd");
    term.assertAt(0, 2);
    term.read(CTRL_E_KEY);
    term.assertScreen("% abcd");
    term.assertAt(0, 6);
    term.assertScreen("% abcd");
    term.assertAt(0, 6);
  }

  @Test
  public void testResetDuringInteraction1() {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    term.read('a', 'b', 'c', 'd');
    term.read(CTRL_C_KEY);
    term.read('e');
    term.assertScreen("% abcd", "% e");
    term.assertAt(1, 3);
    term.read('\r');
    assertEquals("e", line.get());
  }

  @Test
  public void testResetDuringInteraction2() {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    term.read('a', 'b', 'c', 'd', '\\', '\r');
    term.read(CTRL_C_KEY);
    term.read('e');
    term.assertScreen("% abcd\\", "> ", "% e");
    term.assertAt(2, 3);
    term.read('\r');
    assertEquals("e", line.get());
  }

  @Test
  public void testIllegalChar() {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    assertEquals(0, term.getBellCount());
    term.read('a', 6, 'b', '\r');
    assertEquals(1, term.getBellCount());
    assertEquals("ab", line.get());
  }

  @Test
  public void testEventHandler() {
    TestTerm term = new TestTerm(this);
    final LinkedList<TtyEvent> events = new LinkedList<TtyEvent>();
    BiConsumer<TtyEvent, Integer> handler = new BiConsumer<TtyEvent, Integer>() {
      @Override
      public void accept(TtyEvent event, Integer cp) {
        events.add(event);
      }
    };
    term.eventHandler = handler;
    Supplier<String> line = term.readlineComplete();
    term.read(CTRL_C_KEY);
    term.read('\r');
    assertEquals("", line.get());
    assertEquals(Collections.emptyList(), events);
    term.eventHandler.accept(TtyEvent.EOF, 4);
    assertEquals(Collections.singletonList(TtyEvent.EOF), events);
  }

  @Test
  public void testEOF() {
    TestTerm term = new TestTerm(this);
    final LinkedList<String> lines = new LinkedList<String>();
    term.readline(new Consumer<String>() {
      @Override
      public void accept(String s) {
        lines.add(s);
      }
    });
    term.read(TtyEvent.EOF.codePoint());
    assertEquals(Collections.singletonList(null), lines);
  }

  @Test
  public void testDeleteChar() {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    term.read('a', 'b', 'c');
    term.read(CTRL_D_KEY);
    term.assertScreen("% abc");
    term.assertAt(0, 5);
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.read(CTRL_D_KEY);
    term.assertScreen("% ac");
    term.assertAt(0, 3);
    term.read('\r');
    assertEquals("ac", line.get());
  }

  @Test
  public void testKillLine() {
    TestTerm term = new TestTerm(this);
    Supplier<String> line = term.readlineComplete();
    term.setWidth(4);
    term.read('a', 'b', 'c', 'd', 'e');
    term.assertScreen("% ab", "cde");
    term.assertAt(1, 3);
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.read(BACKWARD_KEY);
    term.assertScreen("% ab", "cde");
    term.assertAt(0, 3);
    term.read(KILL_LINE);
    term.assertScreen("% a", "");
    term.assertAt(0, 3);
    term.read('\r');
    assertEquals("a", line.get());
  }

  @Test
  public void testBackwardWord() {
    TestTerm term = new TestTerm(this);
    term.readlineComplete();
    term.read('a', '0', '1', ' ', 'e', '9', '_', '_' ,'5', 'a');
    term.read(BACKWARD_KEY);
    term.assertAt(0, 11);
    term.read(BACKWARD_WORD_SEQ);
    term.assertAt(0, 10);
    term.read(BACKWARD_WORD_SEQ);
    term.assertAt(0, 6);
    term.read(BACKWARD_WORD_SEQ);
    term.assertAt(0, 2);
    term.read(BACKWARD_WORD_SEQ);
    term.assertAt(0, 2);
  }

  @Test
  public void testForwardWord() {
    TestTerm term = new TestTerm(this);
    term.readlineComplete();
    term.read('a', '0', '1', ' ', 'e', '9', '_', '_', '5', 'a');
    term.read(CTRL_A_KEY);
    term.assertAt(0, 2);
    term.read(FORWARD_KEY);
    term.assertAt(0, 3);
    term.read(FORWARD_WORD_SEQ);
    term.assertAt(0, 5);
    term.read(FORWARD_WORD_SEQ);
    term.assertAt(0, 8);
    term.read(FORWARD_WORD_SEQ);
    term.assertAt(0, 12);
    term.read(FORWARD_WORD_SEQ);
    term.assertAt(0, 12);
  }

  @Test
  public void testBackwardKillWord() {
    TestTerm term = new TestTerm(this);
    term.readlineComplete();
    term.read('a', '0', '1', ' ', 'e', '9', '_', '_', '5', 'a');
    term.read(BACKWARD_KEY);
    term.assertAt(0, 11);
    term.read(META_BACKSPACE);
    term.assertAt(0, 10);
    term.assertScreen("% a01 e9__a");
    term.read(META_BACKSPACE);
    term.assertAt(0, 6);
    term.assertScreen("% a01 a");
    term.read(META_BACKSPACE);
    term.assertAt(0, 2);
    term.assertScreen("% a");
    term.read(META_BACKSPACE);
    term.assertAt(0, 2);
    term.assertScreen("% a");
  }
}
