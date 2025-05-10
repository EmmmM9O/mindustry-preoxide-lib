/* (C) 2025 */
package preoxide.mod;

import arc.util.serialization.*;

public interface POChildParser<R, T> {
  public R parse(T father, JsonValue value) throws Exception;
}
