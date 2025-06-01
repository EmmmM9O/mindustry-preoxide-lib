
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

import arc.graphics.g3d.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import preoxide.graphics.*;

public interface CustomizeRenderI {

  /**
   * Render the Skybox of POPlanetRenderer
   *
   * @param cam Camera3D
   * @param mesh Mesh from POPlanetRenderer
   * @param params params
   * @return wheather to continue render
   */
  default boolean renderSkybox(Camera3D cam, CubemapMesh mesh, PlanetParams params) {
    return false;
  }

  default boolean hiddenRenderer() {
    return false;
  }

  default void afterRender(Camera3D cam, POPlanetRenderer renderer, PlanetParams params) {}

  default boolean disableRendererAll() {
    return false;
  }

  default void rendererAll(Camera3D cam, POPlanetRenderer renderer, PlanetParams params) {

  }

  default float camLength() {
    return 4.0f;
  }

  default float maxZoom() {
    return 4.0f;
  }

  default void onResize() {

  }
}
