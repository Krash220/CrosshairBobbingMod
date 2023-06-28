package ik.ffm1.gradle.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ik.ffm1.gradle.extensions.ModExtension;

public class MergeMixin extends DefaultTask {

    private Map<String, File> mixinFiles = new LinkedHashMap<>();
    private File mainFile = null;

    @TaskAction
    public void exec() throws IOException {
        Project project = this.getProject();
        File tmp = this.getTemporaryDir();

        File tmpMainJar = new File(tmp, "fabric-mixin.jar");

        Files.copy(this.mainFile.toPath(), tmpMainJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

        JarOutputStream mixinOutput = new JarOutputStream(new FileOutputStream(this.mainFile));

        JarFile tmpInput = new JarFile(tmpMainJar);

        tmpInput.stream().forEach(file -> {
            if (!file.isDirectory()) {
                try {
                    this.writeFile(mixinOutput, new JarEntry(file.getName()), tmpInput.getInputStream(file));
                } catch (IOException e) {}
            }
        });

        tmpInput.close();

        ModExtension mod = project.getExtensions().getByType(ModExtension.class);
        String pkg = mod.get("package") + ".mixin.";
        String pkgPath = pkg.replace('.', '/');

        Map<String, List<String>> plugins = new LinkedHashMap<>();
        Map<String, Long> crcMap = new HashMap<>();

        for (Entry<String, File> entry : this.mixinFiles.entrySet()) {
            List<String> pluginList = new ArrayList<>();

            JarFile jar = new JarFile(entry.getValue());
            Enumeration<JarEntry> it = jar.entries();

            while (it.hasMoreElements()) {
                JarEntry file = it.nextElement();

                if (!file.isDirectory()) {
                    String fn = file.getName();

                    if (fn.equals("version" + entry.getKey() + "-refmap.json")) {
                        JarEntry json = new JarEntry("mixin." + mod.get("id") + ".refmap." + entry.getKey() + ".json");

                        this.writeFile(mixinOutput, json, jar.getInputStream(file));
                    } else if (fn.startsWith(pkgPath) && fn.endsWith(".class")) {
                        String clazz = fn.substring(0, fn.length() - 6).replace('/', '.');
                        Long crc = crcMap.get(clazz);

                        if (crc == null) {
                            crcMap.put(clazz, file.getCrc());

                            this.writeFile(mixinOutput, file, jar.getInputStream(file));
                        } else {
                            while (crc != null && crc != file.getCrc()) {
                                clazz += '_';

                                crc = crcMap.get(clazz);
                            }

                            if (crc == null) {
                                crcMap.put(clazz, file.getCrc());
                                fn = clazz.replace('.', '/') + ".class";

                                byte[] bytecode = this.modifyClass(jar.getInputStream(file), clazz);

                                mixinOutput.putNextEntry(new JarEntry(fn));
                                mixinOutput.write(bytecode);
                                mixinOutput.closeEntry();
                            }
                        }

                        pluginList.add(clazz.substring(pkg.length()));
                    }
                }
            }

            plugins.put(entry.getKey(), pluginList);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        mixinOutput.putNextEntry(new JarEntry("plugins.json"));
        mixinOutput.write(gson.toJson(plugins).getBytes("UTF-8"));
        mixinOutput.closeEntry();

        mixinOutput.close();
    }

    public void main(File main) {
        this.mainFile = main;
        this.getOutputs().file(main);
    }

    public void mixin(String version, File file) {
        this.mixinFiles.put(version, file);
        this.getInputs().file(file);
    }

    private byte[] modifyClass(InputStream bytecode, String targetClass) throws IOException {
        ClassReader cr = new ClassReader(bytecode);
        ClassNode node = new ClassNode();

        cr.accept(node, ClassReader.SKIP_FRAMES);

        String clazz = node.name;
        String target = targetClass.replace('.', '/');

        String clazzType = "L" + clazz + ";";
        String targetType = "L" + target + ";";

        node.name = target;

        for (FieldNode field : node.fields) {
            if (field.desc.equals(clazzType)) {
                field.desc = targetType;
            }
        }

        for (MethodNode method : node.methods) {
            method.desc = method.desc.replace(clazzType, targetType);

            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode f = (FieldInsnNode) insn;

                    if (f.owner.equals(clazz)) {
                        f.owner = target;
                    }
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode) insn;

                    if (m.owner.equals(clazz)) {
                        m.owner = target;
                    }

                    m.desc = m.desc.replace(clazzType, targetType);
                } else if (insn instanceof TypeInsnNode) {
                    TypeInsnNode t = (TypeInsnNode) insn;

                    if (t.desc.equals(clazz)) {
                        t.desc = target;
                    }
                } else if (insn instanceof LdcInsnNode) {
                    LdcInsnNode l = (LdcInsnNode) insn;

                    if (l.cst instanceof Type) {
                        Type t = (Type) l.cst;

                        if (t.getInternalName().equals(clazz)) {
                            l.cst = Type.getType(targetType);
                        }
                    }
                } else if (insn instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode invoke = (InvokeDynamicInsnNode) insn;

                    for (int i = 0; i < invoke.bsmArgs.length; i++) {
                        Object arg = invoke.bsmArgs[i];

                        if (arg instanceof Handle) {
                            Handle handle = (Handle) arg;

                            if (handle.getOwner().equals(clazz)) {
                                invoke.bsmArgs[i] = new Handle(handle.getTag(), target, handle.getName(), handle.getDesc(), handle.isInterface());
                            }
                        }
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);

        return cw.toByteArray();
    }

    private void writeFile(JarOutputStream out, JarEntry entry, InputStream stream) throws IOException {
        out.putNextEntry(entry);

        byte[] buffer = new byte[4096];
        int readed;

        while ((readed = stream.read(buffer)) > 0) {
            out.write(buffer, 0, readed);
        }

        stream.close();
        out.closeEntry();
    }
}
