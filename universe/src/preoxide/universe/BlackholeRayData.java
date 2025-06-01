
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
