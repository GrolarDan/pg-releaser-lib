# Publish java artifact to Maven Central Repository

Publish java packages to Maven Central Repository through Sonatype OSSRH (OSS Repository Hosting) with maven tool and GitHub actions.    
It is based on guides from Sonatype, Maven and GitHub pages.

- [Sonatype](https://central.sonatype.org/publish/publish-guide)
- Maven
  - [GPG](https://maven.apache.org/plugins/maven-gpg-plugin)
  - [Release plugin](https://maven.apache.org/maven-release/maven-release-plugin)
  - [Source plugin](https://maven.apache.org/plugins/maven-source-plugin)
  - [Javadoc plugin](https://maven.apache.org/plugins/maven-javadoc-plugin)
- Github
  - [Publish with Maven](https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-maven) 
  - [Sign and release](https://gist.github.com/sualeh/ae78dc16123899d7942bc38baba5203c)

Other sources

- [GitHub Actions and Maven releases](https://blog.frankel.ch/github-actions-maven-releases)

## Prerequisites  

Before publishing artifact to the Central Repository, you need to initiate Sonatype.  
Sonatype Initial Setup is in detail explained [here](https://central.sonatype.org/publish/publish-guide/). 

### Sonatype Initial Setup

  1. [Create account](https://issues.sonatype.org/secure/Signup!default.jspa) in OSSRH Jira
  2. [Create ticket](https://issues.sonatype.org/secure/CreateIssue.jspa?pid=10134&issuetype=21) to register group id  
    If you use your own domain you need to add DNS TXT record to your domain
      - name: `@`
      - type: `TXT`
      - text: `YOUR.TICKET.NUMBER`

### GPG
One of Sonatype requirements is signing files. For that we use GPG.

  1. Install GnuPG [from](https://www.gnupg.org/download)
  2. Generate a Key Pair and distribute public key

```shell
# Generate a Key Pair
gpg --gen-key
# List keys
gpg --list-keys
# Distribute public key to one of below 
# - keyserver.ubuntu.com
# - keys.openpgp.org
# - pgp.mit.edu
# YOUR.GENERATED.KEY is taken from pub part of list keys
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR.GENERATED.KEY
```

## Deploy to OSSRH with Apache Maven

### OSSRH Requirements

Sonatype has some [Requirements](https://central.sonatype.org/publish/requirements) which can be set up in the maven pom file. 

```xml
<project>
    <!-- Project name, description and URL -->
    <name>PlayGround - Releaser library</name>
    <description>A demo for deployment to the Maven Central Repository via OSSRH</description>
    <url>https://github.com/GrolarDan/pg-releaser-lib</url>

    <!-- License Information -->
    <licenses>
        <license>
            <name>GNU GPL v3.0</name>
            <url>https://www.gnu.org/licenses/gpl-3.0.txt</url>
        </license>
    </licenses>

    <!-- Developer Information-->
    <developers>
        <developer>
            <name>Daniel Mašek</name>
            <email>daniel@masci.cz</email>
            <organization>MASCI-CZ</organization>
            <organizationUrl>https://github.com/masci-cz</organizationUrl>
        </developer>
    </developers>

    <!-- SCM (Source Code Management) information -->
    <scm>
        <connection>scm:git:git://github.com/GrolarDan/pg-releaser-lib.git</connection>
        <developerConnection>scm:git:https://github.com/GrolarDan/pg-releaser-lib.git</developerConnection>
        <url>https://github.com/GrolarDan/pg-releaser-lib.git/tree/master</url>
        <tag>HEAD</tag>
    </scm>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven.compiler.plugin.version}</version>
            </plugin>
            <!-- Deploy components to OSSRH -->
            <plugin>
                <groupId>org.sonatype.plugins</groupId>
                <artifactId>nexus-staging-maven-plugin</artifactId>
                <version>${nexus.staging.plugin.version}</version>
                <extensions>true</extensions>
                <configuration>
                    <serverId>ossrh</serverId>
                    <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
                    <!-- Automatic release to the Central Repository without manual inspect -->
                    <autoReleaseAfterClose>true</autoReleaseAfterClose>
                </configuration>
            </plugin>
            <!-- Prepare new release to be deployed (runs nexus-staging maven plugin) -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-release-plugin</artifactId>
                <version>${maven.release.plugin.version}</version>
                <configuration>
                    <autoVersionSubmodules>true</autoVersionSubmodules>
                    <useReleaseProfile>false</useReleaseProfile>
                    <!-- Used build profile -->
                    <releaseProfiles>release</releaseProfiles>
                    <!-- Tells maven to run deploy with nexus staging plugin -->
                    <goals>deploy nexus-staging:release</goals>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>release</id>
            <build>
                <plugins>
                    <!-- Attach Java Sources -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-source-plugin</artifactId>
                        <version>${maven.source.plugin.version}</version>
                        <executions>
                            <execution>
                                <id>attach-sources</id>
                                <goals>
                                    <goal>jar-no-fork</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                    <!-- Attach Java Documentation -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-javadoc-plugin</artifactId>
                        <version>${maven.javadoc.plugin.version}</version>
                        <executions>
                            <execution>
                                <id>attach-javadocs</id>
                                <goals>
                                    <goal>jar</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                    <!-- Sign artifacts -->
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>${maven.gpg.plugin.version}</version>
                        <executions>
                            <execution>
                                <id>sign-artifacts</id>
                                <goals>
                                    <goal>sign</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>
                </plugins>
            </build>
        </profile>
    </profiles>
</project>

```

### Distribution Management and Authentication

To deploy to OSSRH we need provide maven with credentials.  
Also GPG plugin needs passphrase to the private key.  
Because we don't want to publish credentials I found useful using environment variables and these then use in maven build.

#### Environment variables

- `env.OSSRH_USERNAME` - username to the Sonatype JIRA account
- `env.OSSRH_TOKEN` - token to the Sonatype JIRA account
- `env.GPG_PASSPHRASE` - passphrase used to generate GPG private key (explained [here](#gpg))

#### settings.xml
```xml
  <settings>
    <servers>
      <server>
        <id>ossrh</id>
        <username>${env.OSSRH_USERNAME}</username>
        <password>${env.OSSRH_TOKEN}</password>
      </server>
      <server>
        <id>gpg.passphrase</id>
        <passphrase>${env.GPG_PASSPHRASE}</passphrase>
      </server>
    </servers>
  </settings>
```

### Perform release locally

Finally, we can run the release locally with this command

`mvn --batch-mode release:prepare release:perform`

## GitHub Actions
