package org.jid.metajava;

import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.jid.metajava.model.ClassType.ANNOTATION;
import static org.jid.metajava.model.ClassType.CLASS;
import static org.jid.metajava.model.ClassType.ENUM;
import static org.jid.metajava.model.ClassType.INTERFACE;
import static org.jid.metajava.model.ClassType.RECORD;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import org.jid.metajava.model.AnnotationArgument;
import org.jid.metajava.model.AnnotationMeta;
import org.jid.metajava.model.Annotationable;
import org.jid.metajava.model.ClassMeta;
import org.jid.metajava.model.ImportMeta;
import org.jid.metajava.model.MethodMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MetaJavaTest {

  private MetaJava metaJava = new MetaJava();
  private File sampleClass1;
  private File sampleClass2;
  private File sampleInterface1;
  private File sampleInterface2;
  private File sampleRecord1;
  private File sampleEnum1;
  private File sampleAnnotation1;
  private File sampleAnnotation2;
  private List<File> sampleClasses;
  private List<File> samplesClassTypes;

  @BeforeEach
  void setup() {
    Path sampleRootPath = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "sampleCode", "org", "jid");
    sampleClass1 = sampleRootPath.resolve("sample1").resolve("Class1.java").toFile();
    sampleClass2 = sampleRootPath.resolve("sample2").resolve("ClassEmpty.java").toFile();
    sampleInterface1 = sampleRootPath.resolve("sample1").resolve("Interface1.java").toFile();
    sampleInterface2 = sampleRootPath.resolve("sample2").resolve("Interface2.java").toFile();
    sampleRecord1 = sampleRootPath.resolve("sample1").resolve("Record1.java").toFile();
    sampleEnum1 = sampleRootPath.resolve("sample1").resolve("Enum1.java").toFile();
    sampleAnnotation1 = sampleRootPath.resolve("sample1").resolve("Annotation1.java").toFile();
    sampleAnnotation2 = sampleRootPath.resolve("sample2").resolve("Annotation2.java").toFile();
    sampleClasses = List.of(sampleClass1, sampleClass2);
    samplesClassTypes = List.of(sampleClass1, sampleInterface1, sampleRecord1, sampleEnum1, sampleAnnotation1);
  }

  @Nested
  class ClassMetaTests {

    @Nested
    class CommonForAllTypes {


      @Test
      void readClassName() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        assertThat(actual).map(ClassMeta::name).containsExactlyInAnyOrder("Class1", "ClassEmpty");
      }

      @Test
      void readPackageInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        assertThat(actual).map(ClassMeta::packageName)
          .containsExactlyInAnyOrder("org.jid.sample1", "org.jid.sample2");
      }

      @Test
      void readSourceFile() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        String source1 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("Class1.java")).findFirst().orElseThrow();
        assertThat(source1).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample1",
          "Class1.java");

        String source2 = actual.stream().map(ClassMeta::sourceFileUri).filter(s -> s.endsWith("ClassEmpty.java")).findFirst().orElseThrow();
        assertThat(source2).containsSubsequence("meta-java", "lib", "src", "test", "resources", "sampleCode", "org", "jid", "sample2",
          "ClassEmpty.java");
      }

      @Test
      void readStaticImportInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        List<String> staticImports = class1.imports().stream().filter(ImportMeta::isStatic).map(ImportMeta::importString).toList();
        assertThat(staticImports).containsExactlyInAnyOrder(
          "sample.staticimport.Class11.method1", "sample.staticimport.Class11.method2", "sample.staticimport.ClassWildcard1.*"
        );

        ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
        assertThat(classEmpty.imports()).isEmpty();
      }

      @Test
      void readNonStaticImportInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        List<String> staticImports = class1.imports().stream().filter(not(ImportMeta::isStatic)).map(ImportMeta::importString).toList();
        assertThat(staticImports).containsExactlyInAnyOrder(
          "sample.nonstaticimport.Class12", "sample.nonstaticimport.Class13", "sample.nonstaticimport.wildcard.*"
        );

        ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
        assertThat(classEmpty.imports()).isEmpty();
      }

      @Test
      void readClassAnnotationsWithNoArguments() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "MyClassAnnotation1");
        assertThat(annotation.name()).isEqualTo("MyClassAnnotation1");
        assertThat(annotation.args()).isEmpty();
      }

      @Test
      void readClassAnnotationsWithDefaultArgumentAsAString() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "MyClassAnnotation2");
        assertThat(annotation.name()).isEqualTo("MyClassAnnotation2");
        assertThat(annotation.args()).containsExactly(new AnnotationArgument(null, "default param 1"));
      }

      @Test
      void readClassAnnotationsWithDefaultArgumentAsAClassReference() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleAnnotation1));

        ClassMeta class1 = getClassMeta(actual, "Annotation1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "Retention");
        assertThat(annotation.name()).isEqualTo("Retention");
        assertThat(annotation.args()).containsExactly(new AnnotationArgument(null, "RetentionPolicy.RUNTIME"));
      }

      @Test
      void readClassAnnotationsWithMultipleArguments() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta class1 = getClassMeta(actual, "Class1");
        AnnotationMeta annotation = getAnnotationMeta(class1, "MyClassAnnotation3");
        assertThat(annotation.name()).isEqualTo("MyClassAnnotation3");
        assertThat(annotation.args()).containsExactlyInAnyOrder(
          new AnnotationArgument("arg1", "{3, 2, 1}"),
          new AnnotationArgument("arg2", "24")
        );
      }

      @Test
      void readClassAnnotationsWhenThereIsNoAnnotation() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

        ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
        assertThat(classEmpty.annotations()).isEmpty();
      }

    }


    @Nested
    class ClassType {

      @Test
      void readClassType() throws IOException {
        Set<ClassMeta> actual = metaJava.getMetaFrom(
          List.of(sampleClass1, sampleInterface1, sampleEnum1, sampleAnnotation1, sampleRecord1));

        ClassMeta clazz = getClassMeta(actual, "Class1");
        assertThat(clazz.type()).isEqualTo(CLASS);
      }

      @Test
      void readInheritanceInfoWhenClassExtendsFromAnotherClass() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleClass1));

        ClassMeta class1 = getClassMeta(actual, "Class1");
        assertThat(class1.extendsFrom()).containsExactly("ClassParent1");
      }

      @Test
      void readNoInheritanceInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleClass2));

        ClassMeta class2 = getClassMeta(actual, "ClassEmpty");
        assertThat(class2.extendsFrom()).isEmpty();
      }
    }

    @Nested
    class InterfaceType {

      @Test
      void readInterfaceType() throws IOException {
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Interface1");
        assertThat(clazz.type()).isEqualTo(INTERFACE);
      }

      @Test
      void readInheritanceInfoWhenInterfaceExtendsFromAnotherInterface() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleInterface1));

        ClassMeta interface1 = getClassMeta(actual, "Interface1");
        assertThat(interface1.extendsFrom()).containsExactlyInAnyOrder("InterfaceParent1", "InterfaceParent2");
      }

      @Test
      void readNoInheritanceInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleInterface2));

        ClassMeta interface2 = getClassMeta(actual, "Interface2");
        assertThat(interface2.extendsFrom()).isEmpty();
      }
    }

    @Nested
    class RecordType {

      @Test
      void readRecordType() throws IOException {
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Record1");
        assertThat(clazz.type()).isEqualTo(RECORD);
      }

      @Test
      void readNoInheritanceInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleRecord1));

        ClassMeta record1 = getClassMeta(actual, "Record1");
        assertThat(record1.extendsFrom()).isEmpty();
      }
    }

    @Nested
    class EnumType {

      @Test
      void readEnumType() throws IOException {
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Enum1");
        assertThat(clazz.type()).isEqualTo(ENUM);
      }

      @Test
      void readNoInheritanceInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleEnum1));

        ClassMeta enum1 = getClassMeta(actual, "Enum1");
        assertThat(enum1.extendsFrom()).isEmpty();
      }
    }

    @Nested
    class AnnotationType {

      @Test
      void readAnnotationType() throws IOException {
        Set<ClassMeta> actual = metaJava.getMetaFrom(samplesClassTypes);

        ClassMeta clazz = getClassMeta(actual, "Annotation1");
        assertThat(clazz.type()).isEqualTo(ANNOTATION);
      }

      @Test
      void readNoInheritanceInfo() throws IOException {

        Set<ClassMeta> actual = metaJava.getMetaFrom(List.of(sampleAnnotation1));

        ClassMeta annotation1 = getClassMeta(actual, "Annotation1");
        assertThat(annotation1.extendsFrom()).isEmpty();
      }

    }


  }

  @Nested
  class MethodMetaTests {

    @Test
    void readMethodNames() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      assertThat(class1.methods()).map(MethodMeta::name).containsExactlyInAnyOrder("m11", "m12", "noAnnotationMethod");
    }

    @Test
    void returnEmptyWhenNoMethodNames() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta classEmpty = getClassMeta(actual, "ClassEmpty");
      assertThat(classEmpty.methods()).isEmpty();
    }
  }

  @Nested
  class MethodAnnotationsMeta {

    @Test
    void readAnnotationsWithoutArguments() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m12");
      AnnotationMeta myMethodAnnotation13 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation13");

      assertThat(myMethodAnnotation13.name()).isEqualTo("MyMethodAnnotation13");
      assertThat(myMethodAnnotation13.args()).isEmpty();
    }

    @Test
    void readAnnotationsWithDefaultArgument() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m11");
      AnnotationMeta myMethodAnnotation11 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation11");

      assertThat(myMethodAnnotation11.name()).isEqualTo("MyMethodAnnotation11");
      assertThat(myMethodAnnotation11.args()).hasSize(1);

      AnnotationArgument arg = myMethodAnnotation11.args().stream().findFirst().orElseThrow();
      assertThat(arg).isEqualTo(new AnnotationArgument(null, "annotation param 11"));
    }

    @Test
    void readAnnotationsWithSeveralArguments() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "m12");
      AnnotationMeta MyMethodAnnotation12 = getAnnotationMeta(methodMeta12, "MyMethodAnnotation12");

      assertThat(MyMethodAnnotation12.name()).isEqualTo("MyMethodAnnotation12");
      assertThat(MyMethodAnnotation12.args()).hasSize(2);

      AnnotationArgument arg1Value = getAnnotationArgsMeta(MyMethodAnnotation12, "arg1");
      assertThat(arg1Value).isEqualTo(new AnnotationArgument("arg1", "42"));

      AnnotationArgument arg2Value = getAnnotationArgsMeta(MyMethodAnnotation12, "arg2");
      assertThat(arg2Value).isEqualTo(new AnnotationArgument("arg2", "{1, 2, 3}"));
    }

    @Test
    void readMethodWithNoAnnotations() throws IOException {

      Set<ClassMeta> actual = metaJava.getMetaFrom(sampleClasses);

      ClassMeta class1 = getClassMeta(actual, "Class1");
      MethodMeta methodMeta12 = getMethodMeta(class1, "noAnnotationMethod");

      assertThat(methodMeta12.annotations()).isEmpty();
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

  private ClassMeta getClassMeta(Set<ClassMeta> actual, String name) {
    return actual.stream().filter(c -> c.name().equals(name)).findFirst().orElseThrow();
  }

  private MethodMeta getMethodMeta(ClassMeta class1, String methodName) {
    return class1.methods().stream().filter(m -> methodName.equals(m.name())).findFirst().orElseThrow();
  }

  private AnnotationMeta getAnnotationMeta(Annotationable element1, String annotationName) {
    return element1.annotations().stream().filter(a -> annotationName.equals(a.name())).findFirst().orElseThrow();
  }

  private AnnotationArgument getAnnotationArgsMeta(AnnotationMeta annotationMeta, String argName) {
    return annotationMeta.args().stream()
      .filter(AnnotationArgument::hasName)
      .filter(arg -> arg.name().startsWith(argName))
      .findFirst()
      .orElseThrow();
  }


}
