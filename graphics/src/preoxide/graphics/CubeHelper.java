
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

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;

public class CubeHelper {
  private int currentSide;
  private static final Cubemap.CubemapSide[] cubemapSides = Cubemap.CubemapSide.values();
  public FrameBufferCubemap frameBuffer;

  public CubeHelper(FrameBufferCubemap frameBuffer) {
    this.frameBuffer = frameBuffer;
  }

  public void begin() {
    currentSide = -1;
    frameBuffer.begin();
  }

  public void end() {
    frameBuffer.end();
  }

  public boolean nextSide() {
    if (currentSide > 5) {
      throw new ArcRuntimeException("No remaining sides.");
    } else if (currentSide == 5) {
      return false;
    }

    currentSide++;
    frameBuffer.bindSide(getSide());
    return true;
  }

  public Cubemap.CubemapSide getSide() {
    return currentSide < 0 ? null : cubemapSides[currentSide];
  }
}
