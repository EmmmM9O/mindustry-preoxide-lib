/* (C) 2025 */
package preoxide.universe;

import arc.graphics.*;
import arc.graphics.g3d.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class POPlanet extends Planet implements CustomizeRenderI, Disposable {

  public POPlanet(String name, Planet parent, float radius, int sectorSize) {
    super(name, parent, radius, sectorSize);
  }

  public POPlanet(String name, Planet parent, float radius) {
    super(name, parent, radius);
  }

  public Cubemap cubemap;
  public float camLength = 4.0f, maxZoom = 2.0f;

  @Override
  public boolean renderSkybox(Camera3D cam, CubemapMesh mesh, PlanetParams params) {
    if (cubemap != null)
      mesh.setCubemap(cubemap);
    return false;
  }

  @Override
  public void dispose() {
    if (cubemap != null && !cubemap.isDisposed())
      cubemap.dispose();
  }

  @Override
  public float camLength() {
    return camLength;
  }

  @Override
  public float maxZoom() {
    return maxZoom;
  }
}
