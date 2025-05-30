/* (C) 2025 */
package preoxide.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import preoxide.graphics.gl.HDRFrameBuffer;

public class POGUtil {
  public static int width;
  public static int height;
  public static FrameBufferCubemap cubeBuffer;
  public static CubeHelper cubeHelper;
  public static final Vec3 t31 = new Vec3();
  public static final Vec3 t32 = new Vec3();
  public static final Vec3 t33 = new Vec3();
  public static final Vec3 t34 = new Vec3();
  public static final Vec3 t35 = new Vec3();
  public static final Vec2 t21 = new Vec2();
  public static int cubeSize = 1024;
  private static ScreenQuad quad;
  private static CamPlane camPlane;
  private static HDRFrameBuffer frameBuffer;

  public static HDRFrameBuffer getFrameBuffer() {
    if (frameBuffer == null)
      frameBuffer = new HDRFrameBuffer(Core.graphics.getWidth(),
          Core.graphics.getHeight(), true);
    return frameBuffer;
  }

  public static ScreenQuad getQuad() {
    if (quad == null)
      quad = new ScreenQuad();
    return quad;
  }

  public static CamPlane getPlane() {
    if (camPlane == null)
      camPlane = new CamPlane();
    return camPlane;
  }

  public static Cubemap getCube(Camera3D cam, Vec3 pos, Runnable render) {
    if (cubeBuffer == null) {
      cubeBuffer = new FrameBufferCubemap(Format.rgba8888, cubeSize, cubeSize, false);
      cubeHelper = new CubeHelper(cubeBuffer);
    }
    var tpos = t31.set(cam.position);
    var up = t32.set(cam.up);
    var dir = t33.set(cam.direction);
    var fov = cam.fov;
    var w = cam.width;
    var h = cam.height;
    cam.fov = 90f;
    cam.resize(cubeSize, cubeSize);
    cam.position.set(pos);
    cubeHelper.begin();
    while (cubeHelper.nextSide()) {
      cubeHelper.getSide().getUp(cam.up);
      cubeHelper.getSide().getDirection(cam.direction);
      cam.update();
      render.run();
    }
    cubeHelper.end();
    cam.resize(w, h);
    cam.position.set(tpos);
    cam.up.set(up);
    cam.direction.set(dir);
    cam.fov = fov;
    return cubeBuffer.getTexture();
  }

  public static Fi getCubeMapT(String path) {
    return Vars.tree.get("cubemaps/" + path);
  }

  public static Cubemap getCubeMap(String base) {
    return new Cubemap(getCubeMapT(base + "right.png"), getCubeMapT(base + "left.png"),
        getCubeMapT(base + "top.png"), getCubeMapT(base + "bottom.png"),
        getCubeMapT(base + "front.png"), getCubeMapT(base + "back.png"));
  }

  public static Mat getCamMat(Camera3D cam) {
    Mat mat = new Mat();
    Vec3 dir = t31.set(cam.direction).nor();
    Vec3 right = t32.set(cam.up).crs(dir).nor();
    Vec3 up = t33.set(dir).crs(right).nor();
    mat.val[Mat.M00] = right.x;
    mat.val[Mat.M01] = right.y;
    mat.val[Mat.M02] = right.z;
    mat.val[Mat.M10] = up.x;
    mat.val[Mat.M11] = up.y;
    mat.val[Mat.M12] = up.z;
    mat.val[Mat.M20] = dir.x;
    mat.val[Mat.M21] = dir.y;
    mat.val[Mat.M22] = dir.z;
    return mat;
  }

  public static void init() {
    width = Core.graphics.getWidth();
    height = Core.graphics.getHeight();
    Events.on(ResizeEvent.class, e -> {
      width = Core.graphics.getWidth();
      height = Core.graphics.getHeight();
      if (frameBuffer != null)
        frameBuffer.resize(width, height);
    });
  }
}
