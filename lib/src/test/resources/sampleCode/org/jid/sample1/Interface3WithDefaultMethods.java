package org.jid.sample1;

import java.io.IOException;

public interface Interface3WithDefaultMethods {

  int method31() default {
    return 42;
  }
  void method32() throws IOException;

}