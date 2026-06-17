package dev.joordih.zenmap.managers.strategy.query;

import dev.joordih.zenmap.managers.strategy.Strategy;

public interface QueryStrategy<T> extends Strategy {
  T runQuery(String query);
}
