package com.rbkmoney.maven.plugins.pg_embedded_plugin;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

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
    /** PostgreSQL version */
    @Parameter(property = "shutdown_timeout", defaultValue = "500")
    private int shutdownTimeout;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
            if (StartPgServerMojo.isRunning()) {
                getLog().info("Stopping the PostgreSQL server...");
                StartPgServerMojo.stopPgServer();
                wait(shutdownTimeout);
                getLog().info("The PostgreSQL server stopped");
            } else {
                getLog().info("The PostgreSQL server wasn't started!");
            }
        } catch (IOException e) {
            getLog().error("Error encountered while stopping the server ", e);
        } catch (InterruptedException e) {
            getLog().error("Wait error while stopping the server ", e);
        }
    }
}