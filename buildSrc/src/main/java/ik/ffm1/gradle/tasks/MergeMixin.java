package ik.ffm1.gradle.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
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
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import com.google.gson.Gson;

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
        ModExtension mod = project.getExtensions().getByType(ModExtension.class);
        String pkg = mod.get("package") + ".mixin.";
        String pkgPath = pkg.replace('.', '/');

        Map<String, List<String>> plugins = new LinkedHashMap<>();

        for (Entry<String, File> entry : this.mixinFiles.entrySet()) {
            String verPkg = pkg + "v" + entry.getKey().replace('.', '_') + ".";
            String verPkgPath = verPkg.replace('.', '/');

            List<String> pluginList = new ArrayList<>();

            JarFile jar = new JarFile(entry.getValue());
            Enumeration<JarEntry> it = jar.entries();

            while (it.hasMoreElements()) {
                JarEntry file = it.nextElement();

                if (!file.isDirectory()) {
                    String fn = file.getName();

                    if (fn.equals("fabric" + entry.getKey() + "-refmap.json")) {
                        JarEntry json = new JarEntry("mixin." + mod.get("id") + ".refmap." + entry.getKey() + ".json");

                        this.writeFile(mixinOutput, json, modifyJson(jar.getInputStream(file), pkg, verPkg));
                    } else if (fn.startsWith(pkgPath) && fn.endsWith(".class")) {
                        String clazz = replaceHead(fn, pkgPath, verPkgPath);
                        JarEntry clazzEntry = new JarEntry(clazz);

                        this.writeFile(mixinOutput, clazzEntry, modifyClass(jar.getInputStream(file), pkg, verPkg));
                        pluginList.add(clazz.substring(0, clazz.length() - 6).substring(pkg.length()).replace('/', '.'));
                    }
                }
            }

            plugins.put(entry.getKey(), pluginList);
        }

        String mixins = new Gson().toJson(plugins);
        JarFile tmpInput = new JarFile(tmpMainJar);

        tmpInput.stream().forEach(file -> {
            if (!file.isDirectory()) {
                try {
                    if (file.getName().endsWith(".class")) {
                        this.writeFile(mixinOutput, new JarEntry(file.getName()), injectMixins(tmpInput.getInputStream(file), mixins));
                    } else {
                        this.writeFile(mixinOutput, new JarEntry(file.getName()), tmpInput.getInputStream(file));
                    }
                } catch (IOException e) {}
            }
        });
        tmpInput.close();
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

    private static byte[] modifyJson(InputStream json, String from, String to) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int readed;

        while ((readed = json.read(buffer)) > 0) {
            bos.write(buffer, 0, readed);
        }

        String str = new String(bos.toByteArray(), "UTF-8").replace(from.replace('.', '/'), to.replace('.', '/'));

        bos.close();

        return str.getBytes("UTF-8");
    }

    public static byte[] modifyClass(InputStream bytecode, String from, String to) throws IOException {
        ClassReader cr = new ClassReader(bytecode);
        ClassNode node = new ClassNode();

        cr.accept(node, ClassReader.SKIP_FRAMES);

        from = from.replace('.', '/');
        to = to.replace('.', '/');

        String fromPkg = "L" + from;
        String toPkg = "L" + to;

        node.name = replaceHead(node.name, from, to);

        for (FieldNode field : node.fields) {
            field.desc = replaceHead(field.desc, fromPkg, toPkg);
        }

        for (MethodNode method : node.methods) {
            method.desc = replaceHead(method.desc, fromPkg, toPkg);

            for (AbstractInsnNode insn = method.instructions.getFirst(); insn != null; insn = insn.getNext()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode f = (FieldInsnNode) insn;

                    f.owner = replaceHead(f.owner, from, to);
                } else if (insn instanceof MethodInsnNode) {
                    MethodInsnNode m = (MethodInsnNode) insn;

                    m.owner = replaceHead(m.owner, from, to);
                    m.desc = replaceHead(m.desc, fromPkg, toPkg);
                } else if (insn instanceof TypeInsnNode) {
                    TypeInsnNode t = (TypeInsnNode) insn;

                    t.desc = replaceHead(t.desc, from, to);
                } else if (insn instanceof LdcInsnNode) {
                    LdcInsnNode l = (LdcInsnNode) insn;

                    if (l.cst instanceof Type) {
                        Type t = (Type) l.cst;

                        if (t.getInternalName().startsWith(from)) {
                            l.cst = Type.getType(replaceHead(t.getInternalName(), fromPkg, toPkg));
                        }
                    }
                } else if (insn instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode invoke = (InvokeDynamicInsnNode) insn;

                    for (int i = 0; i < invoke.bsmArgs.length; i++) {
                        Object arg = invoke.bsmArgs[i];

                        if (arg instanceof Handle) {
                            Handle handle = (Handle) arg;

                            if (handle.getOwner().startsWith(from)) {
                                invoke.bsmArgs[i] = new Handle(handle.getTag(), replaceHead(handle.getOwner(), from, to), handle.getName(), handle.getDesc(), handle.isInterface());
                            }
                        }
                    }
                }
            }

            if (method.localVariables != null) {
                for (LocalVariableNode v : method.localVariables) {
                    v.desc = replaceHead(v.desc, fromPkg, toPkg);
                    v.signature = replaceHead(v.signature, fromPkg, toPkg);
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);

        return cw.toByteArray();
    }

    private static byte[] injectMixins(InputStream input, String mixins) throws IOException {
        ClassReader cr = new ClassReader(input);
        ClassNode node = new ClassNode();

        cr.accept(node, ClassReader.SKIP_FRAMES);

        for (MethodNode m : node.methods) {
            for (AbstractInsnNode n = m.instructions.getFirst(); n != null; n = n.getNext()) {
                if (n instanceof LdcInsnNode) {
                    LdcInsnNode l = (LdcInsnNode) n;

                    if (l.cst.equals("<MIXINS_JSON>")) {
                        l.cst = mixins;
                    }
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);

        return cw.toByteArray();
    }

    private static String replaceHead(String str, String from, String to) {
        if (str != null && str.startsWith(from)) {
            return to + str.substring(from.length());
        } else {
            return str;
        }
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

    private void writeFile(JarOutputStream out, JarEntry entry, byte[] buf) throws IOException {
        out.putNextEntry(entry);
        out.write(buf);
        out.closeEntry();
    }
}
