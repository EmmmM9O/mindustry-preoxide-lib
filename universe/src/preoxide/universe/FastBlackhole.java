/* (C) 2025 */
package preoxide.universe;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import preoxide.graphics.*;
import preoxide.graphics.POShaders.*;
import static mindustry.Vars.*;

public class FastBlackhole extends POPlanet {
  public FastBlackhole(String name, Planet parent, float radius, int sectorSize) {
    super(name, parent, radius, sectorSize);
  }

  public FastBlackhole(String name, Planet parent, float radius) {
    super(name, parent, radius);
  }

  @Override
  public boolean hiddenRenderer() {
    return true;
  }

  @Override
  public boolean disableRendererAll() {
    return true;
  }

  public float startDistance = 20.0f;
  public int level = 1;
  public @Nullable FastBlackholeShaderBase blackholeShader;
  public Seq<String> rayMaps = Seq.with();

  @Override
  public void load() {
    super.load();
    if (!headless) {
      blackholeShader = switch (level) {
        case 1 -> new FastBlackholeShaderR1();
        default -> throw new IllegalArgumentException("Level " + level + " is invalid");
      };
      blackholeShader.startDistance = startDistance;
      blackholeShader.radius = radius;

      if (rayMaps.isEmpty()) {
        throw new IllegalArgumentException("Blackhole need a ray map use rayMaps:[\"xxx\"] ");
      }
      if (blackholeShader instanceof FastBlackholeShaderR1 r1) {
        if (rayMaps.size != 1)
          Log.warn("level @ only need 1 rayMap get @", level, rayMaps.size);
        r1.rayMap = new Texture(Vars.tree.get(rayMaps.get(0)), true);
      }
    }
  }

  @Override
  public void rendererAll(Camera3D cam, POPlanetRenderer renderer, PlanetParams params) {
    int w = params.viewW <= 0 ? Core.graphics.getWidth() : params.viewW;
    int h = params.viewH <= 0 ? Core.graphics.getHeight() : params.viewH;

    // lock to up vector so it doesn't get confusing
    cam.up.set(Vec3.Y);

    cam.resize(w, h);
    renderer.bloom.resize(w, h);
    params.camPos.setLength((params.planet.radius + params.planet.camRadius) * POPlanetRenderer.camLength
        + (params.zoom - 1f) * (params.planet.radius + params.planet.camRadius) * 2);

    if (params.otherCamPos != null) {
      cam.position.set(params.otherCamPos).lerp(params.planet.position, params.otherCamAlpha).add(params.camPos);
    } else {
      cam.position.set(params.planet.position).add(params.camPos);
    }
    // cam.up.set(params.camUp); //TODO broken
    cam.lookAt(params.planet.position);
    cam.update();
    Draw.flush();
    // renderer.renderF(params);
    // renderer.renderR(params);
    blackholeShader.target = params.planet.solarSystem.position;
    /*
     * blackholeShader.cubemap = POGUtil.getCube(cam, blackholeShader.target, () ->
     * {
     * renderer.bloom.resize(POGUtil.cubeSize, POGUtil.cubeSize);
     * renderer.renderF(params);
     * renderer.renderR(params);
     * });
     */
    blackholeShader.cubemap = renderer.skyboxCube;
    blackholeShader.camera = cam;
    blackholeShader.resolution = POGUtil.t21.set(Core.graphics.getWidth(), Core.graphics.getHeight());
    blackholeShader.cubemap = cubemap;
    Gl.clear(Gl.depthBufferBit);
    Gl.enable(Gl.depthTest);
    Gl.depthMask(false);
    Gl.disable(Gl.cullFace);
    blackholeShader.bind();
    blackholeShader.apply();
    // renderer.bloom.capture();
    POMeshs.screen.render(blackholeShader, Gl.triangles);
    // renderer.bloom.render();
    // renderer.skybox.setCubemap(cubemap);
    // renderer.skybox.render(cam.projection);
    Gl.depthMask(true);
    Gl.enable(Gl.cullFace);
    Gl.cullFace(Gl.back);
    renderer.renderR(params);
  }
}
