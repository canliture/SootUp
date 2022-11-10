package de.upb.sse.sootup.test;

import static junit.framework.TestCase.*;

import categories.SlowTest;
import de.upb.sse.sootup.callgraph.AbstractCallGraphAlgorithm;
import de.upb.sse.sootup.callgraph.CallGraph;
import de.upb.sse.sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import de.upb.sse.sootup.callgraph.RapidTypeAnalysisAlgorithm;
import de.upb.sse.sootup.core.model.SootClass;
import de.upb.sse.sootup.core.model.SootMethod;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.typehierarchy.TypeHierarchy;
import de.upb.sse.sootup.core.typehierarchy.ViewTypeHierarchy;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.core.views.JavaView;
import de.upb.sse.sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collections;
import java.util.Optional;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(SlowTest.class)
public class CallGraphTest {

  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
  protected JavaClassType mainClassSignature;
  protected MethodSignature mainMethodSignature;
  private AbstractCallGraphAlgorithm algorithm;
  private String algorithmName;

  protected AbstractCallGraphAlgorithm createAlgorithm(JavaView view, TypeHierarchy typeHierarchy) {
    if (algorithmName.equals("RTA")) {
      return new RapidTypeAnalysisAlgorithm(view, typeHierarchy);
    } else {
      return new ClassHierarchyAnalysisAlgorithm(view, typeHierarchy);
    }
  }

  private JavaView createViewForClassPath(String classPath) {
    JavaProject javaProject =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(
                new JavaClassPathAnalysisInputLocation(
                    System.getProperty("java.home") + "/lib/rt.jar"))
            .addInputLocation(new JavaSourcePathAnalysisInputLocation(classPath))
            .build();
    return javaProject.createView();
  }

  CallGraph loadCallGraph(String testDirectory, String className) {
    double version = Double.parseDouble(System.getProperty("java.specification.version"));
    if (version > 1.8) {
      fail("The rt.jar is not available after Java 8. You are using version " + version);
    }

    String classPath = "src/test/resources/callgraph/" + testDirectory;

    // JavaView view = viewToClassPath.computeIfAbsent(classPath, this::createViewForClassPath);
    JavaView view = createViewForClassPath(classPath);

    mainClassSignature = identifierFactory.getClassType(className);
    mainMethodSignature =
        identifierFactory.getMethodSignature(
            mainClassSignature, "main", "void", Collections.singletonList("java.lang.String[]"));

    SootClass<?> sc = view.getClass(mainClassSignature).get();
    Optional<SootMethod> m =
        (Optional<SootMethod>) sc.getMethod(mainMethodSignature.getSubSignature());
    assertTrue(mainMethodSignature + " not found in classloader", m.isPresent());

    final ViewTypeHierarchy typeHierarchy = new ViewTypeHierarchy(view);
    algorithm = createAlgorithm(view, typeHierarchy);
    CallGraph cg = algorithm.initialize(Collections.singletonList(mainMethodSignature));

    assertTrue(
        mainMethodSignature + " is not found in CallGraph", cg.containsMethod(mainMethodSignature));
    assertNotNull(cg);
    return cg;
  }

  @Test
  public void testRTA() {
    algorithmName = "RTA";
    CallGraph cg = loadCallGraph("Misc", "HelloWorld");

    ClassType clazzType = JavaIdentifierFactory.getInstance().getClassType("java.io.PrintStream");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            clazzType, "println", "void", Collections.singletonList("java.lang.String"));

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }

  @Test
  public void testCHA() {
    algorithmName = "CHA";
    CallGraph cg = loadCallGraph("Misc", "HelloWorld");

    ClassType clazzType = JavaIdentifierFactory.getInstance().getClassType("java.io.PrintStream");

    MethodSignature method =
        identifierFactory.getMethodSignature(
            clazzType, "println", "void", Collections.singletonList("java.lang.String"));

    assertTrue(cg.containsCall(mainMethodSignature, method));
  }
}