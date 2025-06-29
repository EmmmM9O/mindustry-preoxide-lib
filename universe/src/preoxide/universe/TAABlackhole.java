
/*
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
*/
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
import preoxide.POPVars;
import preoxide.graphics.*;
import preoxide.graphics.POUShaders.*;
import preoxide.graphics.bloom.*;
import preoxide.graphics.gl.*;
import preoxide.mod.*;

public class TAABlackhole extends POPlanet implements CustomizeParser {
  public TAABlackhole(String name, Planet parent, float radius, int sectorSize) {
    super(name, parent, radius, sectorSize);
  }

  public TAABlackhole(String name, Planet parent, float radius) {
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
  public @Nullable TAABlackholeShader blackholeShader;
  public @Nullable TAABlackholeComposite composite;
  public int bloomScl = 2;
  public float rayScl = 2.0f;
  public @Nullable TAABlackholeBuffer ray1;
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
    ray1 = new TAABlackholeBuffer((int) (Core.graphics.getWidth() / rayScl),
        (int) (Core.graphics.getHeight() / rayScl), false);

  }

  @Override
  public void onResize() {
    if (bloom != null)
      bloom.resize(Core.graphics.getWidth() / bloomScl, Core.graphics.getHeight() / bloomScl);
    if (ray1 != null)
      ray1.resize((int) (Core.graphics.getWidth() / rayScl),
          (int) (Core.graphics.getHeight() / rayScl));
  }

  public @Nullable TAABlackholeData data;

  @Override
  public void parse(String name, String mod, JsonValue data) throws Exception {
    if (headless)
      return;
    if (data.has("data")) {
      this.data = POPVars.mod.parser.parser.readValue(TAABlackholeData.class, data.get("data"));
      data.remove("data");
    } else {
      this.data = new TAABlackholeData();
    }
    blackholeShader = new TAABlackholeShader();
    blackholeShader.data = this.data;
    blackholeShader.parse(name, mod, data, this);
    composite = new TAABlackholeComposite();
    composite.data = this.data;
  }

  @Override
  public void rendererAll(Camera3D cam, POPlanetRenderer renderer, PlanetParams params) {
    // var cubemap = this.cubemap == null ? renderer.skyboxCube : this.cubemap;
    int w = params.viewW <= 0 ? Core.graphics.getWidth() : params.viewW;
    int h = params.viewH <= 0 ? Core.graphics.getHeight() : params.viewH;

    // lock to up vector so it doesn't get confusing
    cam.up.set(Vec3.Y);

    cam.resize(w, h);
    renderer.bloom.resize(w, h);
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
    blackholeShader.target = params.planet.solarSystem.position;
    renderer.enableBloom = false;
    float ta = params.uiAlpha;
    params.uiAlpha = 0f;
    blackholeShader.cubemap = POGUtil.getCube(cam, blackholeShader.target, () -> {
      renderer.renderF(params);
      renderer.renderR(params);
    });
    params.uiAlpha = ta;
    params.drawUi = true;

    // blackholeShader.cubemap = cubemap;
    blackholeShader.camera = cam;
    blackholeShader.resolution = POGUtil.t21.set(Core.graphics.getWidth() / rayScl, Core.graphics.getHeight() / rayScl);
    cam.update();
    renderer.bloom.blending = !params.drawSkybox;
    ray1.begin();
    Gl.clearColor(0, 0, 0, 0);
    Gl.clear(Gl.depthBufferBit | Gl.colorBufferBit);
    Draw.blit(blackholeShader);
    ray1.end();
    blackholeShader.backbuffer = ray1.getTAA();

    composite.camera = cam;
    composite.target = params.planet.solarSystem.position;
    composite.resolution = POGUtil.t21.set(Core.graphics.getWidth(), Core.graphics.getHeight());
    composite.input = ray1.getTAA();
    composite.background = ray1.getBackground();

    var buffer = POGUtil.getFrameBuffer();
    buffer.begin();
    Gl.clearColor(0, 0, 0, 0);
    Gl.clear(Gl.depthBufferBit | Gl.colorBufferBit);
    Draw.blit(composite);
    buffer.end();
    bloom.capture();
    Gl.clear(Gl.depthBufferBit);

    Gl.enable(Gl.depthTest);
    Gl.depthMask(true);

    Gl.enable(Gl.cullFace);

    Gl.cullFace(Gl.back);
    renderer.renderR(params);
    renderer.enableBloom = true;
    var plane = POGUtil.getPlane();
    plane.texture = buffer.getTexture();
    plane.render(cam);
    bloom.render();
  }

  @Override
  public void dispose() {
    super.dispose();
    blackholeShader.dispose();
    composite.dispose();
    ray1.dispose();
  }

}
