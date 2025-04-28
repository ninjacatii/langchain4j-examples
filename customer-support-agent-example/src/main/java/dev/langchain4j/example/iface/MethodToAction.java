package dev.langchain4j.example.iface;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MethodToAction {
    Class[] paraType() default {};
    String[] paraName() default {};
    String description() default "";
}
