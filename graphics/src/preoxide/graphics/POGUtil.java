/* (C) 2025 */
package preoxide.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Pixmap.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;

public class POGUtil {
  public static int width;
  public static int height;
  public static FrameBufferCubemap cubeBuffer;
  public static CubeHelper cubeHelper;
  public static final Vec3 t31 = new Vec3();
  public static final Vec3 t32 = new Vec3();
  public static final Vec3 t33 = new Vec3();
  public static int cubeSize = 512;

  public static Cubemap getCube(Camera3D cam, Vec3 pos, Runnable render) {
    if (cubeBuffer == null) {
      cubeBuffer = new FrameBufferCubemap(Format.rgba8888, cubeSize, cubeSize, false);
      cubeHelper = new CubeHelper(cubeBuffer);
    }
    var tpos = Tmp.v34.set(cam.position);
    var up = t31.set(cam.up);
    var dir = t32.set(cam.direction);
    var fov = cam.fov;
    cam.fov = 45f;
    cam.position.set(pos);
    cubeHelper.begin();
    while (cubeHelper.nextSide()) {
      cubeHelper.getSide().getUp(cam.up);
      cubeHelper.getSide().getDirection(cam.direction);
      cam.update();
      render.run();
    }
    cubeHelper.end();
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
    Vec3 dir = cam.direction.cpy().nor();
    Vec3 right = cam.up.cpy().crs(dir).nor();
    Vec3 up = dir.cpy().crs(right).nor();
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
    });
  }
}
