# test-and-stuff

How to Travis + Sonatype + Codecov a multi-module SBT build

## Helper

A short helper for [gitignore.io](https://gitignore.io/):

```bash
$ which gi
gi () {
  curl -fL https://www.gitignore.io/api/${(j:,:)@}
}
```

## Essentials

### .gitignore

```bash
$ gi scala,emacs,java,sbt > .gitignore
```

### project/plugin.sbt

```scala
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.2.1")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
```

### build.sbt

```scala
inThisBuild(List(
  organization := "<ORGANIZATION>",
  homepage := Some(url("https://github.com/<ORG>/<REPO>")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "<ALIAS>",
      "<NAME>",
      "<EMAIL>",
      url("<WEBSITE>")
    )
  )
))
```

## Signing key

### Generate

```bash
$ gpg --gen-key
```

Enter your name, email, passphrase

Output should be similar to

```bash
[...]
pub   rsa2048 2018-08-24 [SC] [expires: 2020-08-24]
      <KEY_ID>
[...]
```

Grab the `KEY_ID` and store it in a variable

```bash
$ export KEY_ID=<KEY_ID>
```

### Submit to key server

Copy the output of this:
```bash
$ gpg --armor --export "${KEY_ID}"
```

Paste the public key into https://pgp.mit.edu/ and submit.
Also, for quicker propagation, do the same on http://keyserver.ubuntu.com/

### Export private key

`$ gpg --armor --export-secret-keys $LONG_ID | base64 -w0 > project/secret`

Enter the passphrase used when generating the key.
The key is now Base64-encoded in `project/secret`

## Travis configuration

### Initialize

```bash
$ travis init scala --scala 2.12.6 --jdk openjdk8 --before-install 'git fetch --tags'
# Make sure PASSPHRASE, SONATYPE_USER and SONATYPE_PASS are escaped properly (despite the quotation)!!!
# Getting these escaped correctly is literally the hardest part :D
$ travis encrypt 'PGP_PASSPHRASE=<PASSPHRASE>' --add
$ travis encrypt 'SONATYPE_USERNAME=<SONATYPE_USER>' --add
$ travis encrypt 'SONATYPE_PASSWORD=<SONATYPE_PASS>' --add
$ travis encrypt-file ./project/secret ./project/zecret --add
$ echo "project/secret" >> .gitignore
```

Fix the line break in the `openssl` command, it's probably broken...
Also, add ` || true` to the end of it, so Pull Request builds will work

### Add actual build

Add this line to your modules (or common settings)

`build.sbt`

```scala
[...]
settings(crossScalaVersions := List("2.12.6", "2.11.12"))
[...]
```

`.travis.yml`

```yaml
stages:
- name: build
- name: release
  if: (branch = master AND type = push) OR (tag IS present)
jobs:
  include:
    - &build
      stage: build
      name: "Build"
      script: sbt ++$TRAVIS_SCALA_VERSION! coverage test coverageReport
      after_success: bash <(curl -s https://codecov.io/bash)
    - &release
      stage: release
      name: "Release artifacts"
      before_script: export PGP_SECRET="$(<./project/secret)"
      script: sbt ci-release || sbt sonatypeReleaseAll

    - <<: *build
      scala: 2.11.12
      name: "Build for 2.11"
```

### Add Codecov config

`.codecov.yml`

```yaml
comment: off
coverage:
  status:
    project:
      default: off
    module1:
        flags: module1
        target: auto
        threshold: 100
        base: auto
    module2:
        flags: module2
        target: auto
        threshold: 100
        base: auto
    patch:
      default: off
      module1:
        flags: module1
        threshold: 100
        base: auto
      module2:
        flags: module2
        threshold: 100
        base: auto
flags:
  module1:
    paths:
      - module1/
  module2:
    paths:
      - module2/
```
