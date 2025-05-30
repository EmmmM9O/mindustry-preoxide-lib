/* (C) 2025 */
package preoxide.universe;

import static mindustry.Vars.*;
import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import preoxide.graphics.*;
import preoxide.graphics.POUShaders.*;
import preoxide.graphics.bloom.OCBloom;
import preoxide.graphics.bloom.PyramidFourNAvgBloom;
import preoxide.graphics.gl.*;
import preoxide.mod.*;

public class FastAdiskBlackhole extends POPlanet implements CustomizeParser {
  public FastAdiskBlackhole(String name, Planet parent, float radius, int sectorSize) {
    super(name, parent, radius, sectorSize);
  }

  public FastAdiskBlackhole(String name, Planet parent, float radius) {
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
  public float invScl = 1.0f;
  public @Nullable FastAdiskBlackholeBase blackholeShader;
  public int bloomScl = 2;
  public int level;
  public float rayScl = 2.0f;
  public @Nullable OCBloom bloom;

  @Override
  public void init() {
    super.init();

  }

  @Override
  public void load() {
    super.load();
    if (bloom == null)
      bloom = new PyramidFourNAvgBloom(Core.graphics.getWidth() / bloomScl,
          Core.graphics.getHeight() / bloomScl, true);
  }

  @Override
  public void onResize() {
    if (bloom != null)
      bloom.resize(Core.graphics.getWidth() / bloomScl, Core.graphics.getHeight() / bloomScl);
    super.onResize();
  }

  @Override
  public void parse(String name, String mod, JsonValue data) throws Exception {
    if (headless)
      return;
    if (!data.has("level"))
      throw new IllegalArgumentException("No argument level for blackhole");
    level = data.getInt("level");
    data.remove("level");

    blackholeShader = switch (level) {
      case 1 -> new FastAdiskBlackholeShaderR1();
      case 2 -> new FastAdiskBlackholeShaderR2();
      default -> throw new IllegalArgumentException("Level " + level + " is invalid");
    };
    blackholeShader.parse(name, mod, data, this);
  }

  @Override
  public void rendererAll(Camera3D cam, POPlanetRenderer renderer, PlanetParams params) {
    int w = params.viewW <= 0 ? Core.graphics.getWidth() : params.viewW;
    int h = params.viewH <= 0 ? Core.graphics.getHeight() : params.viewH;

    // lock to up vector so it doesn't get confusing
    cam.up.set(Vec3.Y);

    cam.resize(w, h);
    // renderer.bloom.resize(w, h);
    params.camPos.setLength(
        (params.planet.radius + params.planet.camRadius) * POPlanetRenderer.getCamLength(params)
            + (params.zoom - 1f) * (params.planet.radius + params.planet.camRadius) * 2);

    if (params.otherCamPos != null) {
      cam.position.set(params.otherCamPos).lerp(params.planet.position, params.otherCamAlpha)
          .add(params.camPos);
    } else {
      cam.position.set(params.planet.position).add(params.camPos);
    }
    // cam.up.set(params.camUp); //TODO broken
    cam.lookAt(params.planet.position);
    cam.update();
    Draw.flush();
    blackholeShader.startDistance = startDistance;
    blackholeShader.scl = invScl;
    blackholeShader.target = params.planet.solarSystem.position;
    renderer.enableBloom = false;

    blackholeShader.cubemap = POGUtil.getCube(cam, blackholeShader.target, () -> {
      renderer.renderF(params);
      renderer.renderR(params);
    });

    // blackholeShader.cubemap = cubemap;
    blackholeShader.camera = cam;
    blackholeShader.resolution = POGUtil.t21.set(Core.graphics.getWidth(), Core.graphics.getHeight());
    cam.update();

    renderer.bloom.blending = !params.drawSkybox;
    bloom.capture();
    Gl.clear(Gl.depthBufferBit);

    Gl.enable(Gl.depthTest);
    Gl.depthMask(true);

    Gl.enable(Gl.cullFace);

    Gl.cullFace(Gl.back);
    renderer.renderR(params);
    bloom.capturePause();
    renderer.enableBloom = true;
    var buf = POGUtil.getFrameBuffer();
    buf.begin();
    Gl.clearColor(0, 0, 0, 0);
    Gl.clear(Gl.depthBufferBit | Gl.colorBufferBit);
    Draw.blit(blackholeShader);
    buf.end();
    var plane = POGUtil.getPlane();
    plane.texture = buf.getTexture();
    bloom.captureContinue();
    plane.render(cam);
    bloom.render();
  }

  @Override
  public void dispose() {
    super.dispose();
    blackholeShader.dispose();
  }

}
