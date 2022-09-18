# PlayGround - How to deploy jar library to repository
Test repository for releasing package to Sonatype

pom configuration is based on https://central.sonatype.org/publish/publish-maven/ and https://docs.github.com/en/actions/publishing-packages/publishing-java-packages-with-maven  

## Prerequisite
### Group ID

First group id needs to be prepared.  
There is possibility to use github account for it.  
But when you own a domain you could use that as group id.  

### Sonatype

To get access to Sonatype OSSRH (Open Source Software Repository Hosting) there are two steps to take.

* Create JIRA account
* Create a New Project tickets

Both steps are more detailed explained on https://central.sonatype.org/publish/publish-guide/  
I had some difficulties to finish the requirements registering group id which is part of the ticket creation.  

New Project information  
**Name:**       OSSRH group id registration  
**Group Id:**   [your group id]

After ticket creation it awaits users manual step.  
Add new DNS TXT record in your domain DNS manager.  
The name `@` is the most important.

| name | type | text            |
|------|------|-----------------|
| @    | TXT  | [ticket number] |

When the DNS record is inserted the ticket could be changed from `Waiting for Response` to `Open`.


Deploy with maven command
`mvn --batch-mode release:prepare release:perform -P release`