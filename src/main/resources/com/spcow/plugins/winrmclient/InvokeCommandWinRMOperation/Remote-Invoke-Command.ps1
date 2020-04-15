function Remote-Invoke-Command
{
	[CmdletBinding()]
	param
	(
	    [Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$Path,

        [Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$ComputerName,

        [Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$UserName,

        [Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$Password

	)
	process
	{
	    try
	    {
            Write-Host "Connecting to remote host " $ComputerName "...."
            $SecretDetailsFormatted = ConvertTo-SecureString -AsPlainText -Force -String $Password
            $CredentialObject = New-Object -typename System.Management.Automation.PSCredential -argumentlist $UserName, $SecretDetailsFormatted
            $Session = New-PSSession -ComputerName $ComputerName -Credential $CredentialObject
            Write-Host "Connected to remote host."
            Write-Host "Executing commands..."
            Invoke-Command -Session $Session -FilePath $Path
            Write-Host "Executing commands finished."
        }
        catch
        {
            Write-Host $_.Exception.Message
            exit 1
        }
	}
}
