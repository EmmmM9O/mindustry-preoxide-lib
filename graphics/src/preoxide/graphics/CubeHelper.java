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
