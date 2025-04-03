package dev.joordih.zenmap.managers.strategy.fetch;

import dev.joordih.zenmap.managers.nodes.Node;
import dev.joordih.zenmap.managers.strategy.Strategy;

import java.io.IOException;
import java.util.List;

public interface DataFetchStrategy<T extends Node> extends Strategy {
  String fetchData(String url) throws IOException;

  List<T> parseJsonList(String json) throws IOException;
}
