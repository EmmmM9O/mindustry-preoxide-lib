
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
package preoxide.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g3d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import mindustry.*;
import preoxide.*;
import preoxide.graphics.POGShaders.*;
import preoxide.mod.*;
import preoxide.universe.*;

public class POUShaders {
  public static RayTestShader testS;

  public static void init() {
    testS = new RayTestShader();
  }

  public static abstract class NoiseBlackholeBase extends POLoadShader
      implements CustomizeChildParser<NoiseBlackhole> {
    public Camera3D camera;
    public Vec3 target;
    public Vec2 resolution;
    public Cubemap cubemap;
    public float startDistance, scl, radius;
    public BlackholeRayData ray;
    public BlackholeAdiskData adisk;

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180) / 2.0)) * 2.0f);
      setUniformf("u_start_distance", startDistance);
      setUniformf("u_start_distance_2", startDistance * startDistance);
      setUniformf("u_scl", scl);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", POGUtil.t34.set(camera.position).sub(target));
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
      cubemap.bind(0);
      setUniformi("u_cubemap", 0);

      setUniformf("u_radius_2", radius * radius);
      setUniformf("u_time", Time.globalTime / 10f);
      ray.apply(this);
      adisk.apply(this);
    }

    public abstract String addon();

    public NoiseBlackholeBase() {
      super("universe/noise_blackhole_base", "screen");
    }

    @Override
    protected String preprocess(String source, boolean fragment) {
      return super.preprocess(
          source + (fragment ? POGShaders.getShaderFi(addon()).readString() : ""), fragment);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, NoiseBlackhole father)
        throws Exception {
      if (data.has("ray")) {
        this.ray = POPVars.mod.parser.parser.readValue(BlackholeRayData.class, data.get("ray"));
        data.remove("ray");
      } else {
        this.ray = new BlackholeRayData();
      }
      if (data.has("adisk")) {
        this.adisk = POPVars.mod.parser.parser.readValue(BlackholeAdiskData.class, data.get("adisk"));
        data.remove("adisk");
      } else {
        this.adisk = new BlackholeAdiskData();
      }
    }
  }

  public static class Gas1NoiseBlackhole extends NoiseBlackholeBase {
    public int adiskNoiseLOD1 = 4;
    public Texture noise;

    @Override
    public void apply() {
      super.apply();
      setUniformi("u_adisk_noise_LOD_1", adiskNoiseLOD1);
      noise.bind(3);
      setUniformi("u_noise_tex", 3);

      Gl.activeTexture(Gl.texture0);
    }

    @Override
    public String addon() {
      return "universe/gas1.frag";
    }

    @Override
    public void parse(String name, String mod, JsonValue data, NoiseBlackhole father)
        throws Exception {
      super.parse(name, mod, data, father);
      if (data.has("noiseLOD1")) {
        adiskNoiseLOD1 = data.getInt("noiseLOD1");
        data.remove("noiseLOD1");
      }

      if (!data.has("noise"))
        throw new IllegalArgumentException("must have noise");
      noise = new Texture(Vars.tree.get(data.getString("noise")), true);
      data.remove("noise");

    }

    @Override
    public void dispose() {
      super.dispose();
      noise.dispose();
    }
  }

  public static class Gas2NoiseBlackhole extends NoiseBlackholeBase {
    public int adiskNoiseLOD1 = 4, adiskNoiseLOD2 = 4;
    public Texture noise;

    @Override
    public void apply() {
      super.apply();
      setUniformi("u_adisk_noise_LOD_1", adiskNoiseLOD1);
      setUniformi("u_adisk_noise_LOD_2", adiskNoiseLOD2);
      noise.bind(3);
      setUniformi("u_noise_tex", 3);

      Gl.activeTexture(Gl.texture0);
    }

    @Override
    public String addon() {
      return "universe/noise_gas2.frag";
    }

    @Override
    public void parse(String name, String mod, JsonValue data, NoiseBlackhole father)
        throws Exception {
      super.parse(name, mod, data, father);
      if (data.has("noiseLOD1")) {
        adiskNoiseLOD1 = data.getInt("noiseLOD1");
        data.remove("noiseLOD1");
      }
      if (data.has("noiseLOD2")) {
        adiskNoiseLOD2 = data.getInt("noiseLOD2");
        data.remove("noiseLOD2");
      }
      if (!data.has("noise"))
        throw new IllegalArgumentException("must have noise");
      noise = new Texture(Vars.tree.get(data.getString("noise")), true);
      data.remove("noise");
    }

    @Override
    public void dispose() {
      super.dispose();
      noise.dispose();
    }
  }

  public static abstract class FastBlackholeBase extends POLoadShader
      implements CustomizeChildParser<FastBlackhole> {
    public Camera3D camera;
    public Vec3 target;
    public Vec2 resolution;
    public Cubemap cubemap;
    public float startDistance, scl;

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180)) / 2.0) * 2.0f);
      setUniformf("u_start_distance", startDistance);
      setUniformf("u_start_distance_2", startDistance * startDistance);
      setUniformf("u_scl", scl);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", POGUtil.t34.set(camera.position).sub(target));
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
      cubemap.bind(0);
      setUniformi("u_cubemap", 0);
    }

    public FastBlackholeBase(String frag, String vert) {
      super(frag, vert);
    }
  }

  public static abstract class FastBlackholeShaderRayMapBase extends FastBlackholeBase {
    public Seq<String> rayMaps;

    public abstract String addon();

    public FastBlackholeShaderRayMapBase() {
      super("universe/fast_blackhole_base", "screen");
    }

    @Override
    protected String preprocess(String source, boolean fragment) {
      return super.preprocess(
          source + (fragment ? POGShaders.getShaderFi(addon()).readString() : ""), fragment);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, FastBlackhole father)
        throws Exception {
      if (!data.has("rayMaps")) {
        throw new IllegalArgumentException("need ray map");
      }
      rayMaps = Seq.with(data.get("rayMaps").asStringArray());
      data.remove("rayMaps");
      if (rayMaps.isEmpty()) {
        throw new IllegalArgumentException("Blackhole need a ray map use rayMaps:[\"xxx\"] ");
      }

    }
  }

  public static class FastBlackholeShaderR1 extends FastBlackholeShaderRayMapBase {

    public Texture rayMap;

    @Override
    public String addon() {
      return "universe/fast_blackhole_1.frag";
    }

    @Override
    public void apply() {
      super.apply();
      rayMap.bind(2);
      setUniformi("u_ray_map", 2);
      Gl.activeTexture(Gl.texture0);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, FastBlackhole p) throws Exception {
      super.parse(name, mod, data, p);
      if (rayMaps.size != 1)
        Log.warn("level @ only need 1 rayMap get @", p.level, rayMaps.size);
      rayMap = new Texture(Vars.tree.get(rayMaps.get(0)), true);
    }

    @Override
    public void dispose() {
      super.dispose();
      rayMap.dispose();
    }
  }

  public static class FastBlackholeShaderR2 extends FastBlackholeShaderRayMapBase {

    public Texture rayMap;

    @Override
    public String addon() {
      return "universe/fast_blackhole_2.frag";
    }

    @Override
    public void apply() {
      super.apply();
      rayMap.bind(2);
      setUniformi("u_ray_map", 2);
      Gl.activeTexture(Gl.texture0);
    }

    public void parse(String name, String mod, JsonValue data, FastBlackhole p) throws Exception {
      super.parse(name, mod, data, p);
      if (rayMaps.size != 1)
        Log.warn("level @ only need 1 rayMap get @", p.level, rayMaps.size);
      rayMap = new Texture(Vars.tree.get(rayMaps.get(0)), true);
    }

    @Override
    public void dispose() {
      super.dispose();
      rayMap.dispose();
    }
  }

  public static class FastBlackholeShaderR0 extends FastBlackholeBase {
    public float radius;
    public BlackholeRayData rayData;

    public FastBlackholeShaderR0() {
      super("universe/fast_blackhole_0", "screen");
    }

    @Override
    public void apply() {
      super.apply();
      setUniformf("u_radius_2", radius * radius);
      rayData.apply(this);
      Gl.activeTexture(Gl.texture0);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, FastBlackhole p) throws Exception {
      radius = p.radius;
      if (data.has("rayData")) {
        this.rayData = POPVars.mod.parser.parser.readValue(BlackholeRayData.class, data.get("rayData"));
        data.remove("rayData");
      } else {
        this.rayData = new BlackholeRayData();
      }
    }
  }

  public static class RayTestShader extends POLoadShader {
    public Camera3D camera;
    public Vec2 resolution;

    public RayTestShader() {
      super("ray/test", "screen");
    }

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180) / 2.0)) * 2.0f);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", camera.position);
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
    }
  }

  public static abstract class FastAdiskBlackholeBase extends POLoadShader
      implements CustomizeChildParser<FastAdiskBlackhole> {
    public Camera3D camera;
    public Vec3 target;
    public Vec2 resolution;
    public Cubemap cubemap;
    public float startDistance, scl;

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180) / 2.0)) * 2.0f);
      setUniformf("u_start_distance", startDistance);
      setUniformf("u_start_distance_2", startDistance * startDistance);
      setUniformf("u_scl", scl);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", POGUtil.t34.set(camera.position).sub(target));
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
      setUniformf("u_time", Time.globalTime / 10f);
      cubemap.bind(0);
      setUniformi("u_cubemap", 0);
    }

    public FastAdiskBlackholeBase(String frag, String vert) {
      super(frag, vert);
    }

  }

  public static abstract class FastAdiskBlackholeShaderRayMapBase extends FastAdiskBlackholeBase {
    public BlackholeAdiskData adisk;
    public Seq<String> rayMaps;

    public abstract String addon();

    public FastAdiskBlackholeShaderRayMapBase() {
      super("universe/fast_blackhole_base", "screen");
    }

    @Override
    public void apply() {
      super.apply();
      adisk.apply(this);
    }

    @Override
    protected String preprocess(String source, boolean fragment) {
      return super.preprocess(
          source + (fragment ? POGShaders.getShaderFi(addon()).readString() : ""), fragment);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, FastAdiskBlackhole father)
        throws Exception {
      if (!data.has("rayMaps")) {
        throw new IllegalArgumentException("need ray map");
      }
      rayMaps = Seq.with(data.get("rayMaps").asStringArray());
      data.remove("rayMaps");
      if (rayMaps.isEmpty()) {
        throw new IllegalArgumentException("Blackhole need a ray map use rayMaps:[\"xxx\"] ");
      }
      if (data.has("adisk")) {
        this.adisk = POPVars.mod.parser.parser.readValue(BlackholeAdiskData.class, data.get("adisk"));
        data.remove("adisk");
      } else {
        this.adisk = new BlackholeAdiskData();
      }
    }
  }

  public static class FastAdiskBlackholeShaderR1 extends FastAdiskBlackholeShaderRayMapBase {

    public Texture rayMap;

    @Override
    public String addon() {
      return "universe/fast_adisk_blackhole_1.frag";
    }

    @Override
    public void apply() {
      super.apply();
      rayMap.bind(2);
      setUniformi("u_ray_map", 2);
      Gl.activeTexture(Gl.texture0);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, FastAdiskBlackhole p)
        throws Exception {
      super.parse(name, mod, data, p);
      if (rayMaps.size != 1)
        Log.warn("level @ only need 1 rayMap get @", p.level, rayMaps.size);
      rayMap = new Texture(Vars.tree.get(rayMaps.get(0)), true);
    }

    @Override
    public void dispose() {
      super.dispose();
      rayMap.dispose();
    }
  }

  public static class FastAdiskBlackholeShaderR2 extends FastAdiskBlackholeShaderRayMapBase {

    public Texture rayMap;
    public Texture colorMap;
    public float colorScl = 1.2f;

    @Override
    public String addon() {
      return "universe/fast_adisk_blackhole_2.frag";
    }

    @Override
    public void apply() {
      super.apply();
      setUniformf("u_color_scl", colorScl);
      rayMap.bind(2);
      setUniformi("u_ray_map", 2);
      colorMap.bind(3);
      setUniformi("u_color_map", 3);

      Gl.activeTexture(Gl.texture0);
    }

    public void parse(String name, String mod, JsonValue data, FastAdiskBlackhole p)
        throws Exception {
      super.parse(name, mod, data, p);
      if (rayMaps.size != 1)
        Log.warn("level @ only need 1 rayMap get @", p.level, rayMaps.size);
      rayMap = new Texture(Vars.tree.get(rayMaps.get(0)), true);
      if (data.has("colorScl")) {
        colorScl = data.getInt("colorScl");
        data.remove("colorScl");
      }

      if (!data.has("colorMap"))
        throw new IllegalArgumentException("must have colorMap");
      colorMap = new Texture(Vars.tree.get(data.getString("colorMap")), true);
      data.remove("colorMap");
    }

    @Override
    public void dispose() {
      super.dispose();
      rayMap.dispose();
      colorMap.dispose();
    }
  }

  public static class FastAdiskNoiseBlackholeShaderR2 extends FastAdiskBlackholeShaderRayMapBase {

    public Texture rayMap;
    public int adiskNoiseLOD1 = 2;
    public Texture noise;

    @Override
    public String addon() {
      return "universe/fast_adisk_blackhole_noise_2.frag";
    }

    @Override
    public void apply() {
      super.apply();
      rayMap.bind(2);
      setUniformi("u_ray_map", 2);
      setUniformi("u_adisk_noise_LOD_1", adiskNoiseLOD1);
      noise.bind(3);
      setUniformi("u_noise_tex", 3);

      Gl.activeTexture(Gl.texture0);
    }

    public void parse(String name, String mod, JsonValue data, FastAdiskBlackhole p)
        throws Exception {
      super.parse(name, mod, data, p);
      if (rayMaps.size != 1)
        Log.warn("level @ only need 1 rayMap get @", p.level, rayMaps.size);
      rayMap = new Texture(Vars.tree.get(rayMaps.get(0)), true);
      if (data.has("noiseLOD1")) {
        adiskNoiseLOD1 = data.getInt("noiseLOD1");
        data.remove("noiseLOD1");
      }

      if (!data.has("noise"))
        throw new IllegalArgumentException("must have noise");
      noise = new Texture(Vars.tree.get(data.getString("noise")), true);
      data.remove("noise");
    }

    @Override
    public void dispose() {
      super.dispose();
      rayMap.dispose();
      noise.dispose();
    }
  }

  public static class TAABlackholeShader extends POLoadShader implements CustomizeChildParser<TAABlackhole> {
    public Camera3D camera;
    public Vec3 target;
    public Vec2 resolution;
    public Cubemap cubemap;
    public float startDistance;
    public TAABlackholeData data;
    public Texture backbuffer, noise;

    public TAABlackholeShader() {
      super("universe/taablackhole", "screen");
    }

    @Override
    protected String preprocess(String source, boolean fragment) {
      return super.preprocess(source, fragment).replace(
          "out" + (Core.app.isMobile() ? " lowp" : "") + " vec4 fragColor;\n", "");
    }

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180) / 2.0)) * 2f);
      setUniformf("u_start_distance", startDistance);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", POGUtil.t34.set(camera.position).sub(target));
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
      if (backbuffer != null)
        backbuffer.bind(0);
      setUniformi("u_backbuffer", 0);
      noise.bind(1);
      setUniformi("u_noise", 1);
      cubemap.bind(2);
      setUniformi("u_cubemap", 2);
      setUniformf("u_time", Time.globalTime / 10f);
      data.apply(this);
      Gl.activeTexture(Gl.texture0);
    }

    @Override
    public void parse(String name, String mod, JsonValue data, TAABlackhole father)
        throws Exception {
      if (!data.has("noise"))
        throw new IllegalArgumentException("must have noise");
      noise = new Texture(Vars.tree.get(data.getString("noise")), true);
      data.remove("noise");

    }

    @Override
    public void dispose() {
      super.dispose();
      noise.dispose();
    }

  }

  public static class TAABlackholeComposite extends POLoadShader {
    public TAABlackholeData data;
    public Texture input, background;
    public Camera3D camera;
    public Vec3 target;
    public Vec2 resolution;

    public TAABlackholeComposite() {
      super("universe/taablackhole_composite", "screen");
    }

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180) / 2.0)) * 2.0f);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", POGUtil.t34.set(camera.position).sub(target));
      setUniformMatrix("u_camera_mat_inv", POGUtil.getCamMat(camera).inv());
      input.bind(0);
      setUniformi("u_input", 0);
      background.bind(1);
      setUniformi("u_background", 1);
      data.applyComp(this);
      Gl.activeTexture(Gl.texture0);
    }
  }
}
