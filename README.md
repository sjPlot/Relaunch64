Changelog
================

Relaunch64 3.0.1
----------------

### New features
* goto prev/next error also searches inside included files
* goto error now scrolls the log to the related line with error discription
* auto-completion for functions, macros and scripting commands (Kick Assembler, 64tass) (press ctrl+shift+space while typing)
* automatic goto error line when compiling errors occur
* added support for CA65 (not fully implemented yet)
* added quick references (menu view)
* added regular expression checkbox to find-command, to explicitly en-/disable search for regular expressions
* added syntax highlighting for scripting commands (Kick Assembler, 64tass)

### Bug fixes
* bug fix with "Jump to label" function with Kick Assembler
* bug fix with "Find next" when content was changed
* changed auto-completion feature, which did not work with labels with less than 3 chars, and now works for labels with at least 1 char

