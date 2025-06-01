
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

import mindustry.ctype.*;
import preoxide.graphics.*;

import static preoxide.POPVars.*;

import arc.graphics.*;

public class POPlanetParser {
  public static void init() {
    TypeClassMaps.put(ContentType.planet, "POPlanet", preoxide.universe.POPlanet.class);
    TypeClassMaps.put(ContentType.planet, "FastBlackhole", preoxide.universe.FastBlackhole.class);
    TypeClassMaps.put(ContentType.planet, "NoiseBlackhole", preoxide.universe.NoiseBlackhole.class);
    TypeClassMaps.put(ContentType.planet, "FastAdiskBlackhole",
        preoxide.universe.FastAdiskBlackhole.class);
    var parser = mod.parser;

    parser.addClassParser(Cubemap.class, (type, data) -> {
      if (data == null)
        return null;
      if (data.isString())
        return POGUtil.getCubeMap(data.asString());
      if (data.has("right") && data.has("left") && data.has("top") && data.has("bottom")
          && data.has("front") && data.has("back")) {
        return new Cubemap(POGUtil.getCubeMapT(data.getString("right")),
            POGUtil.getCubeMapT(data.getString("left")), POGUtil.getCubeMapT(data.getString("top")),
            POGUtil.getCubeMapT(data.getString("bottom")),
            POGUtil.getCubeMapT(data.getString("front")),
            POGUtil.getCubeMapT(data.getString("back")));
      }
      throw new IllegalArgumentException(
          "Cubemap must be string or {right:string,left:string,top:string,bottom:string,front:string,back:string}");
    });
  }
}
