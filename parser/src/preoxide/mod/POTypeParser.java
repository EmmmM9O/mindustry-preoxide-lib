/* (C) 2025 */
package preoxide.mod;

import arc.util.serialization.*;
import mindustry.ctype.*;

public interface POTypeParser<T extends Content> {
  T parse(String mod, String name, JsonValue value) throws Exception;
}
