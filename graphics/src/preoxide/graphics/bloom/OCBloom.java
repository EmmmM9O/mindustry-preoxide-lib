
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
