package dev.vality.maven.plugins.pg.embedded.plugin;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class GeneralMojo extends AbstractMojo {

    private static final String POSTGRES_STATE_KEY = GeneralMojo.class.getName() + ".postgresState";

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

    protected PostgresRuntimeState getPostgresRuntimeState() {
        return (PostgresRuntimeState) getPluginContext().get(POSTGRES_STATE_KEY);
    }

    protected void setPostgresRuntimeState(PostgresRuntimeState state) {
        getPluginContext().put(POSTGRES_STATE_KEY, state);
    }

    protected void clearPostgresRuntimeState() {
        getPluginContext().remove(POSTGRES_STATE_KEY);
    }

    protected void removeShutdownHook(final Thread shutdownHook) {
        if (shutdownHook == null) {
            return;
        }

        try {
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
        } catch (IllegalStateException ignored) {
            // JVM is already shutting down.
        }
    }

    protected static final class PostgresRuntimeState {

        private final EmbeddedPostgres embeddedPostgres;
        private final Thread shutdownHook;

        protected PostgresRuntimeState(
                final EmbeddedPostgres embeddedPostgres,
                final Thread shutdownHook
        ) {
            this.embeddedPostgres = embeddedPostgres;
            this.shutdownHook = shutdownHook;
        }

        public EmbeddedPostgres getEmbeddedPostgres() {
            return embeddedPostgres;
        }

        public Thread getShutdownHook() {
            return shutdownHook;
        }
    }
}
