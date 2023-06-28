package ik.ffm1.gradle.tasks;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.Copy;
import org.gradle.internal.file.Deleter;
import org.gradle.language.base.internal.tasks.StaleOutputCleaner;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Not worth caching")
public class ProcessSources extends Copy {

    protected void copy() {
        boolean cleanedOutputs = StaleOutputCleaner.cleanOutputs(this.getDeleter(),
                this.getOutputs().getPreviousOutputFiles(), this.getDestinationDir());
        super.copy();
        if (cleanedOutputs) {
            this.setDidWork(true);
        }

    }

    @Inject
    protected Deleter getDeleter() {
        throw new GradleException("Decorator takes care of injection");
    }
}