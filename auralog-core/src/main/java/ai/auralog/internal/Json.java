package ai.auralog.internal;

import java.util.Map;

final class Json {
  private Json() {}

  static String encode(Object value) {
    StringBuilder sb = new StringBuilder();
    writeValue(sb, value);
    return sb.toString();
  }

  private static void writeValue(StringBuilder sb, Object value) {
    if (value == null) {
      sb.append("null");
      return;
    }
    if (value instanceof String) {
      writeString(sb, (String) value);
      return;
    }
    if (value instanceof Number || value instanceof Boolean) {
      sb.append(value);
      return;
    }
    if (value instanceof Map) {
      writeMap(sb, (Map<?, ?>) value);
      return;
    }
    if (value instanceof Iterable) {
      writeArray(sb, (Iterable<?>) value);
      return;
    }
    writeString(sb, value.toString());
  }

  private static void writeMap(StringBuilder sb, Map<?, ?> m) {
    sb.append('{');
    boolean first = true;
    for (Map.Entry<?, ?> e : m.entrySet()) {
      if (!first) sb.append(',');
      writeString(sb, String.valueOf(e.getKey()));
      sb.append(':');
      writeValue(sb, e.getValue());
      first = false;
    }
    sb.append('}');
  }

  private static void writeArray(StringBuilder sb, Iterable<?> it) {
    sb.append('[');
    boolean first = true;
    for (Object v : it) {
      if (!first) sb.append(',');
      writeValue(sb, v);
      first = false;
    }
    sb.append(']');
  }

  private static void writeString(StringBuilder sb, String s) {
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"':
          sb.append("\\\"");
          break;
        case '\\':
          sb.append("\\\\");
          break;
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        default:
          if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
          else sb.append(c);
          break;
      }
    }
    sb.append('"');
  }
}
