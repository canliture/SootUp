package sootup.java.bytecode.interceptors.typeresolving;

import java.util.*;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.IdentifierFactory;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JFieldRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.JEnterMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JExitMonitorStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.jimple.visitor.AbstractStmtVisitor;
import sootup.core.model.Body;
import sootup.core.model.BodyUtils;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.NullType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.java.core.JavaIdentifierFactory;

public abstract class TypeChecker extends AbstractStmtVisitor<Stmt> {

  private AugEvalFunction evalFunction;
  private BytecodeHierarchy hierarchy;
  private Typing typing;
  private Body.BodyBuilder bodyBuilder;
  private Body body;
  private IdentifierFactory factory = JavaIdentifierFactory.getInstance();

  private static final Logger logger = LoggerFactory.getLogger(TypeChecker.class);

  public TypeChecker(
      Body.BodyBuilder builder, AugEvalFunction evalFunction, BytecodeHierarchy hierarchy) {
    this.bodyBuilder = builder;
    this.body = builder.build();
    this.evalFunction = evalFunction;
    this.hierarchy = hierarchy;
  }

  public abstract void visit(Value value, Type stdType, Stmt stmt);

  @Override
  public void caseInvokeStmt(@Nonnull JInvokeStmt stmt) {
    this.handleInvokeExpr(stmt.getInvokeExpr(), stmt);
  }

