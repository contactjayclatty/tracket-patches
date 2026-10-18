# Build dist/patches-1.0.0.mpp from src/tracket/morphe/LifetimeUnlock.java.
# Needs JDK 17+ and the Morphe desktop 1.18.1 fat jar.
#   $env:MORPHE_JAR = 'C:\path\to\morphe-desktop-1.18.1-all.jar'
#   pwsh -File build.ps1

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$Version = '1.0.0'
$Src = Join-Path $Root 'src\tracket\morphe\LifetimeUnlock.java'
$Build = Join-Path $Root 'build'
$Dist = Join-Path $Root 'dist'
New-Item -ItemType Directory -Force -Path $Build, $Dist | Out-Null

function Find-Tool([string] $name, [string[]] $candidates) {
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -LiteralPath $candidate)) {
            return $candidate
        }
    }
    $cmd = Get-Command $name -ErrorAction SilentlyContinue
    if ($cmd) {
        return $cmd.Source
    }
    throw "$name was not found."
}

$javac = Find-Tool 'javac' @(
    $(if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\javac.exe' }),
    'C:\Android\jdk\bin\javac.exe'
)
$jarTool = Join-Path (Split-Path -Parent $javac) 'jar.exe'
if (-not (Test-Path -LiteralPath $jarTool)) {
    throw "jar.exe was not found next to $javac"
}

if ($env:MORPHE_JAR -and (Test-Path -LiteralPath $env:MORPHE_JAR)) {
    $morpheJar = $env:MORPHE_JAR
} else {
    $morpheJar = Find-Tool 'morphe-desktop' @(
        'C:\Android\tools\morphe\morphe-desktop-1.18.1-all.jar'
    )
}

& $javac --release 17 -encoding UTF-8 -cp $morpheJar -d $Build $Src
if ($LASTEXITCODE -ne 0) { throw "javac exited $LASTEXITCODE" }

$out = Join-Path $Dist "patches-$Version.mpp"
if (Test-Path -LiteralPath $out) { Remove-Item -LiteralPath $out }
Push-Location $Build
try {
    & $jarTool cf $out `
        'tracket/morphe/LifetimeUnlock.class' `
        'tracket/morphe/LifetimeUnlock$Build.class' `
        'tracket/morphe/LifetimeUnlock$Execute.class'
} finally {
    Pop-Location
}
if ($LASTEXITCODE -ne 0) { throw "jar exited $LASTEXITCODE" }

Copy-Item -LiteralPath $out -Destination (Join-Path $Root "tracket-lifetime-$Version.mpp") -Force
Get-Item -LiteralPath $out | Select-Object FullName, Length
