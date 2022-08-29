package dmk.pg.jreleaser.lib.service.impl;

import dmk.pg.jreleaser.lib.service.Publisher;

/**
 * Publish message to console using {@code Object.toString()} method.
 */
public class ConsolePublisherImpl implements Publisher {

  @Override
  public <T> void publish(T message) {
    System.out.println(message);
  }
}
