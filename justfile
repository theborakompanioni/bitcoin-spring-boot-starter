# This justfile requires https://github.com/casey/just

# Load environment variables from `.env` file.
set dotenv-load
# Fail the script if the env file is not found.
set dotenv-required

project_dir := justfile_directory()

# print available targets
[group("project-agnostic")]
default:
    @just --list --justfile {{justfile()}}

# evaluate and print all just variables
[group("project-agnostic")]
evaluate:
    @just --evaluate

# print system information such as OS and architecture
[group("project-agnostic")]
system-info:
  @echo "architecture: {{arch()}}"
  @echo "os: {{os()}}"
  @echo "os family: {{os_family()}}"

# clean (remove) the build artifacts
[group("development")]
clean:
    @./gradlew clean

# compile the project
[group("development")]
build:
    @./gradlew build -x test

# list dependency tree of this project
[group("development")]
dependencies:
    @./gradlew dependencyTree

# run unit tests
[group("development")]
test:
    @./gradlew test

# run integration tests
[group("development")]
test-integration:
    @./gradlew integrationTest --rerun-tasks --no-parallel

# run end-to-end tests
[group("development")]
test-e2e:
    @./gradlew e2eTest --rerun-tasks --no-parallel

# run testcontainer tests
[group("development")]
test-testcontainer:
    @./gradlew testcontainerTest --rerun-tasks --no-parallel

# run example tests
[group("development")]
test-example:
    @./gradlew exampleTest --rerun-tasks --no-parallel

# run all tests
[group("development")]
test-all:
    @./gradlew test integrationTest testcontainerTest exampleTest --rerun-tasks --no-parallel

# update metadata for dependency verification
[group("development")]
update-verification:
   @./gradlew dependencies --write-verification-metadata pgp,sha256 --export-keys --write-locks
