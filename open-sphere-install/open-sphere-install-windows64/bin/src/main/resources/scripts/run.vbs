Set WshShell = CreateObject("WScript.Shell")

If WScript.Arguments.Count > 0 Then
  If WScript.Arguments.Item(0) = "-console" Then
    console = 1
  End If
ElseIf Not WshShell.ExpandEnvironmentStrings("%OPENSPHERE_JAVA_CONSOLE%") = "%OPENSPHERE_JAVA_CONSOLE%" Then
  console = 1
End If

WshShell.Run chr(34) & "run.bat" & Chr(34), console
Set WshShell = Nothing
