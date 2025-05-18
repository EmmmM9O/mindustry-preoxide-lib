/* (C) 2025 */
package preoxide;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import preoxide.graphics.*;

public class POGraphicsMain extends Mod {
  public POGraphicsMain() {
    POGUtil.init();
    POMeshs.init();
    Events.on(FileTreeInitEvent.class, e -> {
      Log.info("Preoxide Graphics Lib Loading");
    });
  }

  @Override
  public void init() {
    POGShaders.init();
  }
}
