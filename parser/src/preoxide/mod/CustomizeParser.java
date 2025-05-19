/* (C) 2025 */
package preoxide.mod;

import arc.util.serialization.*;

public interface CustomizeParser {
  public default void parse(String name, String mod, JsonValue data) throws Exception {}
}
