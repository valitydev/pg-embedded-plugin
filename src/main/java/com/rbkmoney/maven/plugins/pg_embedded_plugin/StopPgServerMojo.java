package com.rbkmoney.maven.plugins.pg_embedded_plugin;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;

/**
 * Class to stop the embedded server
 *
 * @author d.baykov
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.COMPILE, threadSafe = true)
public class StopPgServerMojo extends GeneralMojo {

    /** Instance of the PostgreSQL */
    private EmbeddedPostgres embeddedPostgres;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
            if (StartPgServerMojo.isRunning()) {
                getLog().info("Stopping the PostgreSQL server...");
                StartPgServerMojo.stopPgServer();
                getLog().info("The PostgreSQL server stopped");
            } else {
                getLog().info("The PostgreSQL server wasn't started!");
            }
        } catch (IOException e) {
            getLog().error("Error encountered while stopping the server ", e);
        }
    }
}