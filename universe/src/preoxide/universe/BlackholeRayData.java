/* (C) 2025 */
package preoxide.universe;

import arc.graphics.gl.*;

public class BlackholeRayData {
  public float lenScale = 1.0f, maxDistance = 30f, stepSize = 0.15f;
  public int maxSteps = 200;
  public float maxScl = 1.0f, minScl = 1.0f, sclR = 10f, sclT = 0.5f;

  public void apply(Shader s) {
    s.setUniformf("u_len_scl", lenScale);
    s.setUniformf("u_max_distance_2", maxDistance * maxDistance);
    s.setUniformf("u_step_size", stepSize);
    s.setUniformi("u_max_steps", maxSteps);
    s.setUniformf("u_max_scl", maxScl);
    s.setUniformf("u_min_scl", minScl);
    s.setUniformf("u_scl_r", sclR);
    s.setUniformf("u_scl_t", sclT);
  }

  public BlackholeRayData() {}
}
