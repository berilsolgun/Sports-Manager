#define MyAppName "Sports Manager"
#define MyAppVersion "1.0"
#define MyAppPublisher "Sports Manager Team"
#define MyAppExeName "SportsManager.bat"

[Setup]
AppId={{8E04F4D1-50F7-4E2E-950F-17DF8B348D4D}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
AppPublisher={#MyAppPublisher}
DefaultDirName={autopf}\Sports Manager
DefaultGroupName=Sports Manager
DisableProgramGroupPage=yes
OutputDir=Output
OutputBaseFilename=SportsManagerSetup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
ArchitecturesInstallIn64BitMode=x64compatible
PrivilegesRequired=lowest
SetupLogging=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "Create a desktop shortcut"; GroupDescription: "Additional shortcuts:"; Flags: unchecked

[Files]
Source: "..\target\sports-manager-1.0-SNAPSHOT.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\target\lib\*.jar"; DestDir: "{app}\lib"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "..\scripts\run-gui.bat"; DestDir: "{app}"; DestName: "{#MyAppExeName}"; Flags: ignoreversion
Source: "..\docs\UserManual.html"; DestDir: "{app}\docs"; Flags: ignoreversion

[Icons]
Name: "{group}\Sports Manager"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"
Name: "{group}\User Manual"; Filename: "{app}\docs\UserManual.html"
Name: "{autodesktop}\Sports Manager"; Filename: "{app}\{#MyAppExeName}"; WorkingDir: "{app}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "Launch Sports Manager"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\gamesession.json"

[Code]
function InitializeSetup(): Boolean;
var
  ResultCode: Integer;
begin
  if not Exec('java', '-version', '', SW_HIDE, ewWaitUntilTerminated, ResultCode) then
  begin
    MsgBox('Java Runtime was not found. Please install Java 21 or a newer Java Runtime before installing Sports Manager.', mbError, MB_OK);
    Result := False;
    Exit;
  end;

  if ResultCode <> 0 then
  begin
    MsgBox('Java Runtime could not be started. Please install Java 21 or a newer Java Runtime before installing Sports Manager.', mbError, MB_OK);
    Result := False;
    Exit;
  end;

  Result := True;
end;
