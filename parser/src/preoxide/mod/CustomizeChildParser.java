/* (C) 2025 */
package preoxide.mod;

import arc.util.serialization.*;

public interface CustomizeChildParser<T> {
  public default void parse(String name, String mod, JsonValue data, T father) throws Exception {}
}
