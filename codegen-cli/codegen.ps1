Set-PSDebug -Off
#Set-PSDebug -Trace 2

$workDir = $PSScriptRoot
$cliJar = "$workDir\codegen-cli.jar"

if (-not (Test-Path $cliJar)) {
    Write-Host "$cliJar cannot be found."
    Exit 1
}

if (-not (Test-Path env:JAVA_HOME)) {
    Write-Host "JAVA_HOME is not set."
    Exit 1
}

$compilerConf = "$workDir\rocker-compiler.conf"

if (-not (Test-Path $compilerConf)) {
    Write-Host "generate config $compilerConf"
	# Note: do not add quotation marks to the value of rocker.template.dir even if there are spaces in the path
    $escapedDir = $workDir -replace '\\', '/'
    Set-Content -Path $compilerConf -Value "rocker.template.dir=$escapedDir"
}

$classDir = "$workDir\target\classes"

if (-not (Test-Path -PathType Container $classDir)) {
    Write-Host "create class folder $classDir"
    New-Item -ItemType Directory -Force -Path $classDir
}

# Note: please point JAVA_HOME to a JDK installation. JRE is not sufficient.
$javaHome = (get-item env:JAVA_HOME).Value
# Note: the order of the pathes in the classpath matters
$classpath = ".;target\classes;codegen-cli.jar"

& $javaHome\bin\java -cp $classpath com.networknt.codegen.Cli $args
