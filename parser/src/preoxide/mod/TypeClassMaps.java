/* (C) 2025 */
package preoxide.mod;

import arc.struct.*;
import mindustry.ctype.*;

public class TypeClassMaps {
  public static final ObjectMap<ContentType, ObjectMap<String, Class<?>>> typeClasses =
      new ObjectMap<>();

  public static ObjectMap<String, Class<?>> getClasses(ContentType type) {
    return typeClasses.get(type, ObjectMap::new);
  }

  public static void put(ContentType type, String name, Class<?> clazz) {
    getClasses(type).put(name, clazz);
  }

  public static Class<?> get(ContentType type, String name) {
    return getClasses(type).get(name);
  }

  static {
    put(ContentType.planet, "Planet", mindustry.type.Planet.class);
  }
}
