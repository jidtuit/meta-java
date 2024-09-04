package org.jid.sample1;

import java.io.IOException;

public class MethodSample {

  public void publicMethod() {
  }

  int friendlyMethod() {
    return 42;
  }

  private String privateMethod() {
    return "Hello World!";
  }

  protected Double protectedMethod(Integer p1, int p2) {
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