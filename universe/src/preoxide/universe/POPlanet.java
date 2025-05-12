/* (C) 2025 */
package preoxide.universe;

import arc.graphics.*;
import arc.graphics.g3d.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class POPlanet extends Planet implements CustomizeRenderI {
  public POPlanet(String name, Planet parent, float radius, int sectorSize) {
    super(name, parent, radius, sectorSize);
  }

  public POPlanet(String name, Planet parent, float radius) {
    super(name, parent, radius);
  }

  public Cubemap cubemap;

  @Override
  public boolean renderSkybox(Camera3D cam, CubemapMesh mesh, PlanetParams params) {
    if (cubemap != null)
      mesh.setCubemap(cubemap);
    return false;
  }
}
