package ru.trylogic.groovy.transform.inline

import groovy.inspect.swingui.AstNodeToScriptVisitor
import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.io.ReaderSource
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class InlineTransformation implements ASTTransformation {

    SourceUnit sourceUnit;
    
    static interface Handler {
        void transform(ASTNode node, SourceUnit sourceUnit);
    }

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {

        AnnotationNode annotationNode = nodes.find { it instanceof AnnotationNode } as AnnotationNode;
        def node = nodes.find { it != annotationNode };

        def closureExpression = annotationNode.getMember("value") as ClosureExpression
        def closureSource = convertClosureToSource(sourceUnit.getSource(), closureExpression);

        try {

            def script = "";

            def ast = sourceUnit.getAST()
            (ast.imports + ast.starImports + ast.staticImports.values() + ast.staticStarImports.values()).each { ImportNode importNode ->
                script += "${importNode.text}\n"
            }

            script += closureSource;
            
            Handler result = (Handler) (new GroovyShell()).evaluate(script);
            
            result.transform(node, sourceUnit);

            if((annotationNode.getMember("debug") as ConstantExpression)?.value == true) {
                //TODO should i use SLF4j here?
                
                def writer = new StringWriter();
                
                def visitor = new AstNodeToScriptVisitor(writer);
                
                (node as AnnotatedNode).annotations.removeAll { AnnotationNode annotatedClassNode ->
                    annotatedClassNode.classNode == ClassHelper.makeWithoutCaching(InlineTransform, false)
                }
                
                switch(node) {
                    case ClassNode:
                        visitor.visitClass(node as ClassNode);
                        break;
                    //TODO more cases
                }
                
                println writer.toString()
            }
        }
        catch(Exception e) {
            e.printStackTrace()
            sourceUnit.addError(new SyntaxException("", e, closureExpression.lineNumber, closureExpression.columnNumber));
        }
    }


    private void addError(String msg, ASTNode expr) {
        sourceUnit.getErrorCollector().addErrorAndContinue(
                new SyntaxErrorMessage(new SyntaxException(msg + '\n', expr.getLineNumber(), expr.getColumnNumber(), expr.getLastLineNumber(), expr.getLastColumnNumber()), sourceUnit)
        );
    }

    // Yep, copy-paste again (see MacroGroovy). 
    private String convertClosureToSource(ReaderSource source, ClosureExpression expression) {
        if (expression == null) throw new IllegalArgumentException("Null: expression");

        StringBuilder result = new StringBuilder();
        for (int x = expression.getLineNumber(); x <= expression.getLastLineNumber(); x++) {
            String line = source.getLine(x, null);
            if (line == null) {
                addError(
                        "Error calculating source code for expression. Trying to read line " + x + " from " + source.getClass(),
                        expression
                );
            }
            if (x == expression.getLastLineNumber()) {
                line = line.substring(0, expression.getLastColumnNumber() - 1);
            }
            if (x == expression.getLineNumber()) {
                line = line.substring(expression.getColumnNumber() - 1);
            }
            //restoring line breaks is important b/c of lack of semicolons
            result.append(line).append('\n');
        }


        String resultSource = result.toString();//.trim();
        if (!resultSource.startsWith("{")) {
            addError(
                    "Error converting ClosureExpression into source code. Closures must start with {. Found: " + source,
                    expression
            );
        }

        return resultSource;
    }
}



