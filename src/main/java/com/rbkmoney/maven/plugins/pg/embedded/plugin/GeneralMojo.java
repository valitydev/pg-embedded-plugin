package com.rbkmoney.maven.plugins.pg.embedded.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class GeneralMojo extends AbstractMojo {

    @Parameter(defaultValue = "false")
    private boolean skipGoal;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skipGoal) {
            getLog().debug("Goal was skipped!");
        } else {
            doExecute();
        }
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

}
