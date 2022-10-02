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

  1. Install GnuPG from [here](https://www.gnupg.org/download)
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

### Release and deploy

Setup release plugin which increases the version and push it to the github (Can't push to protected branch - _working on this_).  
Release plugin is set up to use the `release` profile defined in profiles section to run java source, java doc and sign plugins during deployment.  
At the end runs the deployment through nexus staging plugin because the nexus plugin overrides the default maven deploy plugin.  

```xml
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
                <!-- Automatic release to the Central Repository without manual inspect must be false -->
                <!-- because there is release call at the end of the maven release plugin -->
                <autoReleaseAfterClose>false</autoReleaseAfterClose>
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
                <!-- Tells maven to run deploy at the end and release deployed nexus staging repository with the nexus staging plugin -->
                <goals>deploy nexus-staging:release</goals>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Distribution Management and Authentication

To deploy to OSSRH we need provide maven with credentials.  
Also GPG plugin needs passphrase to the private key.  
Because we don't want to publish credentials I found useful using environment variables and use them in maven build process. 

#### Environment variables

- `env.OSSRH_USERNAME` - username to the Sonatype JIRA account
- `env.OSSRH_TOKEN` - token to the Sonatype JIRA account
- `env.GPG_PASSPHRASE` - passphrase used to generate GPG private key (explained [here](#gpg))

#### settings.xml
in `.m2` directory
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

Verify  

On each push runs `mvn verify`

Publish

We need to provide Github with credentials same as we did in local deployment.

1. Export your gpg private key from the system that you have created it.
   1. Find your key-id (using `gpg --list-secret-keys --keyid-format=long`)
   2. Export the gpg secret key to an ASCII file using `gpg --export-secret-keys -a <key-id> > secret.txt`
2. Set up GitHub Actions secrets
   1. Create a secret called `OSSRH_USERNAME` containing the account id to the Sonatype Jira
   2. Create a secret called `OSSRH_TOKEN` containing the password to the Sonatype Jira
   3. Create a secret called `GPG_SECRET_KEY` using the text from your `secret.txt` file (with all the `LF`)
   4. Create a secret called `GPG_SECRET_KEY_PASSWORD` containing the password for your gpg secret key
3. Create a GitHub Actions step to set up java with GPG secret key (explained [here](https://github.com/actions/setup-java/blob/main/docs/advanced-usage.md#Publishing-using-Apache-Maven))
   1. Add an action similar to:
    ```yaml
      - name: Set up Maven Central Repository
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'maven'
          server-id: ossrh  # Value of the distributionManagement/repository/id field of the pom.xml
          server-username: MAVEN_USERNAME # env variable for username in deploy
          server-password: MAVEN_CENTRAL_TOKEN # env variable for token in deploy
          gpg-private-key: ${{ secrets.GPG_SECRET_KEY }} # Value of the GPG private key to import - without any modification
          gpg-passphrase: MAVEN_GPG_PASSPHRASE  # env variable for GPG private key passphrase
    ```
   2. `settings.xml` file is created with this content
   ```xml
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
        <servers>
            <server>
                <id>ossrh</id>
                <username>${env.MAVEN_USERNAME}</username>
                <password>${env.MAVEN_CENTRAL_TOKEN}</password>
            </server>
            <server>
                <id>gpg.passphrase</id>
                <passphrase>${env.MAVEN_GPG_PASSPHRASE}</passphrase>
            </server>
        </servers>
    </settings>
   ```
   3. The private key will be written to a file in the runner's temp directory, the private key file will be imported into the GPG keychain, and then the file will be promptly removed before proceeding with the rest of the setup process.
4. Prepare GitHub App to bypass master branch protection
   1. In account/organization settings go to Developer Settings and GitHub Apps
   2. Create new GitHub App
      1. Remember `App ID`
      2. Uncheck Webhook
      3. Generate Private key and download the `.pem` file
      4. Set permissions
         1. Content - Read and write
         2. Metadata - Read only
   3. Install GitHub App to account/organization
5. Add new secrets to the repository
   1. Create secret called `APP_ID` containing the GitHub App `App ID`
   2. Create secret called `APP_PEM` containing GitHub App private key in base64 format in one line
      1. `cat your_app_key.pem | base64 -w 0 && echo`
6. Get installation token
    ```yaml
    - name: Get token
      id: get_token
      uses: machine-learning-apps/actions-app-token@master
      with:
        APP_PEM: ${{ secrets.APP_PEM }}
        APP_ID: ${{ secrets.APP_ID }}

    - name: Get App Installation Token
      run: |
        echo "This token is masked: ${TOKEN}"
      env:
        TOKEN: ${{ steps.get_token.outputs.app_token }}
    ```
7. Use token in checkout step
    ```yaml
    - name: Checkout
      uses: actions/checkout@v3
      with:
        token: ${{ steps.get_token.outputs.app_token }}    
    ```
8. Bring it all together, and create a GitHub Actions step to publish
   1. Add an action similar to:
    ```yaml
    - id: publish-to-central
      name: Publish to Central Repository
      env:
        MAVEN_USERNAME: ${{ secrets.OSSRH_USERNAME }}
        MAVEN_PASSWORD: ${{ secrets.OSSRH_TOKEN }}
        MAVEN_GPG_PASSPHRASE: ${{ secrets.GPG_SECRET_KEY_PASSWORD }}
      run: |
        git config user.name github-actions
        git config user.email github-actions@github.com
        mvn \
          --no-transfer-progress \
          --batch-mode \
          release:prepare release:perform
    ```
   2. Configure the git `user.name` and `user.email` because release submits changes to the git
   ```yaml
   git config user.name github-actions
   git config user.email github-actions@github.com 
    ```
