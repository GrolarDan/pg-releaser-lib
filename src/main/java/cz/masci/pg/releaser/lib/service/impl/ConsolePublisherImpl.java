package cz.masci.pg.releaser.lib.service.impl;

import cz.masci.pg.releaser.lib.service.Publisher;

/**
 * Publish message to console using {@code Object.toString()} method.
 */
public class ConsolePublisherImpl implements Publisher {

  @Override
  public <T> void publish(T message) {
    System.out.println(message);
  }
}
