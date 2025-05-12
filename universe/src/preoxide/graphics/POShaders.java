package preoxide.graphics;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;

public class POShaders {
  public static abstract class FastBlackholeShaderBase extends OCLoadShader {
    public Camera3D camera;
    public Vec3 target;
    public Vec2 resolution;
    public Cubemap cubemap;
    public float startDistance, radius;

    public abstract String addon();

    public FastBlackholeShaderBase() {
      super("universe/fast_blackhole_base", "screen");
    }

    @Override
    public void apply() {
      setUniformf("u_fov_scale", (float) Math.tan((camera.fov * (Math.PI / 180)) / 2.0));
      setUniformf("u_start_distance", startDistance);
      setUniformf("u_start_distance_2", startDistance * startDistance);
      setUniformf("u_radius_2", radius * radius);
      setUniformf("u_resolution", resolution);
      setUniformf("u_camera_pos", Tmp.v34.set(camera.position).sub(target));
      setUniformMatrix("u_camera_mat", POGUtil.getCamMat(camera));
      cubemap.bind(1);
      setUniformi("u_cubemap", 1);
    }

    @Override
    protected String preprocess(String source, boolean fragment) {
      return super.preprocess(source + (fragment ? getShaderFi(addon()).readString() : ""),
          fragment);
    }
  }

  public static class FastBlackholeShaderR1 extends FastBlackholeShaderBase {
    public Texture rayMap;

    @Override
    public String addon() {
      return "universe/fast_blackhole_1.frag";
    }

    @Override
    public void apply() {
      super.apply();
      rayMap.bind(0);
      setUniformi("u_ray_map", 0);
    }
  }

  public static class OCLoadShader extends Shader {
    public OCLoadShader(String frag, String vert) {
      super(getShaderFi(vert + ".vert"), getShaderFi(frag + ".frag"));
    }
  }

  public static Fi getShaderFi(String file) {
    return Vars.tree.get("shaders/" + file);
  }
}
