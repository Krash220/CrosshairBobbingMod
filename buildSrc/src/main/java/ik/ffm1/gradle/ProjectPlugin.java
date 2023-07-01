package ik.ffm1.gradle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.specs.Specs;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.jvm.tasks.Jar;

import com.google.common.collect.ImmutableMap;

import ik.ffm1.gradle.extensions.ModExtension;
import ik.ffm1.gradle.tasks.BuildMod;
import ik.ffm1.gradle.tasks.MergeMixin;
import ik.ffm1.gradle.tasks.ProcessSources;
import ik.ffm1.gradle.tasks.RenamePackage;
import ik.ffm1.gradle.tasks.Validate;

public class ProjectPlugin implements Plugin<Project> {

    @Override
    public void apply(Project root) {
        Logger logger = root.getLogger();

        logger.lifecycle("Welcome to Forge & Fabric Mixin One!");

        root.getDependencies().add("implementation", root.project(":game:api"));

        File prop = root.file("mod.properties");

        if (!prop.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(prop);
                InputStream input = ProjectPlugin.class.getResourceAsStream("/mod.properties");

                byte[] bytes = new byte[4096];
                int read;

                while ((read = input.read(bytes)) > 0) {
                    fos.write(bytes, 0, read);
                }

                input.close();
                fos.close();
            } catch (IOException e) {}
        }

        Properties properties = new Properties();

        try {
            FileInputStream fis = new FileInputStream(prop);

            properties.load(fis);
            fis.close();
        } catch (IOException e) {}

        Map<String, String> expand = new HashMap<>();

        for (Entry<Object, Object> entry : properties.entrySet()) {
            expand.put(entry.getKey().toString().toUpperCase(), entry.getValue().toString());
        }

        root.allprojects(project -> {
            project.getExtensions().add("mod", new ModExtension(properties));
        });

        String pkg = properties.getProperty("mod_package", "");
        String ep = properties.getProperty("mod_entrypoint", "");

        if (pkg.equals("")) {
            if (ep.equals("")) {
                logger.error("Please set 'mod_package' in the file 'mod.properties' and run 'initialization' task.");
            } else {
                logger.error("Please set 'mod_package' in the file 'mod.properties' and run 'renamePackage' task.");
            }
        }

        if (ep.equals("")) {
            root.getTasks().create("initialization", RenamePackage.class, task -> {
                task.setGroup("_Mod_");
                task.setInit(true);
            });

            return;
        }

        root.getTasks().create("renamePackage", RenamePackage.class, task -> {
            task.setGroup("_Mod_");
        });

        root.getTasks().withType(Jar.class, task -> {
            task.getArchiveBaseName().set(properties.getProperty("mod_name").replace(" ", ""));
            task.getArchiveVersion().set(properties.getProperty("mod_version"));
        });

        root.allprojects(project -> {
            Object ext = project.getExtensions().getByName("java");

            if (ext instanceof JavaPluginExtension) {
                JavaPluginExtension java = (JavaPluginExtension) ext;
                java.getSourceSets().all(sourceSet -> {
                    TaskProvider<ProcessSources> provider = project.getTasks().register(sourceSet.getTaskName("process", "Sources"), ProcessSources.class, task -> {
                        File output = new File(project.getBuildDir(), "sources/" + sourceSet.getName());

                        task.from(sourceSet.getJava());
                        task.into(output);
                        task.expand(expand, detail -> {
                            detail.getEscapeBackslash().set(true);
                        });

                        task.getOutputs().upToDateWhen(Specs.SATISFIES_NONE);
                    });

                    project.getTasks().named(sourceSet.getCompileJavaTaskName(), task -> {
                        task.dependsOn(provider);
                        ((SourceTask) task).setSource(new File(project.getBuildDir(), "sources/" + sourceSet.getName()));
                    });
                    project.getTasks().named(sourceSet.getProcessResourcesTaskName(), task -> {
                        Copy copy = (Copy) task;
                        copy.from(sourceSet.getResources(), spec -> {
                            spec.include("**/*.properties", "**/*.json", "**/*.toml", "**/*.mcmeta", "META-INF/services/*");
                            spec.expand(expand, detail -> {
                                detail.getEscapeBackslash().set(true);
                            });
                            spec.setDuplicatesStrategy(DuplicatesStrategy.INCLUDE);
                        });

                        task.getOutputs().upToDateWhen(Specs.SATISFIES_NONE);
                    });
                });
            }
        });

