#!/usr/bin/env python3
"""Generate and compile Java/Kotlin projects using an explicit isolated Maven repository."""
import argparse
import json
import os
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def run(command, cwd=ROOT):
    print("+", " ".join(map(str, command)), flush=True)
    subprocess.run(list(map(str, command)), cwd=cwd, check=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--maven-repo", required=True, type=Path)
    parser.add_argument("--gradle-home", required=True, type=Path)
    parser.add_argument("--offline", action="store_true")
    args = parser.parse_args()
    repo = args.maven_repo.resolve()
    assert repo != (Path.home() / ".m2/repository").resolve(), "Use an isolated Maven repository"
    jar = ROOT / "codegen-cli/target/codegen-cli.jar"
    assert jar.is_file(), "Run the full Maven install reactor first"
    (ROOT / "target").mkdir(exist_ok=True)
    output = Path(tempfile.mkdtemp(prefix="generated-build-qualification-", dir=ROOT / "target"))
    scenarios = {
        "rest": ("light-rest-4j", "openapi.yaml", "config.json"),
        "kotlin": ("light-rest-4j", "openapi.yaml", "config.json"),
        "lambda": ("light-rest-4j", "openapilambda.yaml", "configlambdamaven.json"),
        "lambdanative": ("light-rest-4j", "openapilambda.yaml", "configlambdamaven.json"),
        "hybrid": ("light-hybrid-4j", "spec.yaml", "serviceConfig.json"),
        "graphql": ("light-graphql-4j", "schema.graphqls", "config.json"),
    }
    for name, (module, model, configuration) in scenarios.items():
        resources = ROOT / module / "src/test/resources"
        config = json.loads((resources / configuration).read_text())
        config["buildMaven"] = name != "kotlin"
        config["packageDocker"] = False
        if name in ("lambda", "lambdanative"):
            config["lambdaTrigger"] = "REST"
            config["useLightProxy"] = False
        config_file = output / (name + "-config.json")
        config_file.write_text(json.dumps(config))
        project = output / name
        run(["java", "-cp", jar, ROOT / "scripts/GenerateQualificationProject.java",
             name, project, resources / model, config_file])
        if name == "kotlin":
            command = ["bash", "gradlew", "--no-daemon", "--gradle-user-home", args.gradle_home.resolve(),
                       f"-Dmaven.repo.local={repo}", "compileKotlin", "compileTestKotlin"]
            if args.offline:
                command.insert(2, "--offline")
            run(command, project)
        else:
            poms = [project / "pom.xml"] if (project / "pom.xml").exists() else sorted(project.glob("*/pom.xml"))
            assert poms, f"No generated Maven projects for {name}"
            for pom in poms:
                command = ["mvn", f"-Dmaven.repo.local={repo}", "test-compile"]
                if args.offline:
                    command.insert(1, "-o")
                run(command, pom.parent)
        for dockerfile in project.rglob("*"):
            if dockerfile.is_file() and "dockerfile" in dockerfile.name.lower():
                for line in dockerfile.read_text().splitlines():
                    if line.startswith("FROM ") and any(image in line.lower() for image in ["openjdk", "corretto", "lambda/java"]):
                        assert ":25" in line or "-25" in line, (dockerfile, line)
    print("Generated REST, hybrid, GraphQL, Lambda, Lambda native and Kotlin compilation passed")


if __name__ == "__main__":
    main()
