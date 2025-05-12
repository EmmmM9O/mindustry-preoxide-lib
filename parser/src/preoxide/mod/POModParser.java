/* (C) 2025 */
package preoxide.mod;

import static mindustry.Vars.*;

import java.util.*;

import arc.*;
import arc.files.*;
import arc.struct.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.Mods.*;
import mindustry.type.*;

public class POModParser {
  public static final String contentDir = "pocontent";
  public POContentParser parser = new POContentParser();

  public void init() {
    Events.on(ModContentLoadEvent.class, event -> {
      this.loadContent();
    });
  }

  public void loadContent() {
    Log.info("PO Load Content");
    parser.init();
    content.setCurrentMod(null);
    class LoadRun implements Comparable<LoadRun> {
      final ContentType type;
      final Fi file;
      final LoadedMod mod;

      public LoadRun(ContentType type, Fi file, LoadedMod mod) {
        this.type = type;
        this.file = file;
        this.mod = mod;
      }

      @Override
      public int compareTo(LoadRun l) {
        int mod = this.mod.name.compareTo(l.mod.name);
        if (mod != 0)
          return mod;
        return this.file.name().compareTo(l.file.name());
      }
    }

    Seq<LoadRun> runs = new Seq<>();

    for (LoadedMod mod : mods.orderedMods()) {
      Seq<LoadRun> unorderedContent = new Seq<>();
      ObjectMap<String, LoadRun> orderedContent = new ObjectMap<>();
      String[] contentOrder = mod.meta.contentOrder;
      ObjectSet<String> orderSet = contentOrder == null ? null : ObjectSet.with(contentOrder);

      if (mod.root.child(contentDir).exists()) {
        Fi contentRoot = mod.root.child(contentDir);
        for (ContentType type : ContentType.all) {
          String lower = type.name().toLowerCase(Locale.ROOT);
          Fi folder = contentRoot.child(lower + (lower.endsWith("s") ? "" : "s"));
          if (folder.exists()) {
            for (Fi file : folder.findAll(f -> f.extension().equals("json")
                || f.extension().equals("hjson"))) {

              // if this is part of the ordered content, put it aside to be dealt with later
              if (orderSet != null && orderSet.contains(file.nameWithoutExtension())) {
                orderedContent.put(file.nameWithoutExtension(), new LoadRun(type, file, mod));
              } else {
                unorderedContent.add(new LoadRun(type, file, mod));
              }
            }
          }
        }
      }

      // ordered content will be loaded first, if it exists
      if (contentOrder != null) {
        for (String contentName : contentOrder) {
          LoadRun run = orderedContent.get(contentName);
          if (run != null) {
            runs.add(run);
          } else {
            Log.warn("Cannot find content defined in contentOrder: @", contentName);
          }
        }
      }

      // unordered content is sorted alphabetically per mod
      runs.addAll(unorderedContent.sort());
    }

    for (LoadRun l : runs) {
      Content current = content.getLastAdded();
      try {
        // this binds the content but does not load it entirely
        Content loaded = parser.parse(l.mod, l.file.nameWithoutExtension(),
            l.file.readString("UTF-8"), l.file, l.type);
        Log.debug("[@] Loaded '@'.", l.mod.meta.name,
            (loaded instanceof UnlockableContent u ? u.localizedName : loaded));
      } catch (Throwable e) {
        if (current != content.getLastAdded() && content.getLastAdded() != null) {
          parser.markError(content.getLastAdded(), l.mod, l.file, e);
        } else {
          ErrorContent error = new ErrorContent();
          parser.markError(error, l.mod, l.file, e);
        }
      }
    }

    // this finishes parsing content fields
    parser.finishParsing();

  }
}
