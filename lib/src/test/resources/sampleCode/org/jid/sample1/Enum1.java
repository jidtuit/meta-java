package org.jid.sample1;

public enum Enum1 implements I1, I2 {

  @Deprecated VAR1("hello"), VAR2;

  private final String initVar;

  Enum1(String initVar) {
    this.initVar = initVar;
  }

  Enum1() {
    this.initVar = "world";
  }

  void method11();

}