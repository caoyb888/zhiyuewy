package com.zhaoxinms.resi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 住宅物业项目数据隔离注解
 * <p>
 * 作用于 Controller 或 Service 方法上，AOP 自动拦截并为查询追加 project_id 过滤条件。
 * <p>
 * 使用示例：
 * <pre>
 *   &#064;ResiProjectScope
 *   public List&lt;ResiRoom&gt; list(QueryWrapper&lt;ResiRoom&gt; qw) { ... }
 * </pre>
 *
 * @author zhaoxinms
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResiProjectScope {

    /**
     * 项目ID字段名，默认 "project_id"
     */
    String projectColumn() default "project_id";

    /**
     * 是否忽略数据隔离（用于超管或特殊查询），默认 false
     */
    boolean ignore() default false;
}
