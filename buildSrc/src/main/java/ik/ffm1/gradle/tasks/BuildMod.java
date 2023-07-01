package ik.ffm1.gradle.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ik.ffm1.gradle.extensions.ModExtension;

public class BuildMod extends DefaultTask {

    private List<File> core = new ArrayList<>();
    private File mod = null;
    private File launcher = null;

    @TaskAction
    public void exec() throws FileNotFoundException, IOException {
        if (!Validate.failed.isEmpty()) {
            for (String log : Validate.failed) {
                this.getLogger().error(log);
            }

            throw new GradleException("Validate failed.");
        }

        File dists = this.getProject().file("dists");

        dists.mkdirs();

        JarOutputStream output = new JarOutputStream(new FileOutputStream(new File(dists, this.mod.getName())));

        this.copy(output, this.launcher);
        this.write(output, "mod.jar", this.mod);

        for (File file : this.core) {
            this.write(output, "META-INF/core/" + file.getName(), this.postProcess(file));
        }

        output.close();
    }

    public void core(Project project) {
        project.getTasks().named("jar", task -> {
            this.getInputs().file(((Jar) task).getArchiveFile().get().getAsFile());
            this.core.add(((Jar) task).getArchiveFile().get().getAsFile());
        });
    }

    public void mod(Project project) {
        project.getTasks().named("jar", task -> {
            this.getInputs().file(((Jar) task).getArchiveFile().get().getAsFile());
            this.mod = ((Jar) task).getArchiveFile().get().getAsFile();
            this.getOutputs().file(new File(this.getProject().file("dists"), this.mod.getName()));
        });
    }

    public void launcher(Project project) {
        project.getTasks().named("jar", task -> {
            this.getInputs().file(((Jar) task).getArchiveFile().get().getAsFile());
            this.launcher = ((Jar) task).getArchiveFile().get().getAsFile();
        });
    }

    private void copy(JarOutputStream jar, File src) throws IOException {
        JarFile source = new JarFile(src);

        source.stream().forEach(file -> {
            if (!file.isDirectory()) {
                try {
                    this.writeEntry(jar, file.getName(), source.getInputStream(file));
                } catch (IOException e) {}
            }
        });

        source.close();
    }

    private void write(JarOutputStream out, String filename, File file) throws FileNotFoundException, IOException {
        this.writeEntry(out, filename, new FileInputStream(file));
    }

    private void writeEntry(JarOutputStream out, String filename, InputStream stream) throws IOException {
        out.putNextEntry(new JarEntry(filename));

        byte[] buffer = new byte[4096];
        int readed;

        while ((readed = stream.read(buffer)) > 0) {
            out.write(buffer, 0, readed);
        }

        stream.close();
        out.closeEntry();
    }

    private void writeEntry(JarOutputStream out, String filename, byte[] buf) throws IOException {
        out.putNextEntry(new JarEntry(filename));
        out.write(buf);
        out.closeEntry();
    }

    @SuppressWarnings("unchecked")
    private File postProcess(File core) throws FileNotFoundException, IOException {
        File processed = new File(this.getTemporaryDir(), core.getName());
        final ModExtension mod = this.getProject().getExtensions().getByType(ModExtension.class);
        final String id = mod.get("id");
        final String pkg = mod.get("package") + ".mixin.";
        final String pkgPath = pkg.replace('.', '/');

        if (core.getName().startsWith("fabric") && !core.getName().equals("fabric-mixin.jar")) {
            final String version = core.getName().substring(6, core.getName().length() - 4);
            final String verPkg = pkg + "v" + version.replace('.', '_') + ".";

            JarFile source = new JarFile(core);
            JarOutputStream target = new JarOutputStream(new FileOutputStream(processed));

            source.stream().forEach(file -> {
                if (!file.isDirectory() && !file.getName().startsWith(pkgPath) && file.getName().endsWith(".class")) {
                    try {
                        this.writeEntry(target, file.getName(), MergeMixin.modifyClass(source.getInputStream(file), pkg, verPkg));
                    } catch (IOException e) {}
                }
            });

            source.close();
            target.close();

            return processed;
        } else if (core.getName().startsWith("forge")) {
            String mixin = "mixin." + id + ".json";
            List<String> mixins = new ArrayList<>();
            ByteArrayOutputStream jsonBuf = new ByteArrayOutputStream();

            JarFile source = new JarFile(core);
            JarOutputStream target = new JarOutputStream(new FileOutputStream(processed));

            source.stream().forEach(file -> {
                if (!file.isDirectory()) {
                    try {
                        if (file.getName().equals(mixin)) {
                            copy(source.getInputStream(file), jsonBuf);

                            return;
                        } else if (file.getName().endsWith(".class") && file.getName().startsWith(pkgPath)) {
                            mixins.add(file.getName().substring(pkgPath.length(), file.getName().length() - 6).replace('/', '.'));
                        }

                        this.writeEntry(target, file.getName(), source.getInputStream(file));
                    } catch (IOException e) {}
                }
            });

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Map<String, List<String>> json = gson.fromJson(new String(jsonBuf.toByteArray(), "UTF-8"), Map.class);

            json.put("mixins", mixins);

            target.putNextEntry(new JarEntry(mixin));
            target.write(gson.toJson(json).getBytes("UTF-8"));
            target.closeEntry();

            source.close();
            target.close();

            return processed;
        } else {
            return core;
        }
    }

    private void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int readed;

        while ((readed = in.read(buffer)) > 0) {
            out.write(buffer, 0, readed);
        }

        in.close();
    }
}
