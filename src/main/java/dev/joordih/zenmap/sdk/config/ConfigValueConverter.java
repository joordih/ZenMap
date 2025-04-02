package dev.joordih.zenmap.sdk.config;

import java.util.Optional;

public class ConfigValueConverter {
  public static String toString(Optional<Object> value) {
    return value.map(Object::toString).orElse(null);
  }

  public static Integer toInteger(Optional<Object> value) {
    if (value.isEmpty()) {
      return null;
    }

    Object inputValue = value.get();
    if (inputValue instanceof Integer) {
      return (Integer) inputValue;
    }

    if (inputValue instanceof Number) {
      return ((Number) inputValue).intValue();
    }

    try {
      return Integer.parseInt(inputValue.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  public static Boolean toBoolean(Optional<Object> value) {
    if (value.isEmpty()) {
      return null;
    }

    Object inputValue = value.get();
    if (inputValue instanceof Boolean) {
      return (Boolean) inputValue;
    }

    return Boolean.parseBoolean(inputValue.toString());
  }

  public static Double toDouble(Optional<Object> value) {
    if (value.isEmpty()) {
      return null;
    }

    Object inputValue = value.get();
    if (inputValue instanceof Double) {
      return (Double) inputValue;
    }

    if (inputValue instanceof Number) {
      return ((Number) inputValue).doubleValue();
    }

    try {
      return Double.parseDouble(inputValue.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
