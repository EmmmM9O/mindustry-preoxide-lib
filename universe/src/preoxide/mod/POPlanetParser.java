/* (C) 2025 */
package preoxide.mod;

import mindustry.ctype.*;
import preoxide.graphics.*;

import static preoxide.POPVars.*;

import arc.graphics.*;

public class POPlanetParser {
  public static void init() {
    TypeClassMaps.put(ContentType.planet, "POPlanet", preoxide.universe.POPlanet.class);
    TypeClassMaps.put(ContentType.planet, "FastBlackhole", preoxide.universe.FastBlackhole.class);
    var parser = mod.parser;

    parser.addClassParser(Cubemap.class, (type, data) -> {
      if (data == null)
        return null;
      if (data.isString())
        return POGUtil.getCubeMap(data.asString());
      if (data.has("right") && data.has("left") && data.has("top") &&
          data.has("bottom") && data.has("front") && data.has("back")) {
        return new Cubemap(
            POGUtil.getCubeMapT(data.getString("right")),
            POGUtil.getCubeMapT(data.getString("left")),
            POGUtil.getCubeMapT(data.getString("top")),
            POGUtil.getCubeMapT(data.getString("bottom")),
            POGUtil.getCubeMapT(data.getString("front")),
            POGUtil.getCubeMapT(data.getString("back")));
      }
      throw new IllegalArgumentException(
          "Cubemap must be string or {right:string,left:string,top:string,bottom:string,front:string,back:string}");
    });
  }
}
