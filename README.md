# PlayGround - How to deploy jar library to repository
Test repository for releasing package to Sonatype

pom configuration is based on https://central.sonatype.org/publish/publish-maven/ and https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-maven  

Deploy with maven command
`mvn --batch-mode release:prepare release:perform`