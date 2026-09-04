# This justfile requires https://github.com/casey/just

[group("project")]
project-version:
    @./gradlew properties --console=plain --quiet \
      | grep "^version:" \
      | awk '{ printf "%s\n",$2 }'

[group("project")]
invalidate-configuration-cache:
    # from: https://docs.gradle.org/current/userguide/configuration_cache_enabling.html#config_cache:usage:invalidate (last checked 2026-09-03)
    rm --recursive --force .gradle/configuration-cache
