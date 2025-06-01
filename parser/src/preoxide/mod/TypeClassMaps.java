
mindustry preoxide lib
            Copyright (C) 2025 EmmmM9O

            This program is free software: you can redistribute it and/or modify
            it under the terms of the GNU General Public License as published by
            the Free Software Foundation, either version 3 of the License, or
            (at your option) any later version.

            This program is distributed in the hope that it will be useful,
            but WITHOUT ANY WARRANTY; without even the implied warranty of
            MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
            GNU General Public License for more details.

            You should have received a copy of the GNU General Public License
            along with this program.  If not, see <https://www.gnu.org/licenses/>.
package preoxide.mod;

import arc.struct.*;
import mindustry.ctype.*;
import mindustry.maps.generators.*;
import mindustry.maps.planet.*;

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

  public static final ObjectMap<Class<?>, ObjectMap<String, Class<?>>> baseClasses =
      new ObjectMap<>();

  public static ObjectMap<String, Class<?>> getBaseClasses(Class<?> type) {
    return baseClasses.get(type, ObjectMap::new);
  }

  public static void putBase(Class<?> type, String name, Class<?> clazz) {
    getBaseClasses(type).put(name, clazz);
  }

  public static Class<?> getBase(Class<?> type, String name) {
    return getBaseClasses(type).get(name);
  }

  static {
    put(ContentType.planet, "Planet", mindustry.type.Planet.class);
    putBase(PlanetGenerator.class, "SerpuloPlanetGenerator", SerpuloPlanetGenerator.class);
    putBase(PlanetGenerator.class, "ErekirPlanetGenerator", ErekirPlanetGenerator.class);
  }

}
