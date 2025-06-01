
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
package preoxide.graphics.bloom;

import arc.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.g2d.Draw;
import arc.graphics.gl.*;
import arc.util.*;
import preoxide.graphics.*;
import preoxide.graphics.gl.*;
import preoxide.graphics.POGShaders.*;

public abstract class PyramidBloom<Brightness extends BloomBrightness, Composite extends BloomComposite, Upsample extends BloomUpsample, Downsample extends BloomDownsample, Tonemapping extends BloomTonemapping>
    extends CaptureBloom {
  public final int bloomIter = 9;
  public int bloomIterations = 9;
  public Brightness brightness;
  public Composite composite;
  public Upsample upsample;
  public Downsample downsample;
  public Tonemapping tonemapping;
  FrameBuffer fboBrightness;
  FrameBuffer[] fboDownsampled;
  FrameBuffer[] fboUpsampled;

  @Override
  public void dispose() {
    super.dispose();
    composite.dispose();
    brightness.dispose();
    upsample.dispose();
    downsample.dispose();
    fboBrightness.dispose();
    for (int i = 0; i < bloomIter; i++) {
      fboDownsampled[i].dispose();
      fboUpsampled[i].dispose();
    }
  }

  abstract Brightness createBrightness();

  abstract Composite createComposite();

  abstract Upsample createUpsample();

  abstract Downsample createDownsample();

  abstract Tonemapping createTonemapping();

  public PyramidBloom(Brightness brightness, Composite composite, Upsample upsample,
      Downsample downsample, Tonemapping tonemapping) {
    this(Core.graphics.getWidth(), Core.graphics.getHeight(), false, brightness, composite,
        upsample, downsample, tonemapping);
  }

  public PyramidBloom(int width, int height) {
    this(width, height, null, null, null, null, null);
  }

  public PyramidBloom(int width, int height, Brightness brightness, Composite composite,
      Upsample upsample, Downsample downsample, Tonemapping tonemapping) {
    this(width, height, false, brightness, composite, upsample, downsample, tonemapping);
  }

  public PyramidBloom(int width, int height, boolean hasDepth) {
    this(width, height, hasDepth, null, null, null, null, null);
  }

  public PyramidBloom(int width, int height, boolean hasDepth, Brightness brightness,
      Composite composite, Upsample upsample, Downsample downsample, Tonemapping tonemapping) {
    super();
    this.width = width;
    this.height = height;
    this.brightness = brightness;
    this.composite = composite;
    this.upsample = upsample;
    this.downsample = downsample;
    this.tonemapping = tonemapping;
    init(hasDepth);
  }

  @Override
  public void init(boolean hasDepth) {
    super.init(hasDepth);
    if (brightness == null)
      brightness = createBrightness();
    if (composite == null)
      composite = createComposite();
    if (upsample == null)
      upsample = createUpsample();
    if (downsample == null)
      downsample = createDownsample();
    if (tonemapping == null)
      tonemapping = createTonemapping();
    fboBrightness = new HDRFrameBuffer(width, height, false);
    fboDownsampled = new FrameBuffer[bloomIter];
    fboUpsampled = new FrameBuffer[bloomIter];
    for (int i = 0; i < bloomIter; i++) {
      fboDownsampled[i] = new HDRFrameBuffer(width >> (i + 1), height >> (i + 1), false);
      fboUpsampled[i] = new HDRFrameBuffer(width >> i, height >> i, false);
    }
  }

  void renderBloom(Texture input) {
    fboBrightness.begin();
    Gl.disable(Gl.depthTest);
    prework();
    brightness.resolution = Tmp.v2.set(width, height);
    brightness.input = input;
    Draw.blit(brightness);
    fboBrightness.end();
    for (int level = 0; level < bloomIterations; level++) {
      if (level == 0)
        downsample.input = fboBrightness.getTexture();
      else
        downsample.input = fboDownsampled[level - 1].getTexture();
      FrameBuffer current = fboDownsampled[level];
      current.begin();
      Gl.disable(Gl.depthTest);
      prework();
      downsample.resolution = Tmp.v2.set(width >> (level + 1), height >> (level + 1));

      Draw.blit(downsample);
      current.end();
    }
    for (int level = bloomIterations - 1; level >= 0; level--) {
      if (level == bloomIterations - 1)
        upsample.input = fboDownsampled[level].getTexture();
      else
        upsample.input = fboUpsampled[level + 1].getTexture();
      if (level == 0)
        upsample.addition = fboBrightness.getTexture();
      else
        upsample.addition = fboDownsampled[level - 1].getTexture();
      FrameBuffer current = fboUpsampled[level];
      current.begin();
      Gl.disable(Gl.depthTest);
      prework();
      upsample.resolution = Tmp.v2.set(width >> level, height >> level);

      Draw.blit(upsample);
      current.end();
    }
  }

  void bloomComposite(Texture input) {
    Gl.disable(Gl.depthTest);
    prework();
    composite.input = input;
    composite.bloom = fboUpsampled[0].getTexture();
    Draw.blit(composite);

  }

  void tonemapping(Texture texture) {
    tonemapping.input = texture;
    Draw.blit(tonemapping);
  }

  @Override
  public void render(Texture texture) {
    renderBloom(texture);
    fboCapture.begin();
    prework();
    bloomComposite(texture);
    fboCapture.end();
    tonemapping(fboCapture.getTexture());
  }

  @Override
  public void renderTo(FrameBuffer src, FrameBuffer dest) {
    Texture texture = src.getTexture();
    renderBloom(texture);
    fboCapture.begin();
    prework();
    bloomComposite(texture);
    fboCapture.end();
    dest.begin();
    tonemapping(fboCapture.getTexture());
    dest.end();
  }

  @Override
  public void onResize() {
    super.onResize();
    fboBrightness.resize(width, height);
    for (int i = 0; i < bloomIter; i++) {
      fboDownsampled[i].resize(width, height);
      fboUpsampled[i].resize(width, height);
    }
  }

}
