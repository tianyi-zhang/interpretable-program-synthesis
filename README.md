
# Interpretable Program Synthesis for Regular Expressions

## Software Prerequisites
1. Java 1.8 or higher ([download](https://www.java.com/en/download/))
2. Python 3

**Note:** Use `java --version` to check the versions. We recommend using the 64-bit version of Java so we can allocate more memory to the program synthesizer that runs in a JVM. If you use a 32-bit version, we can only allocate a maximum amount of 4G memory to the synthesizer theorectically. In practice, the actual allocated memory could be as low as 1G. When downloading Java, please try to download the distribution or installer with `x64` in its name (not `x86`).  

## Install from Pre-built Distribution

1. Download our software distribution [here](https://drive.google.com/file/d/13PjquOCUHdmQGPOseWEiBPP3AavpJQTb/view?usp=sharing).
2. Unzip the downloaded file.
3. If you are a Mac user, please copy `lib/libz3java.dylib`, `lib/com.microsoft.z3.jar`, and `lib/libz3.dylib` to `/usr/local/lib`. 
4. In terminal, go into the unzipped folder and start the server.
`java -jar ips-backend.jar edu.harvard.seas.synthesis.SynthesisServer -s lib/`
5. If your default python is not python 3, please specify the path to python3 command as the runtime argument of `SynthesisServer`, e.g., `-p /usr/local/bin/python3`.
6. Launch the HTTP server. 
`java -jar ips-backend.jar edu.harvard.seas.synthesis.HTTPServer`
7. Open `http://localhost:8080` in your web browser.

**Note1:** Don't forget to add a backslash to escape a whitespace if your file path contains a whitespace.

## Install from Source Code

1. Clone this project. 
2. If you are a Mac user, please copy `lib/libz3java.dylib`, `lib/com.microsoft.z3.jar`, and `lib/libz3.dylib` to `/usr/local/lib`. 
3. Import the `back-end` folder into Eclipse as an existing Maven project ([instruction](https://vaadin.com/learn/tutorials/import-maven-project-eclipse)).
4. In Eclipse, add `-s lib/` as the runtime commandline argument of the `SynthesisServer` class ([Tutorial: How to add a commandline argument in Eclipse](https://www.codejava.net/ides/eclipse/how-to-pass-arguments-when-running-a-java-program-in-eclipse)).
5. If your default python is not python 3, please specify the path to python3 command as the runtime argument of `SynthesisServer`, e.g., `-p /usr/local/bin/python3`.
5. Run `SynthesisServer` to start the synthesis server.
6. Run `HTTPServer` to start the HTTP server.
5. Open `http://localhost:8080` in your web browser.

**Note1:** We use Eclipse for development, so the instructions above are based on Eclipse. You can also use other IDEs such as [IntelliJ](https://www.lagomframework.com/documentation/1.6.x/java/IntellijMaven.html). We recommend using a modern IDE since it is easier to run and debug.

**Note2:** If you want to build the project from a terminal, run `mvn package` to build and package the project. A jar of the back-end server is generated in the `target` folder. Then run the jar following Step 3-5 in the next section.

## Backend Server Usage
**Usage:** 
`java -jar ips-backend.jar -s <arg> [-n <arg>] [-t <arg>] [-h]`

**Options:**

-s,--synthesizer <arg>       (Required) specify the path for the program synthesis libraries

-p,--python <arg> 			 (Optional) specify the path to the python3 command 

-n,--example-num <arg>       (Optional) specify the number of input examples generated per cluster per example seed. The default value is 5.

-t,--timeout <arg>           (Optional) specify the timeout for the synthesis. The default value is 60 seconds.

-h,--help                    Print the help information.


## Troubleshooting
1. In Mac, you may see the following error.
```libz3java.dylib cannot be opened because it is from an unidentified developer.```
The underlying synthesizer in our tool depends on a theorem prover, [Z3](https://github.com/Z3Prover/z3), developed by Microsoft Research. Please grant the permission to this app by 1) open System Preferences, 2) click Security & Privacy, 3) click General, and 4) click "Open Anyway" next to the warning of this app.
2. Our synthesizer only works with Z3 4.8.9 or lower. The newer Z3 has changed their class signatures and are no longer compatible with our code. 
