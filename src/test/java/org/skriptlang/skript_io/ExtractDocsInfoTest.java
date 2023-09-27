package org.skriptlang.skript_io;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExtractDocsInfoTest {
    
    public static void main(String[] args) {
        final List<Element> elements = extractElements();
        for (final Element element : elements) {
            final Class<?> type = element.toClass();
            assert type != null;
            if (Modifier.isAbstract(type.getModifiers())) continue;
            final Doc doc = Doc.of(type);
            System.out.println();
            System.out.println("#### " + doc.name);
            System.out.println();
            System.out.println("Since `" + doc.since + "`");
            System.out.println();
            for (final String s : doc.description) System.out.println(s);
            System.out.println();
            System.out.println("```sk");
            for (final String example : doc.examples) System.out.println(example);
            System.out.println("```");
            System.out.println();
        }
    }
    
    private static List<Element> extractElements() {
        SkriptIO.testMode = true;
        final String basePackage = "org.skriptlang.skript_io.elements", source = "src/main/java/org/skriptlang/skript_io/elements/";
        final File root = new File(source);
        assert root.exists() && root.isDirectory();
        final List<Element> elements = new ArrayList<>();
        for (final File file : Objects.requireNonNull(root.listFiles())) {
            if (!file.isDirectory()) continue;
            final String folder = file.getName();
            for (final File sub : Objects.requireNonNull(file.listFiles())) {
                if (!sub.isDirectory()) continue;
                final String type = sub.getName();
                for (final File listFile : Objects.requireNonNull(sub.listFiles())) {
                    final String name = listFile.getName();
                    elements.add(new Element(basePackage, folder, type, name.substring(0, name.indexOf('.'))));
                }
            }
        }
        return elements;
    }
    
    @Test
    public void extract() {
        final List<Element> elements = extractElements();
        assert !elements.isEmpty();
        for (final Element element : elements) {
            final Class<?> type = element.toClass();
            assert type != null;
            if (Modifier.isAbstract(type.getModifiers())) continue;
            Doc.of(type);
        }
    }
    
    record Element(String base, String sort, String type, String name) {
        
        Class<?> toClass() {
            try {
                return Class.forName(base + "." + sort + "." + type + "." + name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        
    }
    
    record Doc(String name, String[] description, String[] examples, String since) {
        
        public static Doc of(Class<?> type) {
            return new Doc(
                type.getAnnotation(Name.class).value(),
                type.getAnnotation(Description.class).value(),
                type.getAnnotation(Examples.class).value(),
                type.getAnnotation(Since.class).value()
            );
        }
        
    }
    
}
