package org.jid.sample1;

import static sample.staticimport.Class11.method1;
import static sample.staticimport.Class11.method2;
import sample.nonstaticimport.Class12;
import sample.nonstaticimport.Class13;

public class Class1 {

  @MyMethodAnnotation11("annotation param 11")
  public void m11() {

  }

  @MyMethodAnnotation12(arg1 = 42, arg2 = {1, 2, 3})
  @MyMethodAnnotation13
  private int m12() {
    return 42;
  }

  void noAnnotationMethod() {
  }
}