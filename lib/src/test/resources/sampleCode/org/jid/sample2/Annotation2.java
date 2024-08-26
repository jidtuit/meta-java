package org.jid.sample1;

import java.lang.annotation.*;

public @interface Annotation2 {

  String value();

  int[] arg12() default {2, 3, 4};

}