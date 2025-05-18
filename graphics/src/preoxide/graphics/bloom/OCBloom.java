/* (C) 2025 */
package preoxide.graphics.bloom;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;
import preoxide.graphics.postprocessing.*;
import preoxide.util.*;

public abstract class OCBloom implements Disposable, ProcessorEffect, BufferCapturable, Resizeable {
  protected boolean enabled = true;

  public abstract void init(boolean hasDepth);

  public abstract void render();

  public abstract void renderTo(FrameBuffer src);

  public abstract void render(Texture texture);

  @Override
  public boolean isEnabled() {
    return enabled;
  }

  @Override
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

}
