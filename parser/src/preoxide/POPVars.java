/* (C) 2025 */
package preoxide;

import preoxide.mod.*;

public class POPVars {
  public static POModParser mod;

  public static void init() {
    mod = new POModParser();
    mod.init();
  }
}