        BuildMod build = root.getTasks().create("buildMod", BuildMod.class, task -> {
            task.setGroup("_Mod_");

            task.dependsOn(root.getTasks().named("assemble"));
            task.dependsOn(root.project(":modloader").getTasks().named("assemble"));

            task.mod(root);
            task.launcher(root.project(":modloader"));
        });

        Project mixin = root.project(":game:fabric-mixin", project -> {
            MergeMixin merge = project.getTasks().create("mergeMixin", MergeMixin.class);

            build.mustRunAfter(merge);

            project.getTasks().named("jar", task -> {
                merge.main(((Jar) task).getArchiveFile().get().getAsFile());
            });

            project.getTasks().named("assemble", task -> {
                task.finalizedBy(merge);
            });
        });

        Project game = root.project(":game");

        game.subprojects(project -> {
            String t = null;

            if (project.getName().startsWith("forge")) {
                t = "forge";
            } else if (project.getName().startsWith("fabric")) {
                t = "fabric";
            }

            final String type = t;

            if (type != null) {
                build.dependsOn(project.getTasks().named("assemble"));

                if (!project.getName().equals("fabric-mixin")) {
                    Validate validate = project.getTasks().create("validateApi", Validate.class, task -> {
                        task.dependsOn(game.project("api").getTasks().named("jar"));
                    });

                    game.project("api").getTasks().named("jar", jar -> {
                        validate.api(((Jar) jar).getArchiveFile().get().getAsFile());
                    });

                    project.getTasks().named("jar", jar -> {
                        File output = ((Jar) jar).getArchiveFile().get().getAsFile();

                        validate.impl(output);

                        if (type.equals("fabric")) {
                            ((MergeMixin) mixin.getTasks().getByName("mergeMixin")).mixin(project.getName().substring(6), ((Jar) jar).getArchiveFile().get().getAsFile());
                        }
                    });

                    project.getTasks().named("assemble", assemble -> {
                        assemble.finalizedBy(validate);
                    });

                    if (type.equals("fabric")) {
                        mixin.getTasks().named("assemble", task -> {
                            task.dependsOn(project.getTasks().named("assemble"));
                        });
                    }
                }

                build.core(project);
                project.apply(ImmutableMap.of("from", game.file(type + ".gradle")));
            }

            if (!project.getName().equals("api")) {
                project.getDependencies().add("runtimeOnly", root);
            }
        });

        Object ext = root.getExtensions().getByName("java");

        if (ext instanceof JavaPluginExtension) {
            JavaPluginExtension java = (JavaPluginExtension) ext;
            SourceSet main = java.getSourceSets().getByName("main");
            Project api = root.project(":game:api");
            JavaPluginExtension apiJava = (JavaPluginExtension) api.getExtensions().getByName("java");
            SourceSet apiMain = apiJava.getSourceSets().getByName("main");

            build.dependsOn(root.getTasks().register("devMainJar", Jar.class, task -> {
                task.doFirst(t -> {
                    root.file("dists").mkdirs();
                });

                task.dependsOn(":classes");
                task.dependsOn(":game:api:classes");

                task.from(main.getOutput());
                task.from(apiMain.getOutput());
                task.getDestinationDirectory().set(root.file("dists"));

                task.getArchiveClassifier().set("dev");
            }));
            build.dependsOn(root.getTasks().register("sourceMainJar", Jar.class, task -> {
                task.doFirst(t -> {
                    root.file("dists").mkdirs();
                });

                task.dependsOn(":processSources");
                task.dependsOn(":game:api:processSources");

                task.from(new File(root.getBuildDir(), "sources/" + main.getName()));
                task.from(new File(api.getBuildDir(), "sources/" + apiMain.getName()));
                task.getDestinationDirectory().set(root.file("dists"));

                task.getArchiveClassifier().set("sources");
            }));
        }
    }
}
