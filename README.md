# RiskBoardGame
For further information on the project, please visit our (german) wiki: https://github.com/elsantner/RiskBoardGame/wiki

---
### How to install (incl. server setup):
1. Download the latest version of the .apk and .jar here: https://github.com/elsantner/RiskBoardGame/releases
2. Setup a webserver of your choice and make sure Java is installed and **Port 53216** is open.
3. Start the server-<version>.jar on your webserver using the following command:

        java -cp server-<version>.jar edu.aau.se2.server.MainServer
        
4. Install the Risiko_<version>.apk on the android device of your choice. (You might have to allow apps from unknown sources since this apk is not signed.)
5. The connection between client and server should now work and you can start playing.
