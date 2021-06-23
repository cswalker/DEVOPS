#ps1_sysnative
$ErrorActionPreference = 'Stop'
$path = $env:SystemRoot + '\Temp\'
$logFile = $path + "CloudInit_$(get-date -f yyyy-MM-dd).log"
$wind_password = "{{ secrets['wind_password'] }}" | ConvertTo-SecureString -asPlainText -Force
$wind_username = "{{ secrets['wind_username'] }}"
function Get-TimeStamp {
    return "[{0:MM/dd/yy} {0:HH:mm:ss}]" -f (Get-Date)
}

function Write-Tracking{
    Param (
        [string]$Message
    )
    Write-Output "$(Get-Timestamp) $Message" | Out-File -FilePath $logFile -Append
}

function Get-ElevatedPermissions{
    Write-Tracking -Message 'Checking Elevated Permissions'
    # Get the ID and security principal of the current user account
    $myWindowsID=[System.Security.Principal.WindowsIdentity]::GetCurrent()
    $myWindowsPrincipal=new-object System.Security.Principal.WindowsPrincipal($myWindowsID)
    # Get the security principal for the Administrator role
    $adminRole=[System.Security.Principal.WindowsBuiltInRole]::Administrator
    # Check to see if we are currently running "as Administrator"
    if (-Not $myWindowsPrincipal.IsInRole($adminRole)){
        Write-Tracking -Message 'ERROR: You need elevated Administrator privileges in order to run this script.'
        Write-Tracking -Message ' Start Windows PowerShell by using the Run as Administrator option.'
        Exit 2
    }else{
    Write-Tracking -Message 'Permissions are Elevated.'
    }
}

function Set-SSHFirewallRules{
    Write-Tracking -Message 'Forcing global SSH firewall access.'
    $fw = New-Object -ComObject HNetCfg.FWPolicy2
    # try to find/enable the default rule first
    $add_rule = $false
    $matching_rules = $fw.Rules | Where-Object { $_.Name -eq 'Windows Remote Management (SSH-In)' }
    $rule = $null
    if ($matching_rules) {
        if ($matching_rules -isnot [Array]) {
            Write-Tracking -Message 'Editing existing single SSH firewall rule.'
            $rule = $matching_rules
        }else {
            # try to find one with the All or Public profile
            Write-Tracking -Message 'Found multiple existing SSH firewall rules.'
            $rule = $matching_rules | ForEach-Object { $_.Profiles -band 4 }[0]
            if (-not $rule -or $rule -is [Array]) {
                Write-Tracking -Message 'Editing an arbitrary single SSH firewall rule (multiple existed).'
                # oh well, just pick the first one
                $rule = $matching_rules[0]
            }
        }
    }elseif (-not $rule) {
        Write-Tracking -Message 'Creating a new SSH firewall rule.'
        $rule = New-Object -ComObject HNetCfg.FWRule
        $rule.Name = 'Windows SSH Management (SSH-In)'
        $rule.Description = 'Inbound rule for Windows SSH. [TCP 22]'
        $add_rule = $true
    }
    $rule.Profiles = 0x7FFFFFFF
    $rule.Protocol = 6
    $rule.LocalPorts = 22
    $rule.RemotePorts = '*'
    $rule.LocalAddresses = '*'
    $rule.RemoteAddresses = '*'
    $rule.Enabled = $true
    $rule.Direction = 1
    $rule.Action = 1
    $rule.Grouping = 'Windows Remote Management'
    if ($add_rule) {
        $fw.Rules.Add($rule)
    }
    Write-Tracking -Message "SSH firewall rule $($rule.Name) updated"
}

Function Install-Chocolatey {
    Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
}

# Create Log File
New-Item $logFile -ItemType file
Write-Tracking -Message 'Logfile Created...'

# Error Check for Elevated Permissions
Get-ElevatedPermissions

# Detect PowerShell version.
Write-Tracking -Message 'Checking Powershell Version...'
if ($PSVersionTable.PSVersion.Major -lt 3){
    Write-Tracking -Message 'PowerShell version 3 or higher is required.'
    Throw "PowerShell version 3 or higher is required."
}else {
    Write-Tracking -Message 'PowerShell is version 3 or higher.'
}

# Setup Local Account
Write-Tracking -Message 'Adding Local User...'
New-LocalUser -Name $wind_username -Password $wind_password -AccountNeverExpires -UserMayNotChangePassword | Set-LocalUser -PasswordNeverExpires $true
Write-Tracking -Message 'Local User Added; Adding to Admin Group.'
Add-LocalGroupMember -Name 'Administrators' -Member $wind_username
Write-Tracking -Message 'Added Local User to Admin Group.'

# Install Cocholatey
Write-Tracking -Message 'Installing Chocolatey'
Install-Chocolatey
Write-Tracking -Message 'Installed Chocolatey'

# Install Open SSH Through Chocalatey
Write-Tracking -Message 'Installing OpenSSH'
choco upgrade -y --install-if-not-installed openssh -params '"/SSHServerFeature"'
Write-Tracking -Message 'Installed OpenSSH'

# Set SSH Firewall Rules
Write-Tracking -Message 'Opening SSH Port'
Set-SSHFirewallRules
Write-Tracking -Message 'SSH Port Opened'
