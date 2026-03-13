package dev.vality.maven.plugins.pg.embedded.plugin;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Class to start the embedded server
 *
 * @author d.baykov
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class StartPgServerMojo extends GeneralMojo {

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private String projectBuildDir;

    /**
     * Directory that will host the service files of postgresql
     */
    @Deprecated
    @Parameter
    private String dbDir;

    /**
     * Directory that will host the service files of postgresql
     */
    @Parameter
    private String dir;

    /**
     * Port on which the instance will be raised
     */
    @Parameter(required = true)
    private int port;

    /**
     * Database name
     */
    @Deprecated
    @Parameter
    private String dbName;

    /**
     * Database name
     */
    @Parameter
    private String name;

    /**
     * Database schemas
     */
    @Parameter(required = true)
    private List<String> schemas;

    /**
     * PostgreSQL server parameters, e.g. shared_buffers, dynamic_shared_memory_type.
     */
    @Parameter
    private Map<String, String> serverConfig;

    /**
     * PostgreSQL locale parameters, e.g. lc_messages.
     */
    @Parameter
    private Map<String, String> localeConfig;

    /**
     * PostgreSQL client connection defaults, e.g. user, loggerLevel.
     */
    @Parameter
    private Map<String, String> connectConfig;

    /**
     * Working directory for initdb and postgres process.
     */
    @Parameter
    private String workingDirectory;

    /**
     * Whether to clean the data directory before start.
     */
    @Parameter(defaultValue = "true")
    private boolean cleanDataDirectory;

    /**
     * Startup wait timeout in milliseconds.
     */
    @Parameter(defaultValue = "30000")
    private long startupWaitMillis;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        PostgresRuntimeState runtimeState = getPostgresRuntimeState();
        if (runtimeState != null) {
            getLog().warn("The PG server is already running!");
            return;
        }

        String dataDirectory = resolveDataDirectory();
        String databaseName = resolveDatabaseName();

        try {
            EmbeddedPostgres embeddedPostgres = startPgServer(dataDirectory);
            createDatabase(embeddedPostgres, databaseName);
            createSchemas(embeddedPostgres, databaseName);
            Thread shutdownHook = registerShutdownHook();
            setPostgresRuntimeState(new PostgresRuntimeState(embeddedPostgres, shutdownHook));
        } catch (IOException ex) {
            throw new MojoExecutionException("Errors occurred while starting the PG server", ex);
        } catch (SQLException ex) {
            tryStopServerQuietly();
            throw new MojoExecutionException("Errors occurred while creating PG objects", ex);
        }
    }

    private EmbeddedPostgres startPgServer(String dataDirectory) throws IOException {
        getLog().info("The PG server is starting...");
        EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
        getLog().info("Dir for PG files: " + dataDirectory);

        builder.setDataDirectory(dataDirectory);
        builder.setPort(port);
        builder.setCleanDataDirectory(cleanDataDirectory);
        builder.setPGStartupWait(Duration.ofMillis(startupWaitMillis));

        if (StringUtils.isNotBlank(workingDirectory)) {
            builder.setOverrideWorkingDirectory(new File(workingDirectory));
        }

        applyConfig(builder, serverConfig, ConfigType.SERVER);
        applyConfig(builder, localeConfig, ConfigType.LOCALE);
        applyConfig(builder, connectConfig, ConfigType.CONNECT);

        EmbeddedPostgres embeddedPostgres = builder.start();
        getLog().info("The PG server was started!");
        return embeddedPostgres;
    }

    private void createDatabase(EmbeddedPostgres embeddedPostgres, String databaseName) throws SQLException {
        try (Connection conn = embeddedPostgres.getPostgresDatabase().getConnection();
                Statement statement = conn.createStatement()) {
            statement.execute("CREATE DATABASE " + databaseName);
        } catch (SQLException ex) {
            getLog().error("An error occurred while creating the database " + databaseName);
            throw ex;
        }
    }

    private void createSchemas(EmbeddedPostgres embeddedPostgres, String databaseName) throws SQLException {
        DataSource database = embeddedPostgres.getDatabase("postgres", databaseName);
        try (Connection connection = database.getConnection();
                Statement statement = connection.createStatement()) {
            for (String schema : schemas) {
                statement.execute("CREATE SCHEMA " + schema);
            }
        } catch (SQLException ex) {
            getLog().error("An error occurred while creating the schemas " + schemas);
            throw ex;
        }
    }

    private Thread registerShutdownHook() {
        Thread shutdownHook = new Thread(new Runnable() {
            @Override
            public void run() {
                tryStopServerQuietly();
            }
        }, "PG-embedded-shutdown-hook");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
        return shutdownHook;
    }

    private void applyConfig(
            EmbeddedPostgres.Builder builder,
            Map<String, String> config,
            ConfigType configType
    ) {
        if (config == null || config.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> entry : config.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            switch (configType) {
                case SERVER:
                    builder.setServerConfig(entry.getKey(), entry.getValue());
                    break;
                case LOCALE:
                    builder.setLocaleConfig(entry.getKey(), entry.getValue());
                    break;
                case CONNECT:
                    builder.setConnectConfig(entry.getKey(), entry.getValue());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported config type " + configType);
            }
        }
    }

    private String resolveDatabaseName() throws MojoExecutionException {
        if (StringUtils.isNotBlank(name)) {
            if (StringUtils.isNotBlank(dbName)) {
                getLog().warn("Parameter 'dbName' is deprecated and ignored because 'name' is provided");
            }
            return name;
        }
        if (StringUtils.isNotBlank(dbName)) {
            getLog().warn("Parameter 'dbName' is deprecated; use 'name' instead");
            return dbName;
        }
        throw new MojoExecutionException("Either 'name' or deprecated 'dbName' must be provided");
    }

    private String resolveDataDirectory() {
        String resolvedDir = StringUtils.defaultIfBlank(dir, dbDir);
        if (StringUtils.isNotBlank(dir) && StringUtils.isNotBlank(dbDir)) {
            getLog().warn("Parameter 'dbDir' is deprecated and ignored because 'dir' is provided");
        } else if (StringUtils.isBlank(dir) && StringUtils.isNotBlank(dbDir)) {
            getLog().warn("Parameter 'dbDir' is deprecated; use 'dir' instead");
        }

        if (StringUtils.isNotBlank(resolvedDir)) {
            return resolvedDir;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        return projectBuildDir + File.separator + "pgdata_" + currentDate;
    }

    private void tryStopServerQuietly() {
        PostgresRuntimeState runtimeState = getPostgresRuntimeState();
        if (runtimeState == null) {
            return;
        }

        try {
            runtimeState.getEmbeddedPostgres().close();
        } catch (IOException ex) {
            getLog().warn("Failed to stop the PostgreSQL server during cleanup", ex);
        } finally {
            removeShutdownHook(runtimeState.getShutdownHook());
            clearPostgresRuntimeState();
        }
    }

    static void stopPgServer(GeneralMojo mojo) throws IOException {
        PostgresRuntimeState runtimeState = mojo.getPostgresRuntimeState();
        if (runtimeState == null) {
            return;
        }

        try {
            runtimeState.getEmbeddedPostgres().close();
        } finally {
            mojo.removeShutdownHook(runtimeState.getShutdownHook());
            mojo.clearPostgresRuntimeState();
        }
    }

    static boolean isRunning(GeneralMojo mojo) {
        return mojo.getPostgresRuntimeState() != null;
    }

    private enum ConfigType {
        SERVER,
        LOCALE,
        CONNECT
    }
}
