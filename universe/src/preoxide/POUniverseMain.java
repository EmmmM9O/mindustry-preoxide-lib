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
import preoxide.universe.*;

public class POUniverseMain extends Mod {
  public static POPlanetRenderer planetRenderer;
  public static POPlanetDialog planet;

  public POUniverseMain() {
    Events.on(FileTreeInitEvent.class, e -> {
      POPlanetParser.init();
    });
    Events.on(ResizeEvent.class, e -> {
      for (var planet : Vars.content.planets()) {
        if (planet instanceof CustomizeRenderI cRender) {
          cRender.onResize();
        }
      }
    });
  }

  @Override
  public void init() {
    Log.info("Preoxide Universe Lib Load");
    POUShaders.init();
    planetRenderer = new POPlanetRenderer();
    planet = new POPlanetDialog();
    setupUI();
    Vars.ui.menufrag.addButton("test", planet::show);
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
