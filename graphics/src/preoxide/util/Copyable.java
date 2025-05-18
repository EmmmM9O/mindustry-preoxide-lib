/* (C) 2025 */
package preoxide.util;

public interface Copyable<T extends Copyable<T>> {
  public T cpy();
}
