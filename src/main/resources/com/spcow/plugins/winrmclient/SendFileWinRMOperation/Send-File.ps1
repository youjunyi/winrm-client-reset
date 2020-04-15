function Send-File
{
	[CmdletBinding()]
	param
	(
		[Parameter(Mandatory)]
		[ValidateNotNullOrEmpty()]
		[string[]]$Path,

		[Parameter(Mandatory)]
		[ValidateNotNullOrEmpty()]
		[string]$Destination,

		[Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$ComputerName,

        [Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$UserName,

        [Parameter(Mandatory)]
        [ValidateNotNullOrEmpty()]
        [string]$Password,

        [string]$ConfigurationValue
	)
	process
	{
	    try
        {
            Write-Host "Connecting to remote host " $ComputerName "...."
            $SecretDetailsFormatted = ConvertTo-SecureString -AsPlainText -Force -String $Password
            $CredentialObject = New-Object -typename System.Management.Automation.PSCredential -argumentlist $UserName, $SecretDetailsFormatted
            $Session = New-PSSession -ComputerName $ComputerName -Credential $CredentialObject -ConfigurationName $ConfigurationValue
            Write-Host "Connected to remote host."
            foreach ($p in $Path)
            {
				if ($p.StartsWith('\\'))
				{
					Write-Host "[$($p)] is a UNC path. Copying locally first"
					Copy-Item -Path $p -Destination ([environment]::GetEnvironmentVariable('TEMP', 'Machine'))
					$p = "$([environment]::GetEnvironmentVariable('TEMP', 'Machine'))\$($p | Split-Path -Leaf)"
				}
				if (Test-Path -Path $p -PathType Container)
				{
					Write-Host $MyInvocation.MyCommand -Message "[$($p)] is a folder. Sending all files"
					$files = Get-ChildItem -Path $p -File -Recurse
					$sendFileParamColl = @()
					foreach ($file in $Files)
					{
						$sendParams = @{
							'Session' = $Session
							'Path' = $file.FullName
						}
						if ($file.DirectoryName -ne $p) ## It's a subdirectory
						{
							$subdirpath = $file.DirectoryName.Replace("$p\", '')
							$sendParams.Destination = "$Destination\$subDirPath"
						}
						else
						{
							$sendParams.Destination = $Destination
						}
						$sendFileParamColl += $sendParams
					}
					foreach ($paramBlock in $sendFileParamColl)
					{
					   	## Write-Host "$($paramBlock.Path)"
						## Send-File @paramBlock

					Write-Host "Starting WinRM copy of [$($paramBlock.Path)] to [$($Destination)]"
                     # Get the source file, and then get its contents
                     $sourceBytes = [System.IO.File]::ReadAllBytes($paramBlock.Path);
                    $streamChunks = @();
                    # Now break it into chunks to stream.
                    $streamSize = 1MB;
                    for ($position = 0; $position -lt $sourceBytes.Length; $position += $streamSize)
                     {
                      $remaining = $sourceBytes.Length - $position
                        $remaining = [Math]::Min($remaining, $streamSize)

                        $nextChunk = New-Object byte[] $remaining
                        [Array]::Copy($sourcebytes, $position, $nextChunk, 0, $remaining)
                        $streamChunks +=, $nextChunk
                        }
                        $remoteScript = {
                        if (-not (Test-Path -Path $using:Destination -PathType Container))
                        {
                        $null = New-Item -Path $using:Destination -Type Directory -Force
                        }

                        ## Write-Host "$($using:p) ---- $($using:paramBlock.Path)"
                        
                        $pathLength =  $using:p.Length
                        $ppath = $using:paramBlock.Path
                         ## Write-Host "ppath ------$($ppath)"
                         ## Write-Host "pathLength---$($pathLength)"
                         ## Write-Host "ppathlh---$($ppath.Length)"
                        $cpath  =  $ppath.substring($pathLength)
                        ## Write-Host "cpath---$($cpath)"
                        $fileDest = "$using:Destination\$($cpath)"
                        ## $fileDest = "$using:Destination\$($using:paramBlock.Path | Split-Path -NoQualifier)"
                        ## Create a new array to hold the file content
                        $destBytes = New-Object byte[] $using:length
                        $position = 0
                        ## Go through the input, and fill in the new array of file content
                        foreach ($chunk in $input)
                        {
                        [GC]::Collect()
                        [Array]::Copy($chunk, 0, $destBytes, $position, $chunk.Length)
                        $position += $chunk.Length
                        }
                         ## Write-Host "$($fileDest)"
                        $fileDirectory ="$($fileDest | Split-Path)"
                         ## Write-Host "$($fileDirectory)"
                        [IO.Directory]::CreateDirectory($fileDirectory)
                        [IO.File]::WriteAllBytes($fileDest, $destBytes)

                        Get-Item $fileDest
                        [GC]::Collect()
                        }

                        # Stream the chunks into the remote script.
                        $Length = $sourceBytes.Length
                        $streamChunks | Invoke-Command -Session $Session -ScriptBlock $remoteScript
                        Write-Host "WinRM copy of [$($paramBlock.Path)] to [$($Destination)] complete"
					}
				}
				else
				{
					Write-Host "Starting WinRM copy of [$($p)] to [$($Destination)]"
					# Get the source file, and then get its contents
					$sourceBytes = [System.IO.File]::ReadAllBytes($p);
					$streamChunks = @();

					# Now break it into chunks to stream.
					$streamSize = 1MB;
					for ($position = 0; $position -lt $sourceBytes.Length; $position += $streamSize)
					{
						$remaining = $sourceBytes.Length - $position
						$remaining = [Math]::Min($remaining, $streamSize)

						$nextChunk = New-Object byte[] $remaining
						[Array]::Copy($sourcebytes, $position, $nextChunk, 0, $remaining)
						$streamChunks +=, $nextChunk
					}
					$remoteScript = {
						if (-not (Test-Path -Path $using:Destination -PathType Container))
						{
							$null = New-Item -Path $using:Destination -Type Directory -Force
						}
						$fileDest = "$using:Destination\$($using:p | Split-Path -Leaf)"
						## Create a new array to hold the file content
						$destBytes = New-Object byte[] $using:length
						$position = 0

						## Go through the input, and fill in the new array of file content
						foreach ($chunk in $input)
						{
							[GC]::Collect()
							[Array]::Copy($chunk, 0, $destBytes, $position, $chunk.Length)
							$position += $chunk.Length
						}

						[IO.File]::WriteAllBytes($fileDest, $destBytes)

						Get-Item $fileDest
						[GC]::Collect()
					}

					# Stream the chunks into the remote script.
					$Length = $sourceBytes.Length
					$streamChunks | Invoke-Command -Session $Session -ScriptBlock $remoteScript
					Write-Host "WinRM copy of [$($p)] to [$($Destination)] complete"
				}
		    }
		}
        catch
        {
            Write-Host $_.Exception.Message
            exit 1
        }
	}
}
