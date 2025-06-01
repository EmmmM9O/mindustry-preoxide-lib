
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
