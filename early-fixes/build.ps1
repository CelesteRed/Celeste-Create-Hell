# Builds celeste-early-fixes.jar and copies it into the pack.
# Needs a JDK 17+ (javac + jar). Usage:  powershell -ExecutionPolicy Bypass -File early-fixes\build.ps1
$ErrorActionPreference = 'Stop'
$Version = '1.0.6'

$root = $PSScriptRoot
$libs = Join-Path $root 'build\libs-compile'
$classes = Join-Path $root 'build\classes'
$out = Join-Path $root "build\celeste-early-fixes-$Version.jar"
$packMods = Join-Path $root '..\pack\mods'

$deps = @{
    'modlauncher-11.0.5.jar'    = 'https://maven.neoforged.net/releases/cpw/mods/modlauncher/11.0.5/modlauncher-11.0.5.jar'
    'relauncher-core-1.1.0.jar' = 'https://repo1.maven.org/maven2/com/juanmuscaria/relauncher-core/1.1.0/relauncher-core-1.1.0.jar'
    'jopt-simple-5.0.4.jar'     = 'https://repo1.maven.org/maven2/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar'
}

if (-not $env:JAVA_HOME) {
    $jdk = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -Filter 'jdk-*' -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
    if ($jdk) { $env:JAVA_HOME = $jdk.FullName }
}
$javac = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\javac.exe' } else { 'javac' }
$jar = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin\jar.exe' } else { 'jar' }

New-Item -ItemType Directory -Force $libs | Out-Null
foreach ($name in $deps.Keys) {
    $dest = Join-Path $libs $name
    if (-not (Test-Path $dest)) { Invoke-WebRequest -UseBasicParsing $deps[$name] -OutFile $dest }
}

if (Test-Path $classes) { Remove-Item -Recurse -Force $classes }
New-Item -ItemType Directory -Force $classes | Out-Null

$sources = Get-ChildItem (Join-Path $root 'src\main\java') -Recurse -Filter *.java | ForEach-Object { $_.FullName }
$cp = (Get-ChildItem $libs -Filter *.jar | ForEach-Object { $_.FullName }) -join ';'
& $javac --release 17 -encoding UTF-8 -Xlint:all -Xlint:-processing -cp $cp -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'javac failed' }

Copy-Item -Recurse -Force (Join-Path $root 'src\main\resources\*') $classes

$manifest = Join-Path $root 'build\MANIFEST.MF'
@"
Manifest-Version: 1.0
Automatic-Module-Name: com.celestered.earlyfixes
Implementation-Title: Celeste Early Fixes
Implementation-Version: $Version
Implementation-Vendor: CelesteRed

"@ | Set-Content -Encoding ASCII $manifest

if (Test-Path $out) { Remove-Item $out }
& $jar --create --file $out --manifest $manifest -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'jar failed' }

# Replace any older copy in the pack.
Get-ChildItem $packMods -Filter 'celeste-early-fixes-*.jar' -ErrorAction SilentlyContinue | Remove-Item
Copy-Item $out $packMods
Write-Host "Built $out and copied it to pack\mods. Run 'packwiz refresh' in pack\ next."
