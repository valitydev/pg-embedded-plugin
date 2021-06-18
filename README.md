##  PG Embedded plugin

This is a maven plugin for for starting a __embedded postgresql server__.
By default the server starts at the initialization stage and stops at the compile stage.
Possible values in the configuration block:
 * __dbDir__ (optional, deprecated, __dir__ actual) - file directory of the instance (if it will be empty then files will create in a terger directory); 
 * __port__ (required; default 15432) - port on which the instance will be started;
 * __dbName__ (required, deprecated, __name__ actual) - name of database witch will be created in the instance;
 * __schemas__ (required) - list of scheme witch will be created in the instance.


You can use this __example__ to start the server during maven initialization lifecycle.

#####  Example:
            <plugin>
                <groupId>com.rbkmoney.maven.plugins</groupId>
                <artifactId>pg-embedded-plugin</artifactId>
                <version>1.7</version>
                <configuration>
                    <port>${db.port}</port>
                    <name>${db.name}</name>
                    <schemas>
                        <schema>${db.schema}</schema>
                    </schemas>
                </configuration>
                <executions>
                    <execution>
                        <id>PG_server_start</id>
                        <phase>initialize</phase>
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
