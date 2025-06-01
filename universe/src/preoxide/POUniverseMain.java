
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
