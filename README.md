##  PG Embedded plugin

This is a maven plugin for for starting a __embedded postgresql server__.
By default the server starts at the initialization stage and stops at the compile stage.
Possible values in the configuration block:
 * __dbDir__ (optional) - file directory of the instance (if it will be empty then files will create in a terger directory);
 * __port__ (required; default 15432) - port on which the instance will be started;
 * __dbName__ (required) - name of database witch will be created in the instance;
 * __schemas__ (required) - list of scheme witch will be created in the instance.


You can use this __example__ to start the server during maven initialization lifecycle.

#####  Example:

    <plugin>
        <groupId>com.rbkmoney.maven.plugins</groupId>
        <artifactId>pg-embedded-plugin</artifactId>
        <version>1.3</version>
        <configuration>
            <port>15432</port>
            <dbName>database_name</dbName>
            <schemas>
                <schema>schema_name</schema>
            </schemas>
        </configuration>
        <executions>
            <execution>
                <id>PG_server_start</id>
                <phase>validate</phase>
                <goals>
                    <goal>start</goal>
                </goals>
            </execution>
            <execution>
                <id>PG_server_stop</id>
                <phase>compile</phase>
                <goals>
                    <goal>stop</goal>
                </goals>
            </execution>
        </executions>
    </plugin>


__Attention:__ extremely important to pay attention to the stages of launching plug-ins dependent on the launch of this plugin. 
For example, your flyway should runnin' on phase initialize and your JOOQ should runnin' on phase generate-sources
