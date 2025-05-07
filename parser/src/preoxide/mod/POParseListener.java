package preoxide.mod;

import arc.util.serialization.*;

public interface POParseListener {
  void parsed(Class<?> type, JsonValue jsonData, Object result);
}
