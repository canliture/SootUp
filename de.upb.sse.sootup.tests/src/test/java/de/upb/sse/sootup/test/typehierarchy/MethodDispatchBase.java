package de.upb.sse.sootup.test.typehierarchy;

import categories.Java8Test;
import de.upb.sse.sootup.core.inputlocation.AnalysisInputLocation;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import de.upb.sse.sootup.java.core.views.JavaView;
import de.upb.sse.sootup.java.sourcecode.inputlocation.JavaSourcePathAnalysisInputLocation;
import java.util.Collections;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** @author Hasitha Rajapakse */
@Category(Java8Test.class)
public class MethodDispatchBase {
  static final String baseDir = "src/test/resources/methoddispatchresolver/";
  protected JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();

  @ClassRule
  public static MethodDispatchBase.CustomTestWatcher customTestWatcher =
      new MethodDispatchBase.CustomTestWatcher();

  public static class CustomTestWatcher extends TestWatcher {
    private String className = MethodDispatchBase.class.getSimpleName();
    private AnalysisInputLocation srcCode;
    private JavaView view;
    private JavaProject project;

    @Override
    protected void starting(Description description) {
      String prevClassName = getClassName();
      setClassName(extractClassName(description.getClassName()));
      if (!prevClassName.equals(getClassName())) {
        srcCode =
            new JavaSourcePathAnalysisInputLocation(
                Collections.singleton(baseDir + "/" + getClassName()));
        project = JavaProject.builder(new JavaLanguage(8)).addInputLocation(this.srcCode).build();
        setView(project.createView());
      }
    }

    public String getClassName() {
      return className;
    }

    private void setClassName(String className) {
      this.className = className;
    }

    private void setView(JavaView view) {
      this.view = view;
    }

    public JavaView getView() {
      return view;
    }
  }

  public JavaClassType getClassType(String className) {
    return identifierFactory.getClassType(className);
  }

  public static String extractClassName(String classPath) {
    String classPathArray = classPath.substring(classPath.lastIndexOf(".") + 1);
    String testDirectoryName = "";
    if (!classPathArray.isEmpty()) {
      testDirectoryName = classPathArray.substring(0, classPathArray.length() - 4);
    }
    return testDirectoryName;
  }
}