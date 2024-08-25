package org.jid.sample1;

import static sample.staticimport.Class11.method1;
import static sample.staticimport.Class11.method2;
import static sample.staticimport.ClassWildcard1.*;

import sample.nonstaticimport.Class12;
import sample.nonstaticimport.Class13;
import sample.nonstaticimport.wildcard.*;

@MyClassAnnotation1
@MyClassAnnotation2("default param 1")
@MyClassAnnotation3(arg1 = {3, 2, 1}, arg2 = 24)
public class Class1 {

  public static final String CONSTANT_1_1 = "constant1-1";
  private int answer = 42;
  private int notInitVar;

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