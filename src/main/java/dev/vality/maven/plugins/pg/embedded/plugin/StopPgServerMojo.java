package dev.vality.maven.plugins.pg.embedded.plugin;

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
@Mojo(name = "stop", defaultPhase = LifecyclePhase.PROCESS_SOURCES, threadSafe = true)
public class StopPgServerMojo extends GeneralMojo {

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
            if (StartPgServerMojo.isRunning(this)) {
                getLog().info("Stopping the PostgreSQL server...");
                StartPgServerMojo.stopPgServer(this);
                getLog().info("The PostgreSQL server stopped");
            } else {
                getLog().info("The PostgreSQL server wasn't started!");
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error encountered while stopping the server", e);
        }
    }
}
