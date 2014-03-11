package ru.trylogic.groovy.transform.inline.tests

import groovy.transform.CompileStatic
import junit.framework.TestCase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.SourceUnit
import ru.trylogic.groovy.transform.inline.InlineTransform

import static org.objectweb.asm.Opcodes.*;

@CompileStatic // yep, it's ok with CompileStatic ;)
class BasicTest extends TestCase {
    
    static final String TRUTH = "Groovy is awesome!";
    
    @InlineTransform(debug = true, value = { ASTNode node, SourceUnit source ->
        // Ugly current class full qualified reference, i know. Will improve next time
        def expression = new ConstantExpression(ru.trylogic.groovy.transform.inline.tests.BasicTest.TRUTH)
        (node as ClassNode).addField("testField", ACC_PUBLIC, ClassHelper.STRING_TYPE, expression)
    })
    static class TestClass {
        
    }
    
    public void testMethod() {
        
        def instance = new TestClass();
        
        assertEquals("Groovy is awesome!", TRUTH);
    }
}