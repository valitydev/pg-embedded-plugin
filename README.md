## PG Embedded plugin

This Maven plugin starts an embedded PostgreSQL server backed by
`io.zonky.test:embedded-postgres`.

By default:
- `start` is bound to `generate-sources`
- `stop` is bound to `process-sources`

That keeps the database close to code generation steps like Flyway and jOOQ
instead of starting it as early as `initialize`.

### Configuration

Supported configuration parameters:
- `dir` - data directory for PostgreSQL files
- `dbDir` - deprecated alias for `dir`
- `port` - required PostgreSQL port
- `name` - required database name
- `dbName` - deprecated alias for `name`
- `schemas` - required list of schemas to create
- `cleanDataDirectory` - whether to wipe the data directory before start
- `startupWaitMillis` - startup timeout for PostgreSQL
- `workingDirectory` - process working directory
- `serverConfig` - PostgreSQL server parameters such as `shared_buffers`,
  `dynamic_shared_memory_type`, `unix_socket_directories`, `max_connections`
- `localeConfig` - locale-related PostgreSQL parameters
- `connectConfig` - default connection properties

Example:

```xml
<plugin>
    <groupId>dev.vality.maven.plugins</groupId>
    <artifactId>pg-embedded-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <port>${db.port}</port>
        <name>${db.name}</name>
        <schemas>
            <schema>${db.schema}</schema>
        </schemas>
        <serverConfig>
            <shared_buffers>16MB</shared_buffers>
            <dynamic_shared_memory_type>posix</dynamic_shared_memory_type>
            <max_connections>20</max_connections>
        </serverConfig>
    </configuration>
    <executions>
        <execution>
            <id>PG_server_start</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>start</goal>
            </goals>
        </execution>
        <execution>
            <id>PG_server_stop</id>
            <phase>process-sources</phase>
            <goals>
                <goal>stop</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Portability

The plugin chooses the embedded PostgreSQL runtime binary through Maven
profiles for:
- macOS amd64
- Linux amd64
- Windows amd64

By default this selection is automatic and is based on the current OS.

If you need to override it explicitly, for example in CI, set
`postgres.binary.profile`:

```bash
mvn -Dpostgres.binary.profile=linux-amd64 ...
```

Supported manual values:
- `darwin-amd64`
- `linux-amd64`
- `linux-amd64-alpine`
- `windows-amd64`

If you need another runtime package, add another profile dependency in the
plugin `pom.xml`.

### Notes

- `name` is the current parameter. `dbName` still works but is deprecated.
- `dir` is the current parameter. `dbDir` still works but is deprecated.
- `cleanDataDirectory` defaults to `true`.
- `startupWaitMillis` defaults to `30000`.
- The plugin keeps runtime state in the Maven plugin context and also registers
  a JVM shutdown hook, so cleanup does not depend only on reaching the `stop`
  phase.
