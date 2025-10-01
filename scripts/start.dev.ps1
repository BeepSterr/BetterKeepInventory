rm ..\_dev_server\plugins\BetterKeepInventory.jar
cp ..\plugin\target\BetterKeepInventory-*-shaded.jar ..\_dev_server\plugins\BetterKeepInventory.jar
cd _dev_server
C:\Users\micro\AppData\Roaming\ModrinthApp\meta\java_versions\zulu21.44.17-ca-jre21.0.8-win_x64\bin\java.exe -Xms4G -Xmx4G -XX:+UseG1GC -jar spigot.jar nogui