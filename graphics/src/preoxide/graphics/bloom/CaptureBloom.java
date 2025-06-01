
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

import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.gl.*;
import preoxide.graphics.gl.*;

public abstract class CaptureBloom extends OCBloom {
  int width;
  int height;
  float r, g, b, a = 1.0f;
  public boolean capturing;
  FrameBuffer fboCapture;

  public CaptureBloom() {}

  public void setClearColor(float r, float g, float b, float a) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.a = a;
  }

  void prework() {
    Gl.clearColor(r, g, b, a);
    Gl.clear(Gl.colorBufferBit | Gl.depthBufferBit);
  }

  @Override
  public void capture() {
    if (!capturing) {
      capturing = true;
      fboCapture.begin();
      prework();
    }
  }

  @Override
  public void capturePause() {
    if (capturing) {
      capturing = false;
      fboCapture.end();
    }
  }

  @Override
  public void captureContinue() {
    if (!capturing) {
      capturing = true;
      fboCapture.begin();
    }
  }

  @Override
  public void dispose() {
    fboCapture.dispose();
  }

  @Override
  public void init(boolean hasDepth) {
    fboCapture = new HDRFrameBuffer(width, height, hasDepth);
  }

  @Override
  public void render() {
    capturePause();
    render(fboCapture.getTexture());
  }

  @Override
  public void renderTo(FrameBuffer src) {
    capturePause();
    renderTo(fboCapture, src);
  }

  @Override
  public void resize(int width, int height) {
    this.width = width;
    this.height = height;
    onResize();
  }

  @Override
  public void onResize() {
    fboCapture.resize(width, height);
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }
}
