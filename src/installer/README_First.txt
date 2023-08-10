Project: Roxy Pos v4.6
Topic:	README installer
Author:	Jack Gerrard
Date: 	5 August 2018


************  Important Notice ******************
This version is 4.6

Roxy Pos v4.6 includes Enhancements and Bug-Fixes.

Please read the unicentaopos_4.6_readme in the Release Notes folder.

Java JRE 8 runtime is required.
The Roxy Pos installer now attempts to validate that Java JRE runtime is 
installed In all cases Java JRE MUST be installed and running properly for 
Roxy Pos to run.

On 64bit platforms; You may receive an error that javaw.exe cannot be found
when trying to run Roxy Pos. This is usually caused by having selected
the Java JRE if located in the :\Program Files(x86)\Java folder hence it
cannot be found in the Windows PATH settings.

Best/Quickest way to overcome is to either:
Select the :\Windows\System32 JRE during Roxy Pos installation OR
Copy javaw.exe into the :\Windows\System32 folder

For further information on installing Java refer to www.java.com

MySQL 5.6 or 5.7 (preferred) is required. MySQL 8 is not supported.
Use either the suggested MAMP server or full installation of the MYSQL server
from Oracle http://mysql.com
