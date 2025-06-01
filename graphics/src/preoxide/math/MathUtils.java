
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
package preoxide.math;

import arc.math.geom.*;

public class MathUtils {
  static final Vec3 l_vez = new Vec3();
  static final Vec3 l_vex = new Vec3();
  static final Vec3 l_vey = new Vec3();
  public static final int M00 = 0;
  /**
   * XY: Typically the negative sine of the angle when rotated on the Z axis. On Vector3
   * multiplication this value is multiplied with the source Y component and added to the target X
   * component.
   */
  public static final int M01 = 4;
  /**
   * XZ: Typically the sine of the angle when rotated on the Y axis. On Vector3 multiplication this
   * value is multiplied with the source Z component and added to the target X component.
   */
  public static final int M02 = 8;
  /**
   * XW: Typically the translation of the X component. On Vector3 multiplication this value is added
   * to the target X component.
   */
  public static final int M03 = 12;
  /**
   * YX: Typically the sine of the angle when rotated on the Z axis. On Vector3 multiplication this
   * value is multiplied with the source X component and added to the target Y component.
   */
  public static final int M10 = 1;
  /**
   * YY: Typically the unrotated Y component for scaling, also the cosine of the angle when rotated
   * on the X and/or Z axis. On Vector3 multiplication this value is multiplied with the source Y
   * component and added to the target Y component.
   */
  public static final int M11 = 5;
  /**
   * YZ: Typically the negative sine of the angle when rotated on the X axis. On Vector3
   * multiplication this value is multiplied with the source Z component and added to the target Y
   * component.
   */
  public static final int M12 = 9;
  /**
   * YW: Typically the translation of the Y component. On Vector3 multiplication this value is added
   * to the target Y component.
   */
  public static final int M13 = 13;
  /**
   * ZX: Typically the negative sine of the angle when rotated on the Y axis. On Vector3
   * multiplication this value is multiplied with the source X component and added to the target Z
   * component.
   */
  public static final int M20 = 2;
  /**
   * ZY: Typical the sine of the angle when rotated on the X axis. On Vector3 multiplication this
   * value is multiplied with the source Y component and added to the target Z component.
   */
  public static final int M21 = 6;
  /**
   * ZZ: Typically the unrotated Z component for scaling, also the cosine of the angle when rotated
   * on the X and/or Y axis. On Vector3 multiplication this value is multiplied with the source Z
   * component and added to the target Z component.
   */
  public static final int M22 = 10;
  /**
   * ZW: Typically the translation of the Z component. On Vector3 multiplication this value is added
   * to the target Z component.
   */
  public static final int M23 = 14;
  /**
   * WX: Typically the value zero. On Vector3 multiplication this value is ignored.
   */
  public static final int M30 = 3;
  /**
   * WY: Typically the value zero. On Vector3 multiplication this value is ignored.
   */
  public static final int M31 = 7;
  /**
   * WZ: Typically the value zero. On Vector3 multiplication this value is ignored.
   */
  public static final int M32 = 11;
  /**
   * WW: Typically the value one. On Vector3 multiplication this value is ignored.
   */
  public static final int M33 = 15;

  public static Mat3D rotateTowardDirection(Mat3D mat, Vec3 direction, Vec3 up) {
    l_vez.set(direction).nor();
    l_vex.set(direction).crs(up).nor();
    l_vey.set(l_vex).crs(l_vez).nor();
    float m00 = mat.val[M00] * l_vex.x + mat.val[M01] * l_vex.y + mat.val[M02] * l_vex.z;
    float m01 = mat.val[M00] * l_vey.x + mat.val[M01] * l_vey.y + mat.val[M02] * l_vey.z;
    float m02 = mat.val[M00] * -l_vez.x + mat.val[M01] * -l_vez.y + mat.val[M02] * -l_vez.z;
    float m10 = mat.val[M10] * l_vex.x + mat.val[M11] * l_vex.y + mat.val[M12] * l_vex.z;
    float m11 = mat.val[M10] * l_vey.x + mat.val[M11] * l_vey.y + mat.val[M12] * l_vey.z;
    float m12 = mat.val[M10] * -l_vez.x + mat.val[M11] * -l_vez.y + mat.val[M12] * -l_vez.z;
    float m20 = mat.val[M20] * l_vex.x + mat.val[M21] * l_vex.y + mat.val[M22] * l_vex.z;
    float m21 = mat.val[M20] * l_vey.x + mat.val[M21] * l_vey.y + mat.val[M22] * l_vey.z;
    float m22 = mat.val[M20] * -l_vez.x + mat.val[M21] * -l_vez.y + mat.val[M22] * -l_vez.z;
    float m30 = mat.val[M30] * l_vex.x + mat.val[M31] * l_vex.y + mat.val[M32] * l_vex.z;
    float m31 = mat.val[M30] * l_vey.x + mat.val[M31] * l_vey.y + mat.val[M32] * l_vey.z;
    float m32 = mat.val[M30] * -l_vez.x + mat.val[M31] * -l_vez.y + mat.val[M32] * -l_vez.z;
    mat.val[M00] = m00;
    mat.val[M10] = m10;
    mat.val[M20] = m20;
    mat.val[M30] = m30;
    mat.val[M01] = m01;
    mat.val[M11] = m11;
    mat.val[M21] = m21;
    mat.val[M31] = m31;
    mat.val[M02] = m02;
    mat.val[M12] = m12;
    mat.val[M22] = m22;
    mat.val[M32] = m32;
    return mat;
  }
}
