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

---
### How to compile:
After checking this project out you can either import it to Android Studio and compile it using the IDE, or you can just use *gradle*.
To compile the project using just gradle, first run the command in the root folder of the project (RiskBoardGame):

        ./gradlew build
        
This should have generated a .apk file at *RiskBoardGame/android/build/outputs/apk/release*.

Then, you can create a executable .jar by running 

        ./gradlew shadowJar
This will generate a .jar containing this projects code & assets and all required libraries at *RiskBoardGame/server/build/libs*.
