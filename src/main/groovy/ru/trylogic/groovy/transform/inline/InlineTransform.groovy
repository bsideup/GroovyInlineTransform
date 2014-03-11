package ru.trylogic.groovy.transform.inline

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass(["ru.trylogic.groovy.transform.inline.InlineTransformation"])
@interface InlineTransform {
    Class value();

    /**
     * will log generated source to stdout if true
     * @return
     */
    boolean debug() default false;
}
