package cz.masci.pg.releaser.lib.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import cz.masci.pg.releaser.lib.service.Publisher;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsolePublisherImplTest {

  private final PrintStream standardOut = System.out;
  private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

  @BeforeEach
  public void setUp() {
    System.setOut(new PrintStream(outputStreamCaptor));
  }

  @AfterEach
  public void tearDown() {
    System.setOut(standardOut);
  }

  @Test
  void publish() {
    String message = "Test message";

    Publisher publisher = new ConsolePublisherImpl();
    publisher.publish(message);

    assertEquals(message, outputStreamCaptor.toString().trim());
  }
}