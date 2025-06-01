
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
