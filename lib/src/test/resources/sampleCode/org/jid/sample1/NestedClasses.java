package org.jid.sample1;

import org.jid.Class1;

public class NestedClasses {

  public static class StaticNestedClass {

  }

  public class InnerClass {

  }

  public class InnerClass2 {

    public record ChildInnerClass2(int innerParam) {

    }
  }

  record InnerRecord(String p1) {

  }

  private enum InnerEnum {
    VAL1;
  }

  interface InnerInterface {

  }

  @interface InnerAnnotation {

  }

}