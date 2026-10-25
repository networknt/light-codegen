A code generator based on [rocker](https://github.com/fizzed/rocker) that can be used as a command line utility or web service.

[Stack Overflow](https://stackoverflow.com/questions/tagged/light-4j) |
[Google Group](https://groups.google.com/forum/#!forum/light-4j) |
[Gitter Chat](https://gitter.im/networknt/light-codegen) |
[Subreddit](https://www.reddit.com/r/lightapi/) |
[Youtube Channel](https://www.youtube.com/channel/UCHCRMWJVXw8iB7zKxF55Byw) |
[Documentation](https://doc.networknt.com/tool/light-codegen/) |
[Contribution Guide](https://doc.networknt.com/contribute/) |

[![Build Status](https://travis-ci.org/networknt/light-codegen.svg?branch=master)](https://travis-ci.org/networknt/light-codegen)

The Light platform contains several frameworks to build different styles of APIs or microservices based on the contract specifications. It encourages users to create service contract specification before coding, and it would be nice if we can scaffold a project based on the designed specification. Further, it would be even sweeter if we could utilize the specification during runtime to verify the security and validate input request/out response based on the same specification.

Before we built our generator, we looked at the open-source community and couldn't find anything suitable for our needs. So we created one over a weekend, and it turned out very successful. If you want to know more about it, please check out the [light-codegen document](https://doc.networknt.com/tool/light-codegen/).

The generator is very easy to use, and there are numeric ways to use it. Command-line, Docker, Scripting, and [Web UI](https://codegen.lightapi.net). To get started, please follow the [getting-started](https://doc.networknt.com/getting-started/light-codegen/).

If you want to know all the options with the light-codegen, you can follow the tutorials for each generator at [light-codegen tutorial](https://doc.networknt.com/tutorial/generator/).

Also, there are so many [other tutorials](https://doc.networknt.com/tutorial/) that are using light-codegen to scaffold projects, and these can be found in the tutorial section.

## Jackson 3 source and generated projects

This source line requires the Jackson 3 light-4j/http-client/REST prerequisites, Jackson 3.2.3, and yaml-rule 4.0.0-SNAPSHOT where used. Public Generator JsonNode/ObjectMapper types now use tools.jackson; annotations retain com.fasterxml.jackson.annotation. JSON and YAML mappers preserve the measured Jackson 2 common feature policy and stream constraints. They are immutable; YAML input uses YAML 1.2 scalars. Java time and Optional support are built into Jackson 3. Classic SnakeYAML cleanup remains deferred.

Generated Java Maven projects target Java 25 to match the current framework binaries. Their Jackson BOM/dependencies and json-schema-validator use the Jackson 3 line. REST uses Logstash Encoder 9.0 to avoid reintroducing Jackson 2. Lambda native dependencies now select the migrated framework snapshot rather than the older Jackson 2 snapshot. Generated project versions remain supplied by the user config.

Kotlin Gradle output uses build.gradle.kts/settings.gradle.kts, Gradle 9.3.1, Kotlin 2.3.20, Java 25 and jackson-module-kotlin 3.2.3. Local qualification covers this Gradle path; Kotlin Maven output is not qualified.

Local qualification includes the full generator reactor and fresh generated Java main/test compilation for REST, hybrid, GraphQL, Lambda and Lambda native, plus an offline Kotlin Gradle main/test compile in isolated caches. Generated service tests were compiled but not executed. No Docker build, service deployment or AWS execution is implied.

### Reproducible Jackson 3 source build

`.github/workflows/jackson3-source-build.yml` builds the prerequisite source stack in a fresh isolated Maven repository and runs its full test suites. It resolves every prerequisite to a full commit SHA once and uploads `source-refs.json`, `source-commits.txt` and Surefire reports. The shared mapper policy prerequisite is light-4j #2821; merge that before its consumers.

The optional `source_refs` workflow-dispatch input replays recorded full SHAs. GitHub enables dispatch only after the workflow exists on the default branch; before merge, reproduce from the recorded source checkouts locally. A green local suite is separate from a successful remote source-build run.

YAML generator configuration uses `GeneratorConfigYaml.readTree` at the CLI and web input boundaries. This preserves unquoted legacy `yes/no/on/off` flags while quoted strings and JSON strings stay strings. OpenAPI documents continue to use the YAML 1.2 `Generator.yamlMapper`; external callers parsing YAML configuration should use the configuration reader.

Generated Docker templates use Java 25 images to match the emitted compiler targets. CI invokes `scripts/verify-generated-builds.py`, generating fresh REST, hybrid, GraphQL, Lambda, Lambda native and Kotlin projects and compiling their main and test sources. Reproduce after the full source stack and codegen reactor have been installed:

```sh
python3 scripts/verify-generated-builds.py --maven-repo /path/to/isolated/m2 --gradle-home /path/to/isolated/gradle --offline
```

The check uses all emitted Lambda function Maven projects. It does not build or push container images, invoke AWS, or qualify Kotlin Maven mode.

### Exact-source CI qualification

Consumer CI requires the latest full-core workflow for the exact resolved light-4j SHA to have completed successfully. Pending, failed or missing core qualification blocks the build. The core workflow continues to run its full reactor tests; consumers install that qualified source and run their own full tests and other prerequisite suites. `core-qualification.json` records the accepted run.

The full dependency ledger is audited across all groups. Every NetworkNT release and snapshot must be source-installed, apart from the explicitly SHA-256-pinned Jackson 3 validator release. Known Jackson 2 yaml-rule and chaos-monkey releases are rejected even with a local-install marker. Jackson 2 dependencies in other groups are reported; ready migrations reject them except shared annotations.

Generator config preserves legacy yes/no/on/off values through a public SnakeYAML event pass followed by Jackson parsing. It does not subclass Jackson parser internals. Keys, quoted strings, explicit string tags, numeric forms and alias handling retain their measured behavior. This two-pass normalization applies only to generator configuration.
