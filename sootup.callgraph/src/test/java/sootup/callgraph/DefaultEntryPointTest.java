package sootup.callgraph;

import static junit.framework.TestCase.*;

import categories.Java8Test;
import java.nio.file.Paths;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.typehierarchy.ViewTypeHierarchy;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;
import sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleProject;
import sootup.jimple.parser.JimpleView;

@Category(Java8Test.class)
public class DefaultEntryPointTest {

  /** Method to check java version. */
  private void checkVersion() {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }
  }

  /**
   * The method returns the view for input java source path and rt.jar file.
   *
   * @param classPath - The location of java source files.
   * @return - Java view
   */
  private JavaView getView(String classPath) {
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    return javaProject.createOnDemandView();
  }

  /**
   * Test to create call graph for CHA without specifying entry point. It uses main method present
   * in input java files as entry point.
   */
  @Test
  public void CHADefaultEntryPoint() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/DefaultEntryPoint");

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm = new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize();
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertTrue(cg.containsCall(mainMethodSignature, methodD));

    assertEquals(6, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());
    assertEquals(1, cg.callsTo(methodD).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
    assertEquals(0, cg.callsFrom(methodD).size());
  }

  /**
   * Test uses initialize() method to create call graph, but multiple main methods are present in
   * input java source files. Expected result is RuntimeException.
   */
  @Test
  public void CHAMultipleMainMethod() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/Misc");

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    ClassHierarchyAnalysisAlgorithm algorithm =
        new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when multiple main methods are defined.");
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
      assertTrue(e.getMessage().startsWith("There are more than 1 main method present"));
    }
  }

  /**
   * Test uses initialize() method to create call graph, but no main method is present in input java
   * source files. Expected result is RuntimeException.
   */
  @Test
  public void CHANoMainMethod() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/NoMainMethod");

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    ClassHierarchyAnalysisAlgorithm algorithm =
        new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    try {
      algorithm.initialize();
      fail("Runtime Exception not thrown, when no main methods are defined.");
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
      assertEquals(
          e.getMessage(),
          "No main method is present in the input programs. initialize() method can be used if only one main method exists in the input program and that should be used as entry point for call graph. \n Please specify entry point as a parameter to initialize method.");
    }
  }

  /**
   * Test to create call graph for RTA without specifying entry point. It uses main method present
   * in input java files as entry point.
   */
  @Test
  public void RTADefaultEntryPoint() {
    checkVersion();

    JavaView view = getView("src/test/resources/callgraph/DefaultEntryPoint");

    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

    JavaClassType mainClassSignature = identifierFactory.getClassType("example1.Example");
    MethodSignature mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    CallGraphAlgorithm algorithm = new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize();
    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);

    MethodSignature constructorB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature constructorC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "<init>",
            "void",
            Collections.emptyList());

    MethodSignature methodA =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.A"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodB =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.B"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodC =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.C"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    MethodSignature methodD =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("example1.D"),
            "print",
            "void",
            Collections.singletonList("java.lang.Object"));

    assertTrue(cg.containsCall(mainMethodSignature, constructorB));
    assertTrue(cg.containsCall(mainMethodSignature, constructorC));

    assertTrue(cg.containsCall(mainMethodSignature, methodA));
    assertTrue(cg.containsCall(mainMethodSignature, methodB));
    assertTrue(cg.containsCall(mainMethodSignature, methodC));
    assertFalse(cg.containsMethod(methodD));

    assertEquals(5, cg.callsFrom(mainMethodSignature).size());

    assertEquals(2, cg.callsTo(constructorB).size());
    assertEquals(1, cg.callsTo(constructorC).size());
    assertEquals(1, cg.callsTo(methodA).size());
    assertEquals(1, cg.callsTo(methodB).size());
    assertEquals(1, cg.callsTo(methodC).size());

    assertEquals(0, cg.callsFrom(methodA).size());
    assertEquals(0, cg.callsFrom(methodB).size());
    assertEquals(0, cg.callsFrom(methodC).size());
  }

  /**
   * Test for JavaSourcePathAnalysisInputLocation. Specifying all input source files with source
   * type as Library. Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputSourcePath() {
    checkVersion();

    String classPath = "src/test/resources/callgraph/DefaultEntryPoint";
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaSourcePathAnalysisInputLocation(SourceType.Library, classPath))
            .build();
    JavaView view = javaProject.createOnDemandView();

    Set<SootClass<JavaSootClassSource>> classes =
        new HashSet<>(); // Set to track the classes to check
    for (SootClass<JavaSootClassSource> aClass : view.getClasses()) {
      if (!aClass.isLibraryClass()) {
        System.out.println("Found user defined class " + aClass);
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }

  /**
   * Test for JavaClassPathAnalysisInputLocation. Specifying jar file with source type as Library.
   * Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputClassPath() {
    checkVersion();

    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar", SourceType.Library))
            .build();
    JavaView view = javaProject.createOnDemandView();

    Collection<SootClass<JavaSootClassSource>> classes =
        new HashSet<>(); // Set to track the classes to check

    for (SootClass<JavaSootClassSource> aClass : view.getClasses()) {
      // System.out.println(aClass.getClassSource().getClassType().isBuiltInClass());
      if (!aClass.isLibraryClass()) {
        System.out.println("Found user defined class " + aClass);
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }

  /**
   * Test for JimpleAnalysisInputLocation. Specifying jimple file with source type as Library.
   * Expected - All input classes are of source type Library.
   */
  @Test
  public void specifyBuiltInInputJimplePath() {
    checkVersion();

    String classPath = "src/test/resources/callgraph/jimple";
    AnalysisInputLocation<JavaSootClass> jimpleInputLocation =
        new JimpleAnalysisInputLocation<>(Paths.get(classPath), SourceType.Library);
    JimpleView view = new JimpleProject(jimpleInputLocation).createOnDemandView();

    Collection<SootClass<?>> classes = new HashSet<>(); // Set to track the classes to check

    for (SootClass<?> aClass : view.getClasses()) {
      // System.out.println(aClass.getClassSource().getClassType().isBuiltInClass());
      if (!aClass.isLibraryClass()) {
        System.out.println("Found user defined class " + aClass);
        classes.add(aClass);
      }
    }

    assertEquals("User Defined class found, expected none", 0, classes.size());
  }
}
