/* (C) 2025 */
package preoxide.mod;

import arc.util.serialization.*;

public interface POFieldParser<T> {
  public T parse(Class<?> type, JsonValue value) throws Exception;
}
