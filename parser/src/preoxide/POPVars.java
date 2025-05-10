/* (C) 2025 */
package preoxide;

import preoxide.mod.*;

public class POPVars {
  public static POModParser parser;

  public static void init() {
    parser = new POModParser();
    parser.init();
  }
}
