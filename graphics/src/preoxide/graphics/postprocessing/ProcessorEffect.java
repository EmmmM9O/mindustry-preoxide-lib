/* (C) 2025 */
package preoxide.graphics.postprocessing;

import arc.graphics.gl.*;
import preoxide.util.*;

public interface ProcessorEffect extends Enableable {
  public void renderTo(FrameBuffer src, FrameBuffer dest);
}
