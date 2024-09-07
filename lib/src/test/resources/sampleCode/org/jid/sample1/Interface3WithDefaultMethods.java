package org.jid.sample1;

import java.io.IOException;

public interface Interface3WithDefaultMethods {

  default int method31() {
    return 42;
  }

  default void method32() throws IOException {
    throw new IOException();
  }

  void methodWithNoDefaultImplementation();

}