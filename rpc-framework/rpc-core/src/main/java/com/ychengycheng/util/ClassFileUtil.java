package com.ychengycheng.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Valar Morghulis
 * @Date 2023/10/2
 */
public class ClassFileUtil {
    public static List<String> getAllClassNames(String packageName) {
        // 1、通过packageName获得绝对路径

        String basePath = packageName.replaceAll("\\.", "/");
        URL url = ClassLoader.getSystemClassLoader().getResource(basePath);
        if (url == null) {
            throw new RuntimeException("文件扫描时，发现路径不存在.");
        }
        String absolutePath = url.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(absolutePath, classNames, basePath);
        return classNames;
    }

    private static List<String> recursionFile(String absolutePath, List<String> classNames, String basePath) {
        //获取文件
        File file = new File(absolutePath);
        // 判断文件是否是文件夹
        if (file.isDirectory()){
            // 找到文件夹的所有的文件
            File[] children = file.listFiles(pathname -> pathname.isDirectory() || pathname.getPath().contains(".class"));
            if(children == null || children.length == 0){
                return classNames;
            }
            for (File child : children) {
                if(child.isDirectory()){
                    // 递归调用
                    recursionFile(child.getAbsolutePath(),classNames,basePath);
                } else {
                    // 文件 --> 类的权限定名称
                    String className = getClassNameByAbsolutePath(child.getAbsolutePath(),basePath);
                    classNames.add(className);
                }
            }

        } else {
            // 文件 --> 类的权限定名称
            String className = getClassNameByAbsolutePath(absolutePath,basePath);
            classNames.add(className);
        }
        return classNames;
    }

    private static String getClassNameByAbsolutePath(String absolutePath, String basePath) {
        String fileName = absolutePath
                .substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\")))
                .replaceAll("\\\\",".");

        fileName = fileName.substring(0,fileName.indexOf(".class"));
        return fileName;
    }
}
