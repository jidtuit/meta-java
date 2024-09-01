package org.jid.sample1;

public class MultipleClassIn1File {

  public String m1() {
    return "m1";
  }

}

public class MultipleClassIn1File2 {

  public String m2() {
    return "m2";
  }
}

public record MultipleClassFileRecord(String field1) {

}

public interface MultipleClassFileInterface {

  int mInterface1();
}

public enum MultipleClassFileEnum {
  VAR1, VAR2
}

public @interface MultipleClassFileAnnotation {

  String[] value();
}