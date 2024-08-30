package org.jid.sample1;

public record Record2ImplementsWithGenerics(String param1, int param2) implements IR1<Generic1>, IR2<Generic2> {

  void method11();

}