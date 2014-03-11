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
    
    @InlineTransform(debug = true, value = { ASTNode node, SourceUnit source ->
        (node as ClassNode).addField("testField", ACC_PUBLIC, ClassHelper.STRING_TYPE, new ConstantExpression("Groovy is awesome!"))
    })
    static class TestClass {}
    
    public void testMethod() {
        
        def instance = new TestClass();
        
        assertEquals("Groovy is awesome!", instance.testField);
    }
}