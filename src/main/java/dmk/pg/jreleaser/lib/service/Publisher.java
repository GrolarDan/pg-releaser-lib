package dmk.pg.jreleaser.lib.service;

public interface Publisher {
  /**
   * Publish message.
   *
   * @param message Message to publish
   * @param <T> Message type
   */
  <T> void publish(T message);
}