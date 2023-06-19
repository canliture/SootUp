package sootup.core.util;

import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.signatures.MethodSignature;

import java.util.*;

public class ICFGDotExporter {

    static final StringBuilder sb = new StringBuilder();
    public static String buildICFGGraph(ArrayList<StmtGraph> stmtGraphSet, LinkedHashSet<MethodSignature> sortedMethodSignature){
        DotExporter.buildDiGraphObject(sb);
        int i = 0;
        Map<Integer,MethodSignature> calls;
        for(StmtGraph stmtGraph : stmtGraphSet){
            calls = computeCalls(stmtGraphSet);
            List<MethodSignature> list = new ArrayList<>(sortedMethodSignature);
            String graph = DotExporter.buildGraph(stmtGraph, true, calls,list.get(i));
            sb.append(graph + "\n");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * This method finds out all the calls made in the given StmtGraphs, so it can be edged to
     * other methods.
     * */
    private static Map<Integer,MethodSignature> computeCalls(ArrayList<StmtGraph> stmtGraphSet){
        Map<Integer,MethodSignature> calls = new HashMap<>();
        for(StmtGraph stmtGraph : stmtGraphSet){
            Collection<? extends BasicBlock<?>> blocks;
            try {
                blocks = stmtGraph.getBlocksSorted();
            } catch (Exception e) {
                blocks = stmtGraph.getBlocks();
            }
            for(BasicBlock<?> block : blocks){
                List<Stmt> stmts = block.getStmts();
                for(Stmt stmt : stmts){
                    if(stmt.containsInvokeExpr()){
                        MethodSignature methodSignature = stmt.getInvokeExpr().getMethodSignature();
                        int hashCode = stmt.hashCode();
                        calls.put(hashCode,methodSignature);
                    }
                }
            }
        }
        return calls;
    }

}
