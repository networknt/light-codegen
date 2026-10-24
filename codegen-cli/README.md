# YAML parameterizer

`YAMLFileParameterizer` uses SnakeYAML Engine 3.0.1's low-level `Compose` API with
source marks enabled and the YAML 1.2 Core schema. Engine is a direct dependency
of `codegen-cli`; the parameterizer no longer imports classic SnakeYAML.

This utility composes nodes and rewrites source lines. It does not construct Java
boolean, number, or date values or dump a new YAML document. Scalar defaults such
as `on`, `0755`, `0b11`, `1_000`, `1:30`, and dates retain their original scalar text
in generated placeholders. Existing generation switches, single-document handling,
collection skipping, and duplicate-key source behavior are retained. This is not
a new YAML formatter; existing comment-boundary and trailing-blank-line behavior
remains unchanged.

The protected helpers that accept or return YAML nodes now use Engine node types
instead of classic node types. Subclasses and package clients using those helpers
must adapt and recompile; this is a source/binary compatibility change. Project
versions remain `2.4.2-SNAPSHOT` for the owner's coordinated release update, not
a guarantee of compatibility with previously compiled subclasses.

Classic SnakeYAML 2.7 remains managed for Jackson YAML and other transitive
consumers. Removing that pin allowed Jackson's dependency to resolve to 2.5 in
core/framework/CLI/web modules. The REST generator's existing compile-scope classic
dependency is retained as a compatibility export. This migration does not remove
classic SnakeYAML from every classpath or change Jackson's YAML parser.

The active Engine regression tests use temporary files and a dedicated resource;
the old disabled test class remains unchanged. Runtime deployment, published
artifact consumers, and external subclass compatibility require separate review.
