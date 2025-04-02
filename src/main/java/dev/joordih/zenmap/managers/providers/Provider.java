package dev.joordih.zenmap.managers.providers;


public interface Provider {
  void register();
  default void unregister() {}
}
