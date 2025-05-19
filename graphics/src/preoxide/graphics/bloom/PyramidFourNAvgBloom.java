/* (C) 2025 */
package preoxide.graphics.bloom;

import preoxide.graphics.POGShaders.*;

public class PyramidFourNAvgBloom extends
    PyramidBloom<BloomBrightness, BloomComposite, BloomUpsample, BloomDownsample, BloomTonemapping> {
  public PyramidFourNAvgBloom(int width, int height) {
    super(width, height);
  }

  public PyramidFourNAvgBloom(int width, int height, boolean depth) {
    super(width, height, depth);
  }

  @Override
  BloomBrightness createBrightness() {
    return new BloomBrightness();
  }

  @Override
  BloomComposite createComposite() {
    return new BloomComposite();
  }

  @Override
  BloomUpsample createUpsample() {
    return new BloomUpsample("bloom/fnavg_upsample", "screen");
  }

  @Override
  BloomDownsample createDownsample() {
    return new BloomDownsample("bloom/fnavg_downsample", "screen");
  }

  @Override
  BloomTonemapping createTonemapping() {
    return new BloomTonemapping();
  }
}
