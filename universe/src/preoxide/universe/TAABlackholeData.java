package preoxide.universe;

import arc.graphics.gl.*;

public class TAABlackholeData {
  public float speed = 0.05f, blendWeight = 0.7f;
  public int maxSteps = 100, LOD1 = 4, LOD2 = 6;
  public float flareScl = 1f, len = 3f;

  public void apply(Shader shader) {
    shader.setUniformi("u_max_steps", maxSteps);
    shader.setUniformi("u_LOD_1", LOD1);
    shader.setUniformi("u_LOD_2", LOD2);

    shader.setUniformf("u_speed", speed);
    shader.setUniformf("u_blend_weight", blendWeight);
  }

  public void applyComp(Shader shader) {
    shader.setUniformf("u_len", len);
    shader.setUniformf("u_flare_scl", flareScl);
  }
}
