/* (C) 2025 */
package preoxide.universe;

import arc.graphics.gl.*;

public class BlackholeAdiskData {
  public float adiskSpeed = 0.03f, adiskLit = 0.88f, innerRadius = 2.6f, outerRadius = 12.6f,
      noiseScale = 3f, thickness = 0.20f, coverageLit = 0.75f, lit = 1.2f, maxLight = 1.2f;

  public void apply(Shader s) {
    s.setUniformf("u_adisk_inner_radius", innerRadius);
    s.setUniformf("u_adisk_outer_radius", outerRadius);
    s.setUniformf("u_adisk_speed", adiskSpeed);
    s.setUniformf("u_adisk_lit", adiskLit);
    s.setUniformf("u_lit", lit);
    s.setUniformf("u_adisk_noise_scale", noiseScale);
    s.setUniformf("u_adisk_thickness", thickness);
    s.setUniformf("u_coverage_lit", coverageLit);
    s.setUniformf("u_max_light", maxLight);

  }
}
