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
public abstract class Class1 extends ClassParent1 implements I1, I2 {

  public static final String CONSTANT_1_1 = "constant1-1";
  @MeaningOfLifeUniverseAndEverythingElse
  private transient int answer = 42;
  Double notInitVar;
  protected volatile strictfp float expressionVar = 1f + 1f;

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