package org.jid.sample1;

public record Record1(String param1, @Deprecated int param2) implements IR1, IR2 {

  public static final Integer MY_CONST = 42;

  public Record1 {
    // code for constructor
  }

  void method11();

}