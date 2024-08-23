package org.jid.metajava;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MetaJavaTest {

  private MetaJava metaJava = new MetaJava();
  private File sample1;
  private File sample2;

  @BeforeEach
  void setup() {
    Path sampleRootPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "sampleCode", "org", "jid");
    sample1 = sampleRootPath.resolve("sample1").resolve("Class1.java").toFile();
    sample2 = sampleRootPath.resolve("sample2").resolve("Class2.java").toFile();
  }

  @Nested
  class ClassMetaTests {

    @Test
    void readClassName() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      assertThat(actual).map(ClassMeta::name).containsExactlyInAnyOrder("Class1", "Class2");
    }

    @Test
    void readPackageInfo() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      assertThat(actual).map(ClassMeta::packageName).containsExactlyInAnyOrder("org.jid.sample1", "org.jid.sample2");
    }

    @Test
    void readSourceFile() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      String source1 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("Class1.java")).findFirst().orElseThrow();
      assertThat(source1).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample1",
        "Class1.java");

      String source2 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("Class2.java")).findFirst().orElseThrow();
      assertThat(source2).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample2",
        "Class2.java");
    }

    @Test
    void readStaticImportInfo() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      List<String> staticImports = class1.imports().stream().filter(ImportMeta::isStatic).map(ImportMeta::importString).toList();
      assertThat(staticImports).containsExactlyInAnyOrder(
        "sample.staticimport.Class11.method1", "sample.staticimport.Class11.method2"
      );

      ClassMeta class2 = getClassMeta(actual, "Class2");
      assertThat(class2.imports()).isEmpty();
    }

    @Test
    void readNonStaticImportInfo() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      List<String> staticImports = class1.imports().stream().filter(not(ImportMeta::isStatic)).map(ImportMeta::importString).toList();
      assertThat(staticImports).containsExactlyInAnyOrder(
        "sample.nonstaticimport.Class12", "sample.nonstaticimport.Class13"
      );

      ClassMeta class2 = getClassMeta(actual, "Class2");
      assertThat(class2.imports()).isEmpty();
    }
  }

  @Nested
  class MethodMetaTests {

    @Test
    void readMethodNames() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      assertThat(class1.methods()).map(MethodMeta::name).containsExactlyInAnyOrder("m11", "m12", "noAnnotationMethod");
    }

    @Test
    void returnEmptyWhenNoMethodNames() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class2 = getClassMeta(actual, "Class2");
      assertThat(class2.methods()).isEmpty();
    }
  }

  @Nested
  class MethodAnnotationsMeta {

    @Test
    void readAnnotationsWithoutArguments() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m12");
      AnnotationMeta myMethodAnnotation13 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation13");

      assertThat(myMethodAnnotation13.name()).isEqualTo("MyMethodAnnotation13");
      assertThat(myMethodAnnotation13.args()).isEmpty();
    }

    @Test
    void readAnnotationsWithDefaultArgument() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m11");
      AnnotationMeta myMethodAnnotation11 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation11");

      assertThat(myMethodAnnotation11.name()).isEqualTo("MyMethodAnnotation11");
      assertThat(myMethodAnnotation11.args()).hasSize(1);

      String arg = myMethodAnnotation11.args().stream().findFirst().orElseThrow();
      assertThat(arg).isEqualTo("annotation param 11");
    }

    @Test
    void readAnnotationsWithSeveralArguments() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m12");
      AnnotationMeta MyMethodAnnotation12 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation12");

      assertThat(MyMethodAnnotation12.name()).isEqualTo("MyMethodAnnotation12");
      assertThat(MyMethodAnnotation12.args()).hasSize(2);

      String arg1Value = getAnnotationArgsMeta(MyMethodAnnotation12, "arg1");
      assertThat(arg1Value).isEqualTo("arg1 = 42");

      String arg2Value = getAnnotationArgsMeta(MyMethodAnnotation12, "arg2");
      assertThat(arg2Value).isEqualTo("arg2 = {1, 2, 3}");
    }

    @Test
    void readMethodWithNoAnnotations() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sample1, sample2));

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "noAnnotationMethod");

      assertThat(methodMeta12. annotations()).isEmpty();
    }

  }

  @Nested
  class InputParamTests {

    @Test
    void throwWhenFilesParameterIsNull() {
      assertThatThrownBy(() -> metaJava.getMetaFrom(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void throwWhenFilesParameterIsEmpty() {
      assertThatThrownBy(() -> metaJava.getMetaFrom(Set.of())).isInstanceOf(IllegalArgumentException.class);
    }

  }
  private ClassMeta getClassMeta(Set<ClassMeta> actual, String Class1) {
    return actual.stream().filter(c -> c.name().equals(Class1)).findFirst().orElseThrow();
  }

  private MethodMeta getMethodMeta(ClassMeta class1, String methodName) {
    return class1.methods().stream().filter(m -> methodName.equals(m.name())).findFirst().orElseThrow();
  }

  private AnnotationMeta getAnnotationMeta(MethodMeta method1, String annotationName) {
    return method1.annotations().stream().filter(a -> annotationName.equals(a.name())).findFirst().orElseThrow();
  }

  private String getAnnotationArgsMeta(AnnotationMeta annotationMeta, String argName) {
    return annotationMeta.args().stream().filter(arg -> arg.startsWith(argName)).findFirst().orElseThrow();
  }


}
