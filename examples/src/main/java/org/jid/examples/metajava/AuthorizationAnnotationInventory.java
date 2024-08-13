package org.jid.examples.metajava;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import org.jid.metajava.MetaJava;
import org.jid.metajava.model.ClassMeta;

public class AuthorizationAnnotationInventory {

  private static final String SEARCH_FROM_CARBON_MODULE = "/Users/jidominguez/clarity/ws/backend/src/carbon-module";
  private static final String SEARCH_FROM_TCFD = "/Users/jidominguez/clarity/ws/backend/src/tcfd";
  private static final String JAVA_FILE_SUFFIX = "Controller.java";
  private static final String ANNOTATION_NAME = "Authorization";
  private static final String ANNOTATION_ARG_NAME = "anyResourceOf";
  private static final Path OUTPUT_FILE = Path.of("/Users/jidominguez/Downloads/metaInfo.csv");
  private static final String SEPARATOR = ";";

  public static void main(String[] args) throws IOException {
    Set<File> filesCarbon = searchForControllerJavaFiles(new File(SEARCH_FROM_CARBON_MODULE));
    Set<File> filesTcfd = searchForControllerJavaFiles(new File(SEARCH_FROM_TCFD));
    Set<File> files = new HashSet<>(filesCarbon);
    files.addAll(filesTcfd);
    Set<ClassMeta> classes = new MetaJava().getMetaFrom(files);
    saveToCsv(classes);

    System.out.println("End of program");
  }

  private static void saveToCsv(Set<ClassMeta> classes) throws IOException {

    var csv = new ArrayList<String>();
    csv.add(
      "Module" + SEPARATOR + "Class" + SEPARATOR + "Method" + SEPARATOR + "Annotation" + SEPARATOR + "Permission" + SEPARATOR + "Package");
    classes.forEach(clazz -> clazz.methods().forEach(method ->
        method.annotations().stream().filter(annotation -> ANNOTATION_NAME.equals(annotation.name())).forEach(annotation ->
          annotation.args().stream().filter(arg -> arg.startsWith(ANNOTATION_ARG_NAME))
            .forEach(arg -> { // Alternative: Filter if arg doesn't start with index
                List<String> permissions = parsePermissions(arg);
                permissions.forEach(permission ->
                  csv.add(getModule(clazz) + SEPARATOR + clazz.name() + SEPARATOR + method.name() + SEPARATOR + annotation.name() + SEPARATOR
                    + permission + SEPARATOR + clazz.packageName())
                );
              }
            )
        )
      )
    );

    Files.deleteIfExists(OUTPUT_FILE);
    Files.write(OUTPUT_FILE, csv, UTF_8, CREATE_NEW);

  }

  private static List<String> parsePermissions(String rawPermissions) {
    return Stream.of(rawPermissions.split(","))
      .map(String::trim)
      .map(permission -> permission.replace("{", "").replace("}", ""))
      .map(permission -> permission.split("AuthorizationItems.")[1])
      .toList();
  }

  private static String getModule(ClassMeta clazz) {
    String path = clazz.sourceFileUri();
    if (path.contains("/carbon-module/carbon-module-api")) {
      return "carbon-api";
    }
    if (path.contains("/carbon-module/carbon-module-public-api")) {
      return "carbon-public-api";
    }
    if (path.contains("/tcfd/tcfd-api")) {
      return "tcfd-api";
    }
    if (path.contains("/tcfd/tcfd-public-api")) {
      return "tcfd-public-api";
    }
    return "";
  }

  private static Set<File> searchForControllerJavaFiles(File dir) {
    Set<File> files = new HashSet<>();
    var dirStack = new ArrayDeque<File>();
    dirStack.push(dir);
    while (!dirStack.isEmpty()) {
      var file = dirStack.pop();
      if (file.getName().endsWith(JAVA_FILE_SUFFIX)) {
        files.add(file);
      }
      if (file.isDirectory()) {
        File[] filesInsideDirectory = Objects.requireNonNull(file.listFiles());
        Stream.of(filesInsideDirectory).forEach(dirStack::push);
      }
    }
    return files;
  }

}
