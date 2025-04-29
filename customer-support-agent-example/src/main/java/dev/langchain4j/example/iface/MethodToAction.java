package dev.langchain4j.example.iface;

import com.microsoft.playwright.Page;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.function.Function;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MethodToAction {
    Class[] paraType() default {};
    String[] paraName() default {};
    String description() default "";
    String[] domains() default {};
    String pageFilter() default "";
}
