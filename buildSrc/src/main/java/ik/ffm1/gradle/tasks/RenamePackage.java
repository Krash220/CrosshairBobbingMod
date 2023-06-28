package ik.ffm1.gradle.tasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.TaskAction;

import ik.ffm1.gradle.utils.PropertiesOrdered;

public class RenamePackage extends DefaultTask {

    private boolean isInit;

    @TaskAction
    public void exec() {
        Project root = this.getProject();
        File prop = root.file("mod.properties");
        Properties properties = new PropertiesOrdered();

        try {
            FileInputStream fis = new FileInputStream(prop);

            properties.load(fis);
            fis.close();
        } catch (IOException e) {}

        if (this.isInit) {
            properties.setProperty("mod_entrypoint", "ik.ffm1.MainMod");
        }

        String pkg = properties.getProperty("mod_package", "");
        String ep = properties.getProperty("mod_entrypoint", "");

        if (pkg.endsWith(".")) {
            pkg = pkg.substring(0, pkg.length() - 1);
        }

        if (!this.validatePackage(pkg)) {
            throw new GradleException("Illegal package name.");
        }

        if (ep.equals("")) {
            throw new GradleException("Unable to get the old package name from 'mod_entrypoint'.");
        }

        String newPackage = pkg;
        String newPath = newPackage.replace('.', '/');

        String oldPackage = ep.substring(0, ep.lastIndexOf('.'));
        String oldPath = oldPackage.replace('.', '/');
        String entryPoint = ep.substring(oldPackage.length() + 1);

        if (oldPackage.equals(pkg)) {
            return;
        }

        File temp = this.getTemporaryDir();

        this.getProject().allprojects(project -> {
            if (project.getName().startsWith("version")) {
                return;
            }

            this.getLogger().lifecycle("Rename package for project {}", project.getParent() == null ? "root" : project.getPath());

            Object ext = project.getExtensions().getByName("java");

            if (ext instanceof JavaPluginExtension) {
                JavaPluginExtension java = (JavaPluginExtension) ext;
                java.getSourceSets().forEach(sourceSet -> {
                    for (File dir : sourceSet.getAllJava().getSrcDirs()) {
                        if (!dir.isDirectory()) {
                            continue;
                        }

                        File oldPkgDir = new File(dir, oldPath);
                        File newPkgDir = new File(dir, newPath);

                        if (!oldPkgDir.isDirectory()) {
                            continue;
                        }

                        String[] files = this.listFiles(oldPkgDir);

                        for (String fn : files) {
                            this.getLogger().lifecycle("Moving {}/{} to {}/{}", oldPath, fn, newPath, fn);
                            this.moveFile(new File(oldPkgDir, fn), new File(temp, fn), oldPackage, newPackage);
                        }

                        this.deleteEmptySubDir(oldPkgDir);

                        File delete = oldPkgDir;

                        for (int i = oldPath.split("/").length; i > 0; i--) {
                            if (delete.delete()) {
                                delete = delete.getParentFile();
                            } else {
                                break;
                            }
                        }

                        for (String fn : files) {
                            this.moveFile(new File(temp, fn), new File(newPkgDir, fn), "", "");
                        }
                    }
                });
            }
        });

        properties.setProperty("mod_entrypoint", pkg + "." + entryPoint);

        try {
            FileOutputStream fos = new FileOutputStream(prop);

            properties.store(fos, null);
            fos.close();
        } catch (IOException e) {}
    }

    public void setInit(boolean init) {
        this.isInit = init;
    }

    private boolean validatePackage(String pkg) {
        Pattern pattern = Pattern.compile("^[a-zA-Z]\\w*$");
        String[] parts = pkg.split("\\.");

        for (String part : parts) {
            if (!pattern.matcher(part).matches()) {
                return false;
            }
        }

        return true;
    }

    private String[] listFiles(File path) {
        return this.listFiles("", path);
    }

    private String[] listFiles(String root, File path) {
        if (!path.isDirectory()) {
            return new String[0];
        }

        ArrayList<String> list = new ArrayList<>();

        for (File file : path.listFiles()) {
            if (file.isDirectory()) {
                list.addAll(Arrays.asList(listFiles(root + file.getName() + "/", file)));
            } else {
                list.add(root + file.getName());
            }
        }

        return list.toArray(new String[0]);
    }

    private void moveFile(File from, File to, String pkgFrom, String pkgTo) {
        to.getParentFile().mkdirs();

        try {
            FileInputStream fis = new FileInputStream(from);
            FileOutputStream fos = new FileOutputStream(to);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            byte[] buffer = new byte[4096];
            int readed = -1;

            while ((readed = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, readed);
            }

            buffer = bos.toByteArray();
            fis.close();
            bos.close(); // ?

            if (from.getName().endsWith(".java") && pkgTo.length() > 0) {
                String content = new String(buffer, "UTF-8").replace(pkgFrom, pkgTo);

                buffer = content.getBytes("UTF-8");
            }

            fos.write(buffer);
            fos.close();
            from.delete();
        } catch (IOException e) {
            throw new GradleException("Fatal error", e);
        }
    }

    private void deleteEmptySubDir(File path) {
        for (File f : path.listFiles()) {
            if (f.isDirectory()) {
                deleteEmptySubDir(f);

                f.delete();
            }
        }
    }
}
