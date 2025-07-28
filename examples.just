# This justfile requires https://github.com/casey/just

# run electrum gateway example application
[group("examples")]
bitcoin-electrum-gateway:
    @./gradlew -p examples/incubator/bitcoin-electrum-gateway-example-application bootRun --args='--spring.profiles.active=development'

# run payment request example application
[group("examples")]
bitcoin-payment-request:
    @./gradlew -p examples/incubator/bitcoin-payment-request-example-application bootRun --args="--spring.profiles.active=development"

# run spring lnurl auth example application
[group("examples")]
spring-lnurl-auth:
    @./gradlew -p incubator/spring-lnurl/spring-lnurl-auth-example-application bootRun --args="--spring.profiles.active=development"
