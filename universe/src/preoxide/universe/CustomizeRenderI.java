package preoxide.universe;

import arc.graphics.g3d.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import preoxide.graphics.*;

public interface CustomizeRenderI {

  /**
   * Render the Skybox of POPlanetRenderer
   *
   * @param cam    Camera3D
   * @param mesh   Mesh from POPlanetRenderer
   * @param params params
   * @return wheather to continue render
   */
  default boolean renderSkybox(Camera3D cam, CubemapMesh mesh, PlanetParams params) {
    return false;
  }

  default boolean hiddenRenderer() {
    return false;
  }

  default void afterRender(Camera3D cam, POPlanetRenderer renderer, PlanetParams params) {
  }

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
