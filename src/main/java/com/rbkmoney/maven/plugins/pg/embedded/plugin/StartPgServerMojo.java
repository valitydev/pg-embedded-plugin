package com.rbkmoney.maven.plugins.pg.embedded.plugin;

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
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Class to start the embedded server
 *
 * @author d.baykov
 */
@Mojo(name = "start", defaultPhase = LifecyclePhase.INITIALIZE)
public class StartPgServerMojo extends GeneralMojo {

    /**
     * Directory where the project is located
     */
    @Parameter(defaultValue = "${project.build.directory}")
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
    @Parameter(required = true)
    @Deprecated
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
     * Instance of the PostgreSQL
     */
    private static EmbeddedPostgres embeddedPostgres;

    /**
     * Thread where the PostgreSQL server is running
     */
    private static Thread postgresThread;

    /**
     * Indicates that the server is up and running
     */
    private static boolean running = false;

    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        dir = Optional.ofNullable(dir).orElse(dbDir);
        name = Optional.ofNullable(name).orElse(dbName);
        if (embeddedPostgres != null) {
            getLog().warn("The PG server is already running!");
        } else {
            postgresThread = new Thread(() -> {
                try {
                    startPgServer();
                    createDatabase();
                    createSchemas();
                    setServerRun();
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
    }

    /**
     * Method starts PG server
     */
    private void startPgServer() throws IOException {
        getLog().info("The PG server is starting...");
        EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
        String dbDir = prepareDbDir();
        getLog().info("Dir for PG files: " + dbDir);
        builder.setDataDirectory(dbDir);
        builder.setPort(port);
        //TODO: additional parameters should be added
        embeddedPostgres = builder.start();
        getLog().info("The PG server was started!");
    }

    /**
     * The method creates a new database
     */
    private void createDatabase() throws SQLException {
        try (Connection conn = embeddedPostgres.getPostgresDatabase().getConnection()) {
            Statement statement = conn.createStatement();
            statement.execute("CREATE DATABASE " + name);
            statement.close();
        } catch (SQLException ex) {
            getLog().error("An error occurred while creating the database " + name);
            throw ex;
        }
    }

    /**
     * The method creates a new schema in the created database
     */
    private void createSchemas() throws SQLException {
        DataSource database = embeddedPostgres.getDatabase("postgres", name);
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

    /**
     * The method sets the directory for placing postgre service files
     */
    private String prepareDbDir() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        if (StringUtils.isEmpty(dir)) {
            return projectBuildDir + File.separator + "pgdata_" + currentDate;
        } else {
            return dir;
        }
    }

    /**
     * This method stops the server
     */
    public static void stopPgServer() throws IOException {
        //TODO: Perhaps, it isn't very pefrect realisation and it will be redesign
        if (isRunning() && embeddedPostgres != null) {
            embeddedPostgres.close();
            if (postgresThread != null) {
                postgresThread.interrupt();
            }
            embeddedPostgres = null;
            setServerStop();
        }
    }

    private static void setServerRun() {
        running = true;
    }

    private static void setServerStop() {
        running = false;
    }

    public static boolean isRunning() {
        return running;
    }

}
