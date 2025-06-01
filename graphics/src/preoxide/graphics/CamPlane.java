
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
package preoxide.graphics;

import arc.graphics.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import preoxide.math.*;
import preoxide.graphics.POGShaders.*;

public class CamPlane implements Disposable {
  public Texture texture;
  public Vec3 position = Vec3.Zero;
  public Mesh mesh;
  public Shader shader;

  public CamPlane() {
    createMesh();
    shader = new POLoadShader("camplane", "camplane");
  }

  public void render(Camera3D camera) {
    var pos = POGUtil.t34.set(camera.position).scl(-1);
    Mat3D tran = new Mat3D().setToTranslation(position);
    MathUtils.rotateTowardDirection(tran, pos, Vec3.Y);
    float distance = camera.position.dst(position);
    float height = 2 * (float) Math.tan(Math.toRadians(camera.fov / 2)) * distance;
    float width = height * camera.width / camera.height;
    tran.scale(width, height, 1);
    Gl.enable(Gl.depthTest);
    Gl.enable(Gl.blend);
    Gl.blendFunc(Gl.srcAlpha, Gl.oneMinusSrcAlpha);
    Gl.depthMask(true);
    shader.bind();
    shader.setUniformMatrix4("u_proj", camera.combined.val);
    shader.setUniformMatrix4("u_trans", tran.val);
    texture.bind();
    shader.setUniformi("u_texture0", 0);
    mesh.render(shader, Gl.triangles);
    Gl.disable(Gl.depthTest);
  }

  public void createMesh() {
    mesh = new Mesh(true, 4, 6, VertexAttribute.position3, VertexAttribute.texCoords);
    mesh.setVertices(new float[] {-0.5f, -0.5f, 0, 0, 0, // 左下
        0.5f, -0.5f, 0, 1, 0, // 右下
        0.5f, 0.5f, 0, 1, 1, // 右上
        -0.5f, 0.5f, 0, 0, 1});
    mesh.setIndices(new short[] {0, 1, 2, 2, 3, 0});
  }

  @Override
  public void dispose() {
    mesh.dispose();
    shader.dispose();
  }
}
