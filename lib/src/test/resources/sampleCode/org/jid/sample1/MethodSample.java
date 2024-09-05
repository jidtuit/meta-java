package org.jid.sample1;

import java.io.IOException;

public class MethodSample {

  // How should we treat constructors... as a method? It's own product? Constructors retunr null for returnType
  public MethodSample() {
  }

  public MethodSample(String s) {
  }

  public void m1() {
  }

  int m2() {
    return 42;
  }

  private StringBuilder m3() {
    return new StringBuilder("HelloWorld");
  }

  protected Double m4(Integer p1, @Deprecated int p2) {
    return p1 + p2;
  }

  public static void staticMethod() {
  }

  public void varArgsMethod(String... args) {

  }

  public void throwsExceptionMethod() throws MyException1 {

  }

  @Annotated
  public void annotatedMethod() {
  }

  public void annotatedParams(@Deprecated String p1) {
  }

}