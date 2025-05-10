/* (C) 2025 */
package preoxide.mod;

import arc.files.*;
import mindustry.ctype.*;
import mindustry.mod.Mods.*;

public interface ContentParserI {
  public void init();

  public Content parse(LoadedMod mod, String name, String json, Fi file, ContentType type)
      throws Exception;

  public void finishParsing();

  public void markError(Content content, LoadedMod mod, Fi file, Throwable error);

  public void markError(Content content, Throwable error);

  public void addParseListener(POParseListener listener);

}
