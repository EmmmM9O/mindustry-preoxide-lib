
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
import arc.math.*;

public class POMeshBuilder {
  private static Mesh tmp;

  public static Mesh screenMesh() {
    tmp = new Mesh(true, 4, 6, VertexAttribute.position);
    tmp.setVertices(new float[] { //
        -1f, -1f, //
        -1f, 1f, //
        1f, 1f, //
        1f, -1f,//
    });
    tmp.setIndices(new short[] {0, 1, 2, 0, 2, 3});
    return end();
  }

  public static Mesh toursMesh(float innerRadius, float outerRadius, float y, int segments) {

    int verticesPerSegment = 2;
    int vertexCount = (segments + 1) * verticesPerSegment;
    int indicesCount = (segments + 1) * 2;

    float[] vertices = new float[vertexCount * 3];
    short[] indices = new short[indicesCount];

    for (int i = 0; i <= segments; i++) {
      float angle = Mathf.PI2 * i / segments;
      float xInner = innerRadius * Mathf.cos(angle);
      float zInner = innerRadius * Mathf.sin(angle);
      float xOuter = outerRadius * Mathf.cos(angle);
      float zOuter = outerRadius * Mathf.sin(angle);
      vertices[i * 6] = xInner;
      vertices[i * 6 + 1] = y;
      vertices[i * 6 + 2] = zInner;
      vertices[i * 6 + 3] = xOuter;
      vertices[i * 6 + 4] = y;
      vertices[i * 6 + 5] = zOuter;
    }
    for (int i = 0; i < segments; i++) {
      indices[i * 2] = (short) (i * 2);
      indices[i * 2 + 1] = (short) (i * 2 + 1);
    }
    tmp = new Mesh(true, vertexCount, indicesCount, VertexAttribute.position3);
    tmp.setVertices(vertices);
    tmp.setIndices(indices);
    return end();
  }

  public static Mesh end() {
    Mesh last = tmp;
    tmp = null;
    return last;
  }
}
