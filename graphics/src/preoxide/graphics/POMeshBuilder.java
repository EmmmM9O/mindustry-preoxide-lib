/* (C) 2025 */
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
