package ik.ffm1.gradle.tasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;
import org.gradle.jvm.tasks.Jar;

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
            this.write(output, "META-INF/core/" + file.getName(), file);
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
}
