##  pg embedded plugin

This is a maven plugin for for starting a embedded postgresql server.

You can use this example to start the server during maven initialization lifecicle.


#####  Example:

    <plugin>
        <groupId>com.rbkmoney.maven.plugins</groupId>
        <artifactId>pg-embedded-plugin</artifactId>
        <version>1.0</version>
        <configuration>
            <port>port</port> <!-- default: 15432 -->
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

#####  Example for flyway and jooq:

    <plugin>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-maven-plugin</artifactId>
        <version>${flyway.version}</version>
        <configuration>
            <url>${local.db.url}</url>
            <user>${local.db.user}</user>
            <password>${local.db.password}</password>
            <schemas>
                <schema>${local.db.scheme}</schema>
            </schemas>
        </configuration>
        <executions>
            <execution>
                <id>migrate</id>
                <phase>initialize</phase>
                <goals>
                    <goal>clean</goal>
                    <goal>migrate</goal>
                </goals>
            </execution>
        </executions>
        <dependencies>
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <version>${postgresql.jdbc.version}</version>
            </dependency>
        </dependencies>
    </plugin>
    <plugin>
        <groupId>org.jooq</groupId>
        <artifactId>jooq-codegen-maven</artifactId>
        <version>${jooq.version}</version>
        <configuration>
            <jdbc>
                <driver>org.postgresql.Driver</driver>
                <url>${local.db.url}</url>
                <user>${local.db.user}</user>
                <password>${local.db.password}</password>
            </jdbc>
            <generator>
                <generate>
                    <javaTimeTypes>true</javaTimeTypes>
                    <pojos>true</pojos>
                    <pojosEqualsAndHashCode>true</pojosEqualsAndHashCode>
                    <pojosToString>true</pojosToString>
                </generate>
                <database>
                    <name>org.jooq.util.postgres.PostgresDatabase</name>
                    <includes>.*</includes>
                    <excludes>schema_version|.*func|get_adjustment.*|get_cashflow.*|get_payment.*|get_payout.*|get_refund.*</excludes>
                    <inputSchema>${local.db.scheme}</inputSchema>
                </database>
                <target>
                    <directory>target/generated-sources/db/</directory>
                </target>
            </generator>
        </configuration>
        <executions>
            <execution>
                <id>gen-src</id>
                <phase>generate-sources</phase>
                <goals>
                    <goal>generate</goal>
                </goals>
            </execution>
        </executions>
    </plugin>


