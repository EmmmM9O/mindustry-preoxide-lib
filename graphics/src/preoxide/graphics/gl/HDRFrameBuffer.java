
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
package preoxide.graphics.gl;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.gl.*;

public class HDRFrameBuffer extends FrameBuffer {
  public HDRFrameBuffer() {

  }

  protected HDRFrameBuffer(GLFrameBufferBuilder<? extends GLFrameBuffer<Texture>> bufferBuilder) {
    super(bufferBuilder);
  }

  public HDRFrameBuffer(int width, int height, boolean hasDepth) {
    HDRFrameBufferBuilder bufferBuilder = new HDRFrameBufferBuilder(width, height);
    if (hasDepth)
      bufferBuilder.addBasicDepthRenderBuffer();
    bufferBuilder.addFloatAttachment(GL30.GL_RGBA16F, GL30.GL_RGBA, GL30.GL_FLOAT, false);
    this.textureAttachments.clear();
    this.framebufferHandle = 0;
    this.depthbufferHandle = 0;
    this.stencilbufferHandle = 0;
    this.depthStencilPackedBufferHandle = 0;
    this.hasDepthStencilPackedBuffer = this.isMRT = false;
    this.bufferBuilder = bufferBuilder;
    build();
  }

  @Override
  protected Texture createTexture(FrameBufferTextureAttachmentSpec attachmentSpec) {
    Texture result = super.createTexture(attachmentSpec);
    if (Core.app.isDesktop())
      result.setFilter(TextureFilter.linear, TextureFilter.linear);
    else
      result.setFilter(TextureFilter.nearest, TextureFilter.nearest);
    return result;
  }

  @Override
  public void resize(int width, int height) {
    if (width == getWidth() && height == getHeight())
      return;

    TextureFilter min = getTexture().getMinFilter(), mag = getTexture().getMagFilter();
    boolean hasDepth = depthbufferHandle != 0, hasStencil = stencilbufferHandle != 0;
    dispose();

    HDRFrameBufferBuilder frameBufferBuilder = new HDRFrameBufferBuilder(width, height);
    frameBufferBuilder.addFloatAttachment(GL30.GL_RGBA16F, GL30.GL_RGBA, GL30.GL_FLOAT, false);
    if (hasDepth)
      frameBufferBuilder.addBasicDepthRenderBuffer();
    if (hasStencil)
      frameBufferBuilder.addBasicStencilRenderBuffer();
    this.bufferBuilder = frameBufferBuilder;
    this.textureAttachments.clear();
    this.framebufferHandle = 0;
    this.depthbufferHandle = 0;
    this.stencilbufferHandle = 0;
    this.depthStencilPackedBufferHandle = 0;
    this.hasDepthStencilPackedBuffer = this.isMRT = false;
    build();
    getTexture().setFilter(min, mag);
  };

  public static class HDRFrameBufferBuilder extends GLFrameBufferBuilder<HDRFrameBuffer> {
    public HDRFrameBufferBuilder(int width, int height) {
      super(width, height);
    }

    @Override
    public HDRFrameBuffer build() {
      return new HDRFrameBuffer(this);
    }
  }
}
