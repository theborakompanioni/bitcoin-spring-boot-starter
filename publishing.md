
# Publishing

Signing artifacts
```sh
./gradlew clean sign -Psigning.password=secret
```

In your local `~/.gradle/gradle.properties` you need
```properties
signing.keyId=ABCDEF01
#signing.password=secret - must be provided via command line
signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg
```


### Local Maven Repository
e.g. to publish to your local maven repository:
```sh
./gradlew publishNebulaPublicationToMavenLocal -Prelease.stage=SNAPSHOT -Prelease.scope=patch
```
```sh
./gradlew publishNebulaPublicationToMavenLocal -Prelease.useLastTag=true
```


### Maven Central
```sh
./gradlew -Dorg.gradle.parallel=false -Prelease.useLastTag=true \
    clean assemble -PjavadocEnabled \
    sign -Psigning.password=secret \
    publishAllPublicationsToMavenRepository -PossrhPassword=secret
```

e.g. publishing an individual module to Maven Central (notice params `signing.password` and `ossrhPassword`):
```sh
./gradlew -p bitcoin-jsonrpc-client/bitcoin-jsonrpc-client-core \
    -Dorg.gradle.parallel=false -Prelease.useLastTag=true \
    clean assemble -PjavadocEnabled \
    sign -Psigning.password=secret \
    publishAllPublicationsToMavenRepository -PossrhPassword=secret
```

In your local `~/.gradle/gradle.properties` you need
```
ossrhUsername=your-jira-user
#ossrhPassword=your-jira-password - must be provided via command line
```

### Jitpack
Publishing on JitPack is done by creating a GitHub Release.


## Resources
- Signing Plugin: https://docs.gradle.org/current/userguide/signing_plugin.html#signing_plugin
- Nebula Publishing Plugin: https://github.com/nebula-plugins/nebula-publishing-plugin
- Maven Central Publish Guide: https://central.sonatype.org/publish/publish-guide/
- Maven Central Release Guide: https://central.sonatype.org/publish/release/
- Central OSSRH Nexus Repository: https://s01.oss.sonatype.org/
- Jitpack: https://jitpack.io/docs/#publishing-on-jitpack
- https://discuss.gradle.org/t/how-to-publish-artifacts-signatures-asc-files-using-maven-publish-plugin/7422/24
