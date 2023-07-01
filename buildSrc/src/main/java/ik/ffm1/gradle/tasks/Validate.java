package ik.ffm1.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class Validate extends DefaultTask {

    static final List<String> failed = new ArrayList<>();

    private File api = null;
    private File impl = null;

    @TaskAction
    public void exec() throws IOException {
        ZipFile api = new ZipFile(this.api);
        ZipFile impl = new ZipFile(this.impl);

        boolean error = false;
        Enumeration<? extends ZipEntry> e = api.entries();

        while (e.hasMoreElements()) {
            ZipEntry apiEntry = e.nextElement();

            if (!apiEntry.isDirectory() && apiEntry.getName().endsWith(".class")) {
                ZipEntry implEntry = impl.getEntry(apiEntry.getName());
                String clazz = apiEntry.getName().substring(0, apiEntry.getName().length() - 6).replace('/', '.');

                if (implEntry == null) {
                    if (!error) {
                        error = true;
                        failed.add(this.getProject().getPath());
                    }

                    failed.add(String.format(" - Class '%s' not found.", clazz));

                    continue;
                }

                ClassNode apiClass = new ClassNode();
                ClassNode implClass = new ClassNode();

                new ClassReader(api.getInputStream(apiEntry)).accept(apiClass, 0);
                new ClassReader(impl.getInputStream(implEntry)).accept(implClass, 0);

                ArrayList<String> apiData = read(apiClass);
                ArrayList<String> implData = read(implClass);

                for (String data : apiData) {
                    if (!implData.contains(data)) {
                        if (!error) {
                            error = true;
                            failed.add(this.getProject().getPath());
                        }

                        failed.add(String.format(" - %s in class '%s' not existed.", data, clazz));
                    }
                }
            }
        }

        api.close();
        impl.close();
    }

    public void api(File file) {
        this.api = file;
    }

    public void impl(File file) {
        this.impl = file;
    }

    private ArrayList<String> read(ClassNode node) {
        ArrayList<String> list = new ArrayList<>();

        List<FieldNode> fields = node.fields;

        fields.forEach(f -> {
            if ((f.access & Opcodes.ACC_PUBLIC) != 0) {
                list.add("Field '" + this.asm2name(f) + "'");
            }
        });

        List<MethodNode> methods = node.methods;

        methods.forEach(m -> {
            if ((m.access & Opcodes.ACC_PUBLIC) != 0) {
                if ((node.access & Opcodes.ACC_ABSTRACT) != 0 && this.asm2name(m).equals("public void <init>()")) {
                    return;
                }

                list.add("Method '" + this.asm2name(m) + "'");
            }
        });

        return list;
    }

    private String asm2name(MethodNode node) {
        Pattern descPattern = Pattern.compile("^\\((\\S*)\\)(\\S+)$");
        StringBuilder builder = new StringBuilder("public ");

        if ((node.access & Opcodes.ACC_STATIC) != 0) {
            builder.append("static ");
        }

        Matcher matcher = descPattern.matcher(node.desc);

        matcher.find();

        String[] args = this.getTypes(matcher.group(1));
        String ret = this.getTypes(matcher.group(2))[0];

        builder.append(ret).append(' ').append(node.name).append('(').append(String.join(", ", args)).append(')');

        return builder.toString();
    }

    private String asm2name(FieldNode node) {
        StringBuilder builder = new StringBuilder("public ");

        if ((node.access & Opcodes.ACC_STATIC) != 0) {
            builder.append("static ");
        }

        builder.append(this.getTypes(node.desc)[0]).append(' ').append(node.name);

        return builder.toString();
    }

    private String[] getTypes(String desc) {
        List<String> list = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        boolean isArray = false;
        boolean isClass = false;

        for (char c : desc.toCharArray()) {
            if (isClass) {
                if (c == ';') {
                    isClass = false;
                } else {
                    builder.append(c == '/' ? '.' : c);
                    continue;
                }
            } else {
                switch (c) {
                case 'V':
                    builder.append("void");
                    break;
                case 'Z':
                    builder.append("boolean");
                    break;
                case 'B':
                    builder.append("byte");
                    break;
                case 'C':
                    builder.append("char");
                    break;
                case 'S':
                    builder.append("short");
                    break;
                case 'I':
                    builder.append("int");
                    break;
                case 'J':
                    builder.append("long");
                    break;
                case 'F':
                    builder.append("float");
                    break;
                case 'D':
                    builder.append("double");
                    break;
                case 'L':
                    isClass = true;
                    continue;
                case '[':
                    isArray = true;
                    continue;
                }
            }

            if (isArray) {
                builder.append("[]");
            }

            String type = builder.toString();

            if (type.startsWith("java.lang.")) {
                type = type.substring(10);
            }

            list.add(type);
            builder.setLength(0);
        }

        return list.toArray(new String[0]);
    }
}
