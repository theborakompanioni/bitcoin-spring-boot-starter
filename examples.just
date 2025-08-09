# This justfile requires https://github.com/casey/just


# run payment request example application
[group("examples")]
bitcoin-payment-request:
    @./gradlew -p examples/incubator/bitcoin-payment-request-example-application bootRun --args="--spring.profiles.active=development"

# run spring lnurl auth example application
[group("examples")]
spring-lnurl-auth:
    @./gradlew -p incubator/spring-lnurl/spring-lnurl-auth-example-application bootRun --args="--spring.profiles.active=development"

# run electrum gateway example application
[group("examples")]
electrum-gateway:
    @./gradlew -p examples/incubator/bitcoin-electrum-gateway-example-application bootRun --args='--spring.profiles.active=development'

# run electrum shell example application
[group("examples")]
electrum-shell:
    @SPRING_PROFILES_ACTIVE=mainnet ./gradlew --console=plain -p examples/incubator/bitcoin-electrum-shell-example-application bootRun
