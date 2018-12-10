package com.rbkmoney.maven.plugins.pg_embedded_plugin;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
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
import java.util.List;

/**
 * Class to start the embedded server
 *
 * @author d.baykov
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class StartPgServerMojo extends GeneralMojo {

    /** Directory where the project is located */
    @Parameter(defaultValue = "${project.build.directory}")
    private String projectBuildDir;

    /** PostgreSQL version */
    @Parameter(property = "version")
    private String version;

    /** Directory that will host the service files of postgresql */
    @Parameter(property = "dbDir")
    private String dbDir;

    /** Port on which the instance will be raised */
    @Parameter(property = "port", defaultValue = "15432", required = true)
    private int port;

    /** Database user name */
    @Parameter(property = "username", defaultValue = "postgres", required = true)
    private String userName;

    /** Database password */
    @Parameter(property = "password", defaultValue = "postgres")
    private String password;

    /** Database name */
    @Parameter(property = "dbName", required = true)
    private String dbName;

    /** Database schemas */
    @Parameter(property = "schemas", required = true)
    private List<String> schemas;

    /** Instance of the PostgreSQL */
    private static EmbeddedPostgres embeddedPostgres;

    /** Thread where the PostgreSQL server is running */
    private static Thread postgresThread;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        postgresThread = new Thread(() -> {
            try {
                startPgServer();
                createDatabase();
                createSchemas();
            } catch (IOException e) {
                getLog().error("Errors occurred while starting the PG server:", e);
            } catch (SQLException e) {
                getLog().error("Errors occurred while creating objects:", e);
            }

        }, "PG-embedded-server");
        postgresThread.start();
        try {
            postgresThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Embedded Postgres thread was interrupted", e);
        }
    }

    /** Method starts PG server */
    private void startPgServer() throws IOException {
        if (embeddedPostgres == null) {
            getLog().info("The PG server is starting...");
            EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
            String dbDir = prepareDbDir();
            getLog().info("Dir for PG files: " + dbDir);
            builder.setDataDirectory(dbDir);
            builder.setPort(port);
            //TODO: this can be implemented in the future...
            //builder.setCleanDataDirectory(true);
            //builder.setLocaleConfig();
            //builder.setServerConfig();
            //builder.setConnectConfig();
            embeddedPostgres = builder.start();
            getLog().info("The PG server was started!");
        } else {
            getLog().warn("The PG server is already running!");
        }
    }

    /** The method creates a new database */
    private void createDatabase() throws SQLException {
        try (Connection conn = embeddedPostgres.getPostgresDatabase().getConnection()) {
            Statement statement = conn.createStatement();
            statement.execute("CREATE DATABASE " + dbName);
            statement.close();
        } catch (SQLException ex) {
            getLog().error("An error occurred while creating the database "+ dbName);
            throw ex;
        }
    }

    /** The method creates a new schema in the created database */
    private void createSchemas() throws SQLException {
        DataSource database = embeddedPostgres.getDatabase(userName, dbName);

        try (Connection connection = database.getConnection()) {
            Statement statement = connection.createStatement();
            for (String schema : schemas) {
                statement.execute("CREATE SCHEMA " + schema);
            }
            statement.close();
        } catch (SQLException ex) {
            getLog().error("An error occurred while creating the schemas " + schemas);
            throw ex;
        }
    }

    /** The method sets the directory for placing postgre service files */
    private String prepareDbDir() {
        if (StringUtils.isEmpty(dbDir)) {
            return projectBuildDir + File.separator + "pgdata";
        } else {
            return dbDir;
        }
    }

    public static EmbeddedPostgres getEmbeddedPostgres() {
        return embeddedPostgres;
    }

    public static Thread getPostgresThread() {
        return postgresThread;
    }
}
