
        /*
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
        */
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
