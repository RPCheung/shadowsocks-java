package io.github.rpcheung.shadowsocks.utils;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.ZipScanner;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public enum JarUtils {
    utils;

    public String[] jarScanner(File path) {
        ZipScanner scanner = new ZipScanner();
        scanner.setIncludes(new String[]{"**\\*.class"});
        scanner.setExcludes(new String[]{"**\\*$*.class"});
        scanner.setSrc(path);
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] includedFiles = scanner.getIncludedFiles();
        String[] classes = new String[includedFiles.length];
        int i = 0;
        for (String includedFile : includedFiles) {
            classes[i] = includedFile.substring(0, includedFile.indexOf(".class"))
                    .replace("/", ".").trim();
            i++;
        }
        return classes;
    }

    public URL[] findJar(File path) throws Exception {
        DirectoryScanner scanner = new DirectoryScanner();
        //scanner.setIncludes(new String[]{"**\\*.jar"});
        scanner.setIncludes(new String[]{"*.jar"});
        scanner.setBasedir(path);
        scanner.setCaseSensitive(true);
        scanner.scan();
        String[] includedFiles = scanner.getIncludedFiles();
        URL[] urls = new URL[includedFiles.length];
        int i = 0;
        for (String includedFile : includedFiles) {
            urls[i] = new File(path, includedFile).toURI().toURL();
            i++;
        }
        return urls;
    }

    public void addURL(URL file) throws Exception {
        getAddURLMethod().invoke(Thread.currentThread().getContextClassLoader(), file);
    }

    public Method getAddURLMethod() throws Exception {
        Method method = URLClassLoader.class
                .getDeclaredMethod("addURL", URL.class);
        method.setAccessible(Boolean.TRUE);
        return method;

    }
}