  @Override
  public void caseAssignStmt(@Nonnull JAssignStmt stmt) {
    Value lhs = stmt.getLeftOp();
    Value rhs = stmt.getRightOp();
    Type type_lhs = null;
    if (lhs instanceof Local) {
      type_lhs = this.typing.getType((Local) lhs);
    } else if (lhs instanceof JArrayRef) {
      visit(((JArrayRef) lhs).getIndex(), PrimitiveType.getInt(), stmt);
      ArrayType arrayType = null;
      Local base = ((JArrayRef) lhs).getBase();
      Type type_base = this.typing.getType(base);
      if (type_base instanceof ArrayType) {
        arrayType = (ArrayType) type_base;
      } else {
        if (rhs instanceof Local) {
          Type type_rhs = this.typing.getType((Local) rhs);
          // if base type of lhs is an object-like-type, retrieve its base type from array
          // allocation site.
          if (Type.isObjectLikeType(type_base)
              || (Type.isObject(type_base) && type_rhs instanceof PrimitiveType)) {
            Map<Local, List<Stmt>> defs = BodyUtils.collectDefs(bodyBuilder.getStmts());
            List<Stmt> defStmts = defs.get(base);
            boolean findDef = false;
            if (defStmts != null) {
              for (Stmt defStmt : defStmts) {
                if (defStmt instanceof JAssignStmt) {
                  Value arrExpr = ((JAssignStmt) defStmt).getRightOp();
                  if (arrExpr instanceof JNewArrayExpr) {
                    arrayType = (ArrayType) arrExpr.getType();
                    findDef = true;
                    break;
                  } else if (arrExpr instanceof JNewMultiArrayExpr) {
                    arrayType = ((JNewMultiArrayExpr) arrExpr).getBaseType();
                    findDef = true;
                    break;
                  }
                }
              }
            }
            if (!findDef) {
              arrayType = Type.makeArrayType(type_rhs, 1);
            }
          }
        }
        if (arrayType == null) {
          arrayType = Type.makeArrayType(type_base, 1);
        }
      }
      type_lhs = arrayType.getElementType();
      visit(base, arrayType, stmt);
      visit(lhs, type_lhs, stmt);
    } else if (lhs instanceof JFieldRef) {
      if (lhs instanceof JInstanceFieldRef) {
        visit(
            ((JInstanceFieldRef) lhs).getBase(),
            ((JInstanceFieldRef) lhs).getFieldSignature().getDeclClassType(),
            stmt);
      }
      type_lhs = lhs.getType();
    }

    if (rhs instanceof Local) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JArrayRef) {
      visit(((JArrayRef) rhs).getIndex(), PrimitiveType.getInt(), stmt);
      Local base = ((JArrayRef) rhs).getBase();
      ArrayType arrayType = null;
      Type type_base = typing.getType(base);
      if (type_base instanceof ArrayType) {
        arrayType = (ArrayType) type_base;
      } else {
        if (type_base instanceof NullType || Type.isObjectLikeType(type_base)) {
          Map<Local, List<Stmt>> defs = BodyUtils.collectDefs(bodyBuilder.getStmts());
          Deque<StmtLocalPair> worklist = new ArrayDeque<>();
          Set<StmtLocalPair> visited = new HashSet<>();
          worklist.add(new StmtLocalPair(stmt, base));
          Type sel = null;
          while (!worklist.isEmpty()) {
            StmtLocalPair pair = worklist.removeFirst();
            if (!visited.add(pair)) {
              continue;
            }
            List<Stmt> stmts = defs.get(pair.getLocal());
            for (Stmt s : stmts) {
              if (s instanceof JAssignStmt) {
                Value value = ((JAssignStmt) s).getRightOp();
                if (value instanceof JNewArrayExpr) {
                  sel = selectType(sel, ((JNewArrayExpr) value).getBaseType(), s);
                } else if (value instanceof JNewMultiArrayExpr) {
                  sel = selectType(sel, ((JNewMultiArrayExpr) value).getBaseType(), s);
                } else if (value instanceof Local) {
                  worklist.add(new StmtLocalPair(s, (Local) value));
                } else if (value instanceof JCastExpr) {
                  worklist.add(new StmtLocalPair(s, (Local) ((JCastExpr) value).getOp()));
                }
              }
            }
          }
          if (sel == null) {
            sel = type_base;
          }
          arrayType = Type.makeArrayType(sel, 1);
        }
      }
      Type type_rhs = arrayType.getElementType();
      visit(base, arrayType, stmt);
      visit(rhs, type_rhs, stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JInstanceFieldRef) {
      visit(
          ((JInstanceFieldRef) rhs).getBase(),
          ((JInstanceFieldRef) rhs).getFieldSignature().getDeclClassType(),
          stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof AbstractBinopExpr) {
      this.handleBinopExpr((AbstractBinopExpr) rhs, type_lhs, stmt);
    } else if (rhs instanceof AbstractInvokeExpr) {
      this.handleInvokeExpr((AbstractInvokeExpr) rhs, stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JCastExpr) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JInstanceOfExpr) {
      visit(((JInstanceOfExpr) rhs).getOp(), factory.getType("java.lang.Object"), stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNewArrayExpr) {
      visit(((JNewArrayExpr) rhs).getSize(), PrimitiveType.getInt(), stmt);
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNewMultiArrayExpr) {
      for (int i = 0; i < ((JNewMultiArrayExpr) rhs).getSizeCount(); i++) {
        visit(((JNewMultiArrayExpr) rhs).getSize(i), PrimitiveType.getInt(), stmt);
      }
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JLengthExpr) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof JNegExpr) {
      visit(((JNegExpr) rhs).getOp(), type_lhs, stmt);
    } else if (rhs instanceof JNewExpr) {
      visit(rhs, type_lhs, stmt);
    } else if (rhs instanceof Constant) {
      if (!(rhs instanceof NullConstant)) {
        visit(rhs, type_lhs, stmt);
      }
    }
  }

  @Override
  public void caseEnterMonitorStmt(@Nonnull JEnterMonitorStmt stmt) {
    visit(stmt.getOp(), factory.getType("java.lang.Object"), stmt);
  }

  @Override
  public void caseExitMonitorStmt(@Nonnull JExitMonitorStmt stmt) {
    visit(stmt.getOp(), factory.getType("java.lang.Object"), stmt);
  }

  @Override
  public void caseIfStmt(@Nonnull JIfStmt stmt) {
    handleBinopExpr(stmt.getCondition(), PrimitiveType.getBoolean(), stmt);
  }

  @Override
  public void caseSwitchStmt(@Nonnull JSwitchStmt stmt) {
    visit(stmt.getKey(), PrimitiveType.getInt(), stmt);
  }

  @Override
  public void caseReturnStmt(@Nonnull JReturnStmt stmt) {
    visit(stmt.getOp(), body.getMethodSignature().getType(), stmt);
  }

  @Override
  public void caseThrowStmt(@Nonnull JThrowStmt stmt) {
    visit(stmt.getOp(), factory.getType("java.lang.Throwable"), stmt);
  }

  public Body.BodyBuilder getBuilder() {
    return this.bodyBuilder;
  }

  public Body getBody() {
    return this.body;
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public AugEvalFunction getFuntion() {
    return this.evalFunction;
  }

  public BytecodeHierarchy getHierarchy() {
    return this.hierarchy;
  }

  public Typing getTyping() {
    return this.typing;
  }

  public void setTyping(Typing typing) {
    this.typing = typing;
  }

  private void handleInvokeExpr(AbstractInvokeExpr expr, Stmt stmt) {
    MethodSignature signature = expr.getMethodSignature();
    if (expr instanceof AbstractInstanceInvokeExpr) {
      visit(((AbstractInstanceInvokeExpr) expr).getBase(), signature.getDeclClassType(), stmt);
    }
    for (int i = 0; i < expr.getArgCount(); i++) {
      visit(expr.getArg(i), signature.getParameterTypes().get(i), stmt);
    }
  }

  private void handleBinopExpr(AbstractBinopExpr expr, Type type, Stmt stmt) {
    Value op1 = expr.getOp1();
    Value op2 = expr.getOp2();
    Type t1 = evalFunction.evaluate(typing, op1, stmt, body);
    Type t2 = evalFunction.evaluate(typing, op2, stmt, body);
    if (expr instanceof AbstractConditionExpr
        || expr instanceof AbstractFloatBinopExpr
        || expr instanceof JShlExpr
        || expr instanceof JShrExpr
        || expr instanceof JUshrExpr) {
      if (expr instanceof JEqExpr || expr instanceof JNeExpr) {
        if (!(t1 instanceof PrimitiveType.BooleanType && t2 instanceof PrimitiveType.BooleanType)
            && t1 instanceof PrimitiveType.IntType) {
          visit(op1, PrimitiveType.getInt(), stmt);
          visit(op2, PrimitiveType.getInt(), stmt);
        }
      } else {
        if (type instanceof PrimitiveType.IntType) {
          visit(op1, PrimitiveType.getInt(), stmt);
          visit(op2, PrimitiveType.getInt(), stmt);
        }
      }
    } else if (expr instanceof JXorExpr || expr instanceof JOrExpr || expr instanceof JAndExpr) {
      visit(op1, type, stmt);
      visit(op2, type, stmt);
    }
  }

  // select the type with bigger bit size
  public Type selectType(Type preType, Type newType, Stmt stmt) {
    if (preType == null || preType.equals(newType)) {
      return newType;
    }
    Type sel;
    if (Type.getValueBitSize(newType) > Type.getValueBitSize(preType)) {
      sel = newType;
    } else {
      sel = preType;
    }
    logger.warn(
        "Conflicting array types at "
            + stmt
            + " in "
            + body.getMethodSignature()
            + ". Its base type may be "
            + preType
            + " or "
            + newType
            + ". Select: "
            + sel);
    return sel;
  }
}