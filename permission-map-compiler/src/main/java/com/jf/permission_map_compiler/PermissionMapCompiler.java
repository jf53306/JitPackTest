package com.jf.permission_map_compiler;

import com.google.auto.service.AutoService;
import com.jf.permission_map_anno.PermissionMap;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by jf on 2019/1/11.
 */
@AutoService(Processor.class)
public class PermissionMapCompiler extends AbstractProcessor{


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {


        Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(PermissionMap.class);

        if (elementsAnnotatedWith==null || elementsAnnotatedWith.isEmpty()){
            return false;
        }



        Iterator<? extends Element> iterator = elementsAnnotatedWith.iterator();
        Map<String,String[]> maps=new HashMap<>();
        while (iterator.hasNext()){
            final Element next = iterator.next();
            String key = next.getSimpleName().toString();
            PermissionMap annotation = next.getAnnotation(PermissionMap.class);
            maps.put(key,annotation.value());
        }

//        try {
//            JavaFileObject object=processingEnv.getFiler().createSourceFile("com.taikang.tailife.MyPermissionManager");
//            Writer writer = object.openWriter();
//            writer.write(generateCode(maps));
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            generateMMMCode(maps).writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    public JavaFile generateMMMCode(Map<String,String[]> maps){
        MethodSpec.Builder mapPermissionMethod=MethodSpec
                .methodBuilder("mapPermission")
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .returns(Map.class);
        mapPermissionMethod
                .addStatement("$T<$T,$T[]> mmm= new $T<>()",Map.class,String.class,String.class,HashMap.class);
        Iterator<Map.Entry<String, String[]>> iterator = maps.entrySet().iterator();
        while (iterator.hasNext()){
            final Map.Entry<String, String[]> next = iterator.next();
            mapPermissionMethod.addStatement("mmm.put($S,"+generateStrings(next.getValue())+")",next.getKey());
        }
        mapPermissionMethod.addStatement("return mmm");

        TypeSpec typeSpec=TypeSpec.classBuilder("PermissionBuilder")
                .addMethod(mapPermissionMethod.build())
                .build();

        return JavaFile.builder("com.taikang.tailife",typeSpec).build();
    }

    public String generateCode(Map<String,String[]> maps){
        StringBuilder builder=new StringBuilder("package learn.jf.mypermission;\n");
        builder.append("public class MyPermissionManager {\n")
                .append("\tpublic static String[] getPermissions(String classPath){\n")
                .append("\t\tswitch(classPath){\n");

        Iterator<Map.Entry<String, String[]>> iterator = maps.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String[]> next = iterator.next();
            builder.append("\t\t\tcase \""+next.getKey()+"\":\n")
                    .append("\t\t\treturn "+generateStrings(next.getValue())).append(";\n");
        }

        builder.append("\t\t}\n")
                .append("\t\treturn null;\n")
                .append("\t}\n\n")
                .append(generatePlugin())
                .append("\n}");

        return builder.toString();
    }

    public String generatePlugin(){
        StringBuilder builder=new StringBuilder("\tprivate static String pluginName;\n");
        builder.append("\tpublic static void registPlugin(String name){\n")
                .append("\t\tpluginName=name;\n")
                .append("\t}");
        return builder.toString();
    }


    public String generateStrings(String[] strings){

        if (strings==null || strings.length<1){
            return "new String[]{}";
        }

        StringBuilder builder=new StringBuilder("new String[]{");

        for (String str:strings){

            builder.append("\""+str+"\"");
            builder.append(",");
        }

        builder.deleteCharAt(builder.length()-1);

        return builder.append("}").toString();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(PermissionMap.class.getCanonicalName());
        return types;
    }

}
