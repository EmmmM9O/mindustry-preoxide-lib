/* (C) 2025 */
package preoxide;

import arc.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.*;
import preoxide.graphics.*;
import preoxide.mod.*;
import preoxide.ui.*;

public class POUniverseMain extends Mod {
  public static POPlanetRenderer planetRenderer;
  public static POPlanetDialog planet;

  public POUniverseMain() {
    Events.on(FileTreeInitEvent.class, e -> {
      POPlanetParser.init();
    });
  }

  @Override
  public void init() {
    Log.info("Preoxide Universe Lib Load");
    planetRenderer = new POPlanetRenderer();
    planet = new POPlanetDialog();
    setupUI();
  }

  public void setupUI() {
    var it = Vars.ui.planet;
    it.setStyle(new Dialog.DialogStyle() {
      {
        this.background = Styles.none;
        this.titleFont = Fonts.def;
      }
    });
    it.shown(() -> {
      it.visible = false;
      it.touchable = Touchable.disabled;
      Core.app.post(() -> {
        it.hide(null);
        planet.show();
      });
    });
  }
}
