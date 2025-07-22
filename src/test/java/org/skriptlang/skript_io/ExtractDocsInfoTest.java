package org.skriptlang.skript_io;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractDocsInfoTest {
    
    public static void main(String[] args) {
        List<Described> elements = extractElements();
        for (Described element : elements) element.print(System.out);
    }
    
    private static List<Described> extractElements() {
        SkriptIO.testMode = true;
        final String basePackage = "org.skriptlang.skript_io.elements", source = "src/main/java/org/skriptlang/skript_io/elements/";
        File root = new File(source);
        assert root.exists() && root.isDirectory();
        List<Described> elements = new ArrayList<>();
        for (File file : Objects.requireNonNull(root.listFiles())) {
            if (!file.isDirectory()) continue;
            String folder = file.getName();
            elements.add(new CategoryHeader(folder));
            for (File sub : Objects.requireNonNull(file.listFiles())) {
                if (!sub.isDirectory()) continue;
                String type = sub.getName();
                elements.add(new Header(type));
                for (File listFile : Objects.requireNonNull(sub.listFiles())) {
                    String name = listFile.getName();
                    String stub = name.substring(0, name.indexOf('.'));
                    elements.add(new Element(basePackage, folder, type, name.substring(0, name.indexOf('.'))));
                    File test = new File("src/test/resources/" + stub + ".sk");
                    if (!test.exists()) {
                        try {
                            test.createNewFile();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return elements;
    }
    
    @Test
    public void extract() {
        List<Described> elements = extractElements();
        assert !elements.isEmpty();
        for (Described described : elements) {
            if (!(described instanceof Element element)) continue;
            Class<?> type = element.toClass();
            assert type != null;
            if (Modifier.isAbstract(type.getModifiers())) continue;
            Doc.of(type);
        }
    }
    
    public interface Described {
        
        void print(PrintStream stream);
        
    }
    
    record CategoryHeader(String name) implements Described {
        
        @Override
        public void print(PrintStream stream) {
            stream.println();
            stream.println("## " + name.substring(0, 1).toUpperCase() + name.substring(1));
        }
        
    }
    
    record Header(String name) implements Described {
        
        @Override
        public void print(PrintStream stream) {
            stream.println();
            stream.println("### " + name.substring(0, 1).toUpperCase() + name.substring(1));
        }
        
    }
    
    record Element(String base, String sort, String type, String name) implements Described {
        
        Class<?> toClass() {
            try {
                return Class.forName(base + "." + sort + "." + type + "." + name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        
        @Override
        public void print(PrintStream stream) {
            Class<?> type = toClass();
            if (type == null) return;
            if (Modifier.isAbstract(type.getModifiers())) return;
            Doc doc = Doc.of(type);
            stream.println();
            stream.println("#### " + doc.name);
            stream.println("Since `" + doc.since + "`");
            stream.println();
            for (String s : doc.description) stream.println(s);
            stream.println();
            stream.println("```sk");
            for (String example : doc.examples) stream.println(example);
            stream.println("```");
            stream.println();
        }
        
    }
    
    record Doc(String name, String[] description, String[] examples, String since) {
        
        public static Doc of(Class<?> type) {
            if (!type.isAnnotationPresent(Name.class)) throw new RuntimeException(type + " is missing doc annotations");
            return new Doc(
                type.getAnnotation(Name.class).value(),
                type.getAnnotation(Description.class).value(),
                type.getAnnotation(Examples.class).value(),
                type.getAnnotation(Since.class).value()[0]
            );
        }
        
    }
    
}
