package org.jid.sample1;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Annotation1 {

  String value();

  int[] arg12() default {2, 3, 4};

}