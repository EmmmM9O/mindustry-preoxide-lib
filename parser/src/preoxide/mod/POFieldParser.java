package preoxide.mod;

import arc.util.serialization.*;

public interface POFieldParser {
  public Object parse(Class<?> type, JsonValue value) throws Exception;
}
