#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases.
; #Warn  ; Enable warnings to assist with detecting common errors.
SendMode Input  ; Recommended for new scripts due to its superior speed and reliability.
SetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory.
; Ensures that there is only a single instance of this script running
#SingleInstance, Force
; sets title matching to search for "containing" instead of "exact"
SetTitleMatchMode, 2
SetBatchLines -1
if WinExist("Discord") {
		WinActivate, Discord
		Sleep 1000
		Send, ^k
		Sleep 1000
                Send, {!}%1%
		Sleep 1000
		Send, {Enter}
		Sleep 1000
		WinActivate, vlcj player
		Sleep 1000
		Send, ^+G
}
return