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
integrationTest:
    @./gradlew integrationTest --rerun-tasks --no-parallel

# run end-to-end tests
[group("development")]
e2eTest:
    @./gradlew e2eTest --rerun-tasks --no-parallel

# run testcontainer tests
[group("development")]
testcontainerTest:
    @./gradlew testcontainerTest --rerun-tasks --no-parallel

# run example tests
[group("development")]
exampleTest:
    @./gradlew exampleTest --rerun-tasks --no-parallel

# run all tests
[group("development")]
allTest:
    @./gradlew test integrationTest testcontainerTest exampleTest --rerun-tasks --no-parallel

# write metadata for dependency verification
[group("development")]
write-verification:
   @./gradlew dependencies --write-verification-metadata pgp,sha256 --export-keys --write-locks
