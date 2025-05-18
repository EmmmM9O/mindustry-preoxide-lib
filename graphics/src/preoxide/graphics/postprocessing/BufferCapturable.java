/* (C) 2025 */
package preoxide.graphics.postprocessing;

public interface BufferCapturable {
  public abstract void capture();

  public abstract void capturePause();

  public abstract void captureContinue();
}
