
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

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import preoxide.graphics.*;
import preoxide.graphics.POUShaders.*;
import preoxide.mod.*;

import static mindustry.Vars.*;

public class FastBlackhole extends POPlanet implements CustomizeParser {
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
  public float invScl = 5.0f;
  public int level;
  public @Nullable FastBlackholeBase blackholeShader;

  @Override
  public void parse(String name, String mod, JsonValue data) throws Exception {
    if (headless)
      return;
    if (!data.has("level"))
      throw new IllegalArgumentException("No argument level for blackhole");
    level = data.getInt("level");
    data.remove("level");

    blackholeShader = switch (level) {
      case 0 -> new FastBlackholeShaderR0();
      case 1 -> new FastBlackholeShaderR1();
      case 2 -> new FastBlackholeShaderR2();
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
    blackholeShader.resolution =
        POGUtil.t21.set(Core.graphics.getWidth(), Core.graphics.getHeight());
    Gl.clear(Gl.depthBufferBit);
    cam.update();

    renderer.bloom.blending = !params.drawSkybox;

    Gl.enable(Gl.depthTest);
    Gl.depthMask(true);

    Gl.enable(Gl.cullFace);

    Gl.cullFace(Gl.back);
    renderer.renderR(params);
    renderer.enableBloom = true;

    var buf = POGUtil.getFrameBuffer();
    buf.begin();
    Gl.clearColor(0, 0, 0, 0);
    Gl.clear(Gl.depthBufferBit | Gl.colorBufferBit);
    Draw.blit(blackholeShader);
    buf.end();
    var plane = POGUtil.getPlane();
    plane.texture = buf.getTexture();
    plane.render(cam);
  }

  @Override
  public void dispose() {
    super.dispose();
    blackholeShader.dispose();
  }

}
