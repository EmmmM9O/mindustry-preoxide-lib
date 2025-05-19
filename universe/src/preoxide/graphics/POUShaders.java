/* (C) 2025 */
package preoxide.graphics;

import arc.graphics.*;
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
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180)) / 2.0));
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
        this.adisk =
            POPVars.mod.parser.parser.readValue(BlackholeAdiskData.class, data.get("adisk"));
        data.remove("adisk");
      } else {
        this.adisk = new BlackholeAdiskData();
      }
    }
  }

  public static class Gas2NoiseBlackhole extends NoiseBlackholeBase {
    public int adiskNoiseLOD1 = 4, adiskNoiseLOD2 = 6;
    public Texture noise, colorMap;

    @Override
    public void apply() {
      super.apply();
      setUniformi("u_adisk_noise_LOD_1", adiskNoiseLOD1);
      setUniformi("u_adisk_noise_LOD_2", adiskNoiseLOD2);
      noise.bind(3);
      setUniformi("u_noise_tex", 3);
      colorMap.bind(4);
      setUniformi("u_color_map", 4);
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
      if (!data.has("colorMap"))
        throw new IllegalArgumentException("must have colorMap");
      colorMap = new Texture(Vars.tree.get(data.getString("colorMap")), true);
      data.remove("colorMap");
    }

    @Override
    public void dispose() {
      super.dispose();
      noise.dispose();
      colorMap.dispose();
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
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180)) / 2.0));
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
        this.rayData =
            POPVars.mod.parser.parser.readValue(BlackholeRayData.class, data.get("rayData"));
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
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180)) / 2.0));
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", camera.position);
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
    }
  }

}
