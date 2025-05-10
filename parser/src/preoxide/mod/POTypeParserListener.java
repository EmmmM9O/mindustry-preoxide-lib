/* (C) 2025 */
package preoxide.mod;

import arc.util.serialization.*;
import mindustry.ctype.*;

public interface POTypeParserListener<T extends Content> {
  T parse(T current, String mod, String name, JsonValue value) throws Exception;
}
