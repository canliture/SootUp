package de.upb.swt.soot.test.java.bytecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import java.util.Collections;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Kaustubh Kelkar */
@Category(Java8Test.class)
public class BooleanOperatorsTest extends MinimalBytecodeTestSuiteBase {

  @Test
  public void defaultTest() {

    SootMethod method = loadMethod(getMethodSignature("relationalOpEqual"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 0",
            "label1:",
            "$stack3 = l1",
            "$stack2 = 10",
            "if $stack3 > $stack2 goto label2",
            "l1 = l1 + 1",
            "if l1 != 5 goto label1",
            "goto label2",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("relationalOpNotEqual"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 0",
            "l2 = \"\"",
            "label1:",
            "$stack4 = l1",
            "$stack4 = $stack4",
            "$stack3 = 10",
            "$stack3 = $stack3",
            "if $stack4 >= $stack3 goto label2",
            "l1 = l1 + 1",
            "if l1 == 5 goto label1",
            "l2 = \"i != 5\"",
            "goto label1",
            "label2:",
            "return"));

    method = loadMethod(getMethodSignature("complementOp"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 1",
            "if l1 == 0 goto label3",
            "if l1 != 0 goto label1",
            "$stack2 = 1",
            "goto label2",
            "label1:",
            "$stack2 = 0",
            "label2:",
            "l1 = $stack2",
            "label3:",
            "return"));

    method = loadMethod(getMethodSignature("logicalOpAnd"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 1",
            "l2 = 1",
            "l3 = 0",
            "l4 = 0",
            "l5 = \"\"",
            "$stack6 = l1 & l2",
            "if $stack6 == 0 goto label1",
            "l5 = \"A\"",
            "label1:",
            "$stack15 = l3",
            "$stack14 = l4",
            "$stack7 = $stack15 & $stack14",
            "if $stack7 == 0 goto label2",
            "l5 = \"B\"",
            "label2:",
            "$stack13 = l1",
            "$stack12 = l3",
            "$stack8 = $stack13 & $stack12",
            "if $stack8 == 0 goto label3",
            "l5 = \"C\"",
            "label3:",
            "$stack11 = l4",
            "$stack10 = l2",
            "$stack9 = $stack11 & $stack10",
            "if $stack9 == 0 goto label4",
            "l5 = \"D\"",
            "label4:",
            "return"));

    method = loadMethod(getMethodSignature("logicalOpOr"));

    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 1",
            "l2 = 1",
            "l3 = 0",
            "l4 = 0",
            "l5 = \"\"",
            "$stack6 = l1 | l2",
            "if $stack6 == 0 goto label1",
            "l5 = \"A\"",
            "label1:",
            "$stack15 = l3",
            "$stack14 = l4",
            "$stack7 = $stack15 | $stack14",
            "if $stack7 == 0 goto label2",
            "l5 = \"B\"",
            "label2:",
            "$stack13 = l1",
            "$stack12 = l3",
            "$stack8 = $stack13 | $stack12",
            "if $stack8 == 0 goto label3",
            "l5 = \"C\"",
            "label3:",
            "$stack11 = l4",
            "$stack10 = l2",
            "$stack9 = $stack11 | $stack10",
            "if $stack9 == 0 goto label4",
            "l5 = \"D\"",
            "label4:",
            "return"));

    method = loadMethod(getMethodSignature("logicalOpXor"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 1",
            "l2 = 1",
            "l3 = 0",
            "l4 = 0",
            "l5 = \"\"",
            "$stack6 = l1 ^ l2",
            "if $stack6 == 0 goto label1",
            "l5 = \"A\"",
            "label1:",
            "$stack15 = l3",
            "$stack14 = l4",
            "$stack7 = $stack15 ^ $stack14",
            "if $stack7 == 0 goto label2",
            "l5 = \"B\"",
            "label2:",
            "$stack13 = l1",
            "$stack12 = l3",
            "$stack8 = $stack13 ^ $stack12",
            "if $stack8 == 0 goto label3",
            "l5 = \"C\"",
            "label3:",
            "$stack11 = l4",
            "$stack10 = l2",
            "$stack9 = $stack11 ^ $stack10",
            "if $stack9 == 0 goto label4",
            "l5 = \"D\"",
            "label4:",
            "return"));

    method = loadMethod(getMethodSignature("ConditionalOpAnd"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 1",
            "l2 = 1",
            "l3 = 0",
            "l4 = 0",
            "l5 = \"\"",
            "if l1 == 0 goto label1",
            "if l2 == 0 goto label1",
            "l5 = \"A\"",
            "label1:",
            "$stack8 = l3",
            "$stack8 = $stack8",
            "if $stack8 == 0 goto label2",
            "if l4 == 0 goto label2",
            "l5 = \"B\"",
            "label2:",
            "$stack7 = l1",
            "$stack7 = $stack7",
            "if $stack7 == 0 goto label3",
            "if l3 == 0 goto label3",
            "l5 = \"C\"",
            "label3:",
            "$stack6 = l4",
            "$stack6 = $stack6",
            "if $stack6 == 0 goto label4",
            "if l2 == 0 goto label4",
            "l5 = \"D\"",
            "label4:",
            "return"));
    method = loadMethod(getMethodSignature("conditionalOpOr"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 1",
            "l2 = 1",
            "l3 = 0",
            "l4 = 0",
            "l5 = \"\"",
            "if l1 != 0 goto label1",
            "if l2 == 0 goto label2",
            "label1:",
            "$stack12 = \"A\"",
            "l5 = $stack12",
            "label2:",
            "$stack11 = l3",
            "$stack11 = $stack11",
            "if $stack11 != 0 goto label3",
            "if l4 == 0 goto label4",
            "label3:",
            "$stack10 = \"B\"",
            "l5 = $stack10",
            "label4:",
            "$stack9 = l1",
            "$stack9 = $stack9",
            "if $stack9 != 0 goto label5",
            "if l3 == 0 goto label6",
            "label5:",
            "$stack8 = \"C\"",
            "l5 = $stack8",
            "label6:",
            "$stack7 = l4",
            "$stack7 = $stack7",
            "if $stack7 != 0 goto label7",
            "if l2 == 0 goto label8",
            "label7:",
            "$stack6 = \"D\"",
            "l5 = $stack6",
            "label8:",
            "return"));
    method = loadMethod(getMethodSignature("conditionalOp"));
    assertJimpleStmts(
        method,
        expectedBodyStmts(
            "l0 := @this: BooleanOperators",
            "l1 = 5",
            "l2 = \"\"",
            "if l1 >= 10 goto label1",
            "$stack3 = \"i less than 10\"",
            "goto label2",
            "label1:",
            "$stack3 = \"i greater than 10\"",
            "label2:",
            "l2 = $stack3",
            "return"));
  }

  public MethodSignature getMethodSignature(String methodName) {
    return identifierFactory.getMethodSignature(
        methodName, getDeclaredClassSignature(), "void", Collections.emptyList());
  }
}