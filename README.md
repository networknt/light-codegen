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


### Kafka schema registration migration

Kafka templates use `auto.register.schemas` and the corresponding
`kafka-producer.auto.register.schemas`, `kafka-consumer.auto.register.schemas`,
and `kafka-streams.auto.register.schemas` overrides in `values.yml`.
**Before regenerating or upgrading configuration**, rename existing
`kafka-*.schema.registry.auto.register.schemas` overrides. The old override names
are unused: an old `false` override alone leaves the canonical default `true`.
Generated `values.yml` repeats this warning next to the canonical settings.
Confluent ignored the old `schema.registry.auto.register.schemas` property, so
renaming an old `false` setting makes lookup-only behavior effective. Register
schemas before selecting canonical `false`; `true` allows automatic registration.
Light's own Avro serializer retains its legacy fallback for handwritten old-key
configurations, with the canonical key taking precedence. Users relying on that
fallback must also migrate their values overrides to retain a `false` setting.

## Kafka TLS endpoint identification (#265)

Generated producer, consumer and Streams TLS examples now show `https` for
`ssl.endpoint.identification.algorithm`. The lines remain commented, preserving
Kafka's native hostname-verification default until explicitly enabled. If an
older generated example was uncommented, replace its `algo-name` value or
external override with `https`; `algo-name` is not a valid Java identification
algorithm and fails a TLS handshake. The `kafka-*.ssl.endpoint.identification.algorithm`
keys remain unchanged. An explicitly configured empty string disables hostname
verification; it is not the default. Certificates must match the broker hostname.
The generation regression covers both generators and all eight Kafka flag combinations.
