package preoxide.graphics;

import arc.files.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import mindustry.*;
import arc.graphics.*;
import arc.math.geom.*;

public class POGShaders {
  public static SphereShader sphere;
  public static ScreenShader screen;

  public static void init() {
    sphere = new SphereShader();
    screen = new ScreenShader();
  }

  public static class SphereShader extends POLoadShader {
    public Camera3D cam;

    public SphereShader() {
      super("sphere", "sphere");
    }

    @Override
    public void apply() {
      setUniformMatrix4("u_proj", cam.combined.val);
    }
  }

  public static class POLoadShader extends Shader {
    public POLoadShader(String frag, String vert) {
      super(getShaderFi(vert + ".vert"), getShaderFi(frag + ".frag"));
    }
  }

  public static Fi getShaderFi(String file) {
    return Vars.tree.get("shaders/" + file);
  }

  // bloom
  public static class BloomBrightness extends POLoadShader {
    public Texture input;
    public Vec2 resolution;
    public float threshold = 1.0f;

    public BloomBrightness() {
      super("bloom/bloom_brightness", "screen");
    }

    public BloomBrightness(String frag, String vert) {
      super(frag, vert);
    }

    @Override
    public void apply() {
      setUniformf("resolution", resolution);
      input.bind(0);
      setUniformf("texture0", 0);
      setUniformf("threshold", threshold);
      Gl.activeTexture(Gl.texture0);
    }
  }

  public static class BloomComposite extends POLoadShader {
    public Texture input, bloom;
    public float intensity = 0.10f, exposure = 0.72f;

    public BloomComposite() {
      super("bloom/bloom_composite", "screen");
    }

    public BloomComposite(String frag, String vert) {
      super(frag, vert);
    }

    @Override
    public void apply() {
      setUniformf("intensity", intensity);
      setUniformf("exposure", exposure);
      input.bind(0);
      setUniformi("texture0", 0);
      bloom.bind(1);
      setUniformi("texture1", 1);
      Gl.activeTexture(Gl.texture0);
    }
  }

  public static class BloomDownsample extends POLoadShader {
    public Texture input;
    public Vec2 resolution;

    public BloomDownsample(String frag, String vert) {
      super(frag, vert);
    }

    @Override
    public void apply() {
      setUniformf("resolution", resolution);
      input.bind(0);
      setUniformi("texture0", 0);
      Gl.activeTexture(Gl.texture0);
    }
  }

  public static class BloomUpsample extends POLoadShader {
    public Texture input, addition;
    public Vec2 resolution;

    public BloomUpsample(String frag, String vert) {
      super(frag, vert);
    }

    @Override
    public void apply() {
      setUniformf("resolution", resolution);
      input.bind(0);
      setUniformi("texture0", 0);
      addition.bind(1);
      setUniformi("texture1", 1);
      Gl.activeTexture(Gl.texture0);
    }
  }

  public static class BloomTonemapping extends POLoadShader {
    public boolean enabled = true;
    public float gamma = 2.2f;
    public Texture input;

    public BloomTonemapping() {
      this("bloom/bloom_tonemapping", "screen");
    }

    public BloomTonemapping(String frag, String vert) {
      super(frag, vert);
    }

    @Override
    public void apply() {
      setUniformf("gamma", gamma);
      setUniformf("enabled", enabled ? 1.0f : 0.0f);
      input.bind(0);
      setUniformf("texture0", 0);
    }
  }

  public static class ScreenShader extends POLoadShader {
    public Texture input;

    public ScreenShader() {
      super("screen", "screen");
    }

    @Override
    public void apply() {
      input.bind(0);
      setUniformf("texture0", 0);
    }
  }
}
