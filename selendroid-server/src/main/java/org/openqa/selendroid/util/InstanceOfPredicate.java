package org.openqa.selendroid.util;

import com.android.internal.util.Predicate;

import java.io.Serializable;

public class InstanceOfPredicate
    implements Predicate<Object>, Serializable {
  private final Class<?> clazz;

  public InstanceOfPredicate(Class<?> clazz) {
    this.clazz = Preconditions.checkNotNull(clazz);
  }
  @Override
  public boolean apply(Object o) {
    return clazz.isInstance(o);
  }
  @Override public int hashCode() {
    return clazz.hashCode();
  }
  @Override public boolean equals(Object obj) {
    if (obj instanceof InstanceOfPredicate) {
      InstanceOfPredicate that = (InstanceOfPredicate) obj;
      return clazz == that.clazz;
    }
    return false;
  }
  @Override public String toString() {
    return "IsInstanceOf(" + clazz.getName() + ")";
  }
  private static final long serialVersionUID = 0;
}
