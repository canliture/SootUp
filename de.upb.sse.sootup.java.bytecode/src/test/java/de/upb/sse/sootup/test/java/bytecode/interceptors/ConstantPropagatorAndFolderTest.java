package de.upb.sse.sootup.test.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.jimple.basic.NoPositionInformation;
import de.upb.sse.sootup.core.jimple.basic.StmtPositionInfo;
import de.upb.sse.sootup.core.jimple.basic.Trap;
import de.upb.sse.sootup.core.jimple.common.constant.IntConstant;
import de.upb.sse.sootup.core.jimple.common.expr.JAddExpr;
import de.upb.sse.sootup.core.jimple.common.stmt.Stmt;
import de.upb.sse.sootup.core.model.Body;
import de.upb.sse.sootup.core.types.PrimitiveType;
import de.upb.sse.sootup.core.util.ImmutableUtils;
import de.upb.sse.sootup.java.bytecode.interceptors.ConstantPropagatorAndFolder;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.language.JavaJimple;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Marcus Nachtigall */
@Category(Java8Test.class)
public class ConstantPropagatorAndFolderTest {

  /**
   * Tests the correct folding and propagation. Transforms from
   *
   * <p>a = 3; b = 4; c = a + b; return c;
   *
   * <p>to
   *
   * <p>a = 3; b = 4; c = 7; return 7;
   */
  @Test
  public void testModification() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();
    new ConstantPropagatorAndFolder().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    assertEquals(originalStmts.get(0).toString(), processedStmts.get(0).toString());
    assertEquals(originalStmts.get(1).toString(), processedStmts.get(1).toString());
    assertEquals("c = 3 + 4", originalStmts.get(2).toString());
    assertEquals("c = 7", processedStmts.get(2).toString());
    assertEquals("return c", originalStmts.get(3).toString());
    assertEquals("return 7", processedStmts.get(3).toString());
  }

  /**
   * Tests the correct handling of a builder without any propagation or folding. Considers the
   * following code, but does not change anything:
   *
   * <p>a = 3; b = 4; a = 2; return c;
   */
  @Test
  public void testNoModification() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new ConstantPropagatorAndFolder().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();
    List<Stmt> originalStmts = testBody.getStmts();
    List<Stmt> processedStmts = processedBody.getStmts();

    assertEquals(originalStmts.size(), processedStmts.size());
    for (int i = 0; i < processedStmts.size(); i++) {
      assertEquals(originalStmts.get(i).toString(), processedStmts.get(i).toString());
    }
  }

  private static Body.BodyBuilder createBody(boolean constantFolding) {
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    Local a = JavaJimple.newLocal("a", PrimitiveType.getInt());
    Local b = JavaJimple.newLocal("b", PrimitiveType.getInt());
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    Set<Local> locals = ImmutableUtils.immutableSet(a, b, c);
    List<Trap> traps = Collections.emptyList();

    Stmt assignA = JavaJimple.newAssignStmt(a, IntConstant.getInstance(3), noPositionInfo);
    Stmt assignB = JavaJimple.newAssignStmt(b, IntConstant.getInstance(4), noPositionInfo);
    Stmt assignC;
    if (constantFolding) {
      assignC =
          JavaJimple.newAssignStmt(
              c,
              new JAddExpr(IntConstant.getInstance(3), IntConstant.getInstance(4)),
              noPositionInfo);
    } else {
      assignC = JavaJimple.newAssignStmt(a, IntConstant.getInstance(2), noPositionInfo);
    }
    Stmt ret = JavaJimple.newReturnStmt(c, noPositionInfo);

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(assignA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    builder.addFlow(assignA, assignB);
    builder.addFlow(assignB, assignC);
    builder.addFlow(assignC, ret);

    builder.setLocals(locals);
    builder.setTraps(traps);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}