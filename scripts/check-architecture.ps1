#requires -Version 5.1
<#
架构适应度检查（Architecture Fitness Function）
--------------------------------------------------
在无单元测试的项目里，用"依赖方向"作为可执行的质量门禁：
  控制器 → 应用服务 → 领域/基础设施
任何违规都意味着业务逻辑或 SQL 泄漏到了错误的层。

设计原则：棘轮（ratchet）
- 存量债务记录在 architecture-baseline.json，只允许减少，不允许增加；
- 新代码必须零违规，否则本次检查失败（退出码 1）。
- 每清理一批债务后执行 -Update 收紧基线，门禁随之变严。

用法：
  powershell -File scripts/check-architecture.ps1          # 检查
  powershell -File scripts/check-architecture.ps1 -Update  # 提交当前状态为新的基线
#>
param(
    [switch]$Update
)

$ErrorActionPreference = 'Stop'

$backendRoot = Join-Path $PSScriptRoot '..\pet-backend\src\main\java'
$packageRoot = (Resolve-Path (Join-Path $backendRoot 'com\pethome')).Path
$baselineFile = Join-Path $PSScriptRoot 'architecture-baseline.json'

$files = Get-ChildItem -Path $packageRoot -Recurse -File -Filter *.java

function Get-PackageName($file) {
    $rel = $file.FullName.Substring($packageRoot.Length + 1)
    $segments = $rel -split '\\'
    if ($segments.Count -le 1) { return 'pethome' }
    return $segments[0..($segments.Count - 2)] -join '.'
}

# 规则定义。Match 返回 $true 即视为一次违规（以文件数计，避免同一文件多处引用重复计数）
$rules = @(
    @{ Id = 'controller-to-mapper';       Desc = 'Controller 直接访问 Mapper（表现层承担了持久化）'; Scope = 'controller';   Test = { param($pkg, $text) $text -match 'com\.pethome\.mapper\.' } },
    @{ Id = 'controller-transactional';   Desc = 'Controller 声明事务（事务边界位于表现层）';         Scope = 'controller';   Test = { param($pkg, $text) $text -match '@Transactional' } },
    @{ Id = 'task-to-mapper';             Desc = '定时任务直接访问 Mapper（绕过服务层）';             Scope = 'task';         Test = { param($pkg, $text) $text -match 'com\.pethome\.mapper\.' } },
    @{ Id = 'infra-to-domain';            Desc = '工具/通用包反向依赖 service（破坏依赖倒置）';       Scope = 'util|utils|common'; Test = { param($pkg, $text) $text -match 'import com\.pethome\.(service|controller)\.' } },
    @{ Id = 'config-to-domain';           Desc = '配置包承担业务装配（启动期数据初始化未隔离）';       Scope = 'config';       Test = { param($pkg, $text) $text -match 'import com\.pethome\.(service|mapper)\.' } },
    @{ Id = 'service-to-controller';      Desc = '服务层反向依赖表现层（依赖倒置）';                   Scope = 'service';      Test = { param($pkg, $text) $text -match 'import com\.pethome\.controller\.' } },
    @{ Id = 'controller-to-serviceimpl';  Desc = 'Controller 直接依赖 Service 实现类（破坏接口边界）';   Scope = 'controller';   Test = { param($pkg, $text) $text -match 'import com\.pethome\.service\.impl\.' } },
    @{ Id = 'entity-to-domain';           Desc = '实体反向依赖服务/持久化/表现层';                     Scope = 'entity';       Test = { param($pkg, $text) $text -match 'import com\.pethome\.(service|mapper|controller)\.' } },
    @{ Id = 'mapper-to-domain';           Desc = 'Mapper 反向依赖服务/表现层';                         Scope = 'mapper';       Test = { param($pkg, $text) $text -match 'import com\.pethome\.(service|controller)\.' } },
    @{ Id = 'package-cycle';              Desc = '包级双向依赖（循环依赖）';                           Scope = '*';            Test = { param($pkg, $text) $false } }
)

$violations = [ordered]@{}
foreach ($rule in $rules) { $violations[$rule.Id] = @() }

foreach ($file in $files) {
    $pkg = Get-PackageName $file
    $text = Get-Content -Path $file.FullName -Raw
    foreach ($rule in $rules) {
        if ($rule.Id -eq 'package-cycle') { continue }
        if ($pkg -notmatch ('^(' + $rule.Scope + ')')) { continue }
        if (& $rule.Test $pkg $text) {
            $violations[$rule.Id] += (Join-Path $pkg $file.Name)
        }
    }
}

# 包级循环依赖：A 依赖 B 且 B 依赖 A
$edges = @{}
foreach ($file in $files) {
    $pkg = Get-PackageName $file
    $text = Get-Content -Path $file.FullName -Raw
    $targets = [regex]::Matches($text, 'import\s+com\.pethome\.([^;]+);') |
        ForEach-Object { $_.Groups[1].Value.Substring(0, $_.Groups[1].Value.LastIndexOf('.')) } |
        Sort-Object -Unique
    foreach ($t in $targets) {
        if ($t -eq $pkg) { continue }
        if (-not $edges.ContainsKey($pkg)) { $edges[$pkg] = @{} }
        $edges[$pkg][$t] = $true
    }
}
$cycles = @()
foreach ($a in $edges.Keys) {
    foreach ($b in $edges[$a].Keys) {
        if ($edges.ContainsKey($b) -and $edges[$b].ContainsKey($a) -and $a -lt $b) {
            $cycles += "$a <-> $b"
        }
    }
}
$violations['package-cycle'] = $cycles

# 读取基线
$baseline = @{}
if ((Test-Path $baselineFile) -and -not $Update) {
    $json = Get-Content -Path $baselineFile -Raw | ConvertFrom-Json
    foreach ($prop in $json.PSObject.Properties) { $baseline[$prop.Name] = [int]$prop.Value }
}

$current = @{}
$failed = $false
Write-Host ''
Write-Host '架构适应度检查' -ForegroundColor Cyan
Write-Host ('后端源码根目录: ' + (Resolve-Path $packageRoot).Path)
Write-Host ''

foreach ($rule in $rules) {
    $count = @($violations[$rule.Id]).Count
    $current[$rule.Id] = $count
    $allowed = if ($baseline.ContainsKey($rule.Id)) { $baseline[$rule.Id] } else { 0 }
    $status = if ($count -gt $allowed) { 'FAIL' } elseif ($count -eq 0) { 'PASS' } else { 'DEBT' }
    $color = switch ($status) { 'FAIL' { 'Red' } 'PASS' { 'Green' } default { 'Yellow' } }
    Write-Host ('[{0}] {1,-24} 当前 {2,3} / 基线 {3,3}  {4}' -f $status, $rule.Id, $count, $allowed, $rule.Desc) -ForegroundColor $color
    if ($status -eq 'FAIL') {
        $failed = $true
        $violations[$rule.Id] | Select-Object -First 10 | ForEach-Object { Write-Host ('        ' + $_) -ForegroundColor DarkRed }
    }
}

Write-Host ''
if ($Update) {
    $obj = [ordered]@{}
    foreach ($rule in $rules) { $obj[$rule.Id] = $current[$rule.Id] }
    $obj | ConvertTo-Json | Set-Content -Path $baselineFile -Encoding UTF8
    Write-Host ('基线已更新: ' + $baselineFile) -ForegroundColor Green
}
elseif ($failed) {
    Write-Host '架构检查未通过：存在新增违规，请修正或（确属存量）执行 -Update。' -ForegroundColor Red
    exit 1
}
else {
    Write-Host '架构检查通过。' -ForegroundColor Green
    exit 0
}
