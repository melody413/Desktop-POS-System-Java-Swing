@echo off
start /B javaw -Dlogging.level=INFO -Xms256m -Xmx1024m -cp %CP% -splash:unicenta_splash_dark.png com.openbravo.pos.forms.StartPOS %1