package com.jf.permission_map_anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jf on 2019/1/11.
 */

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface PermissionMap {

    String[] value();

}
