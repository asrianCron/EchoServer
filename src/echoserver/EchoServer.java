/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author asrianCron
 */
public class EchoServer extends Thread {
    
    private static final String adminPass = "root";
    private String myName = "General";
    public Socket clientSocket;
    private BufferedReader in = null;
    private PrintWriter out = null;
    private boolean running;
    public List<EchoServer> echoList;
    private boolean exceptionCaught = false;
    private List<String> lineLog = new ArrayList<>();
    
    public EchoServer(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.running = true;
    }
    
    @Override
    public void run() {
        while (running) {
            
            try {
                System.out.format("%s CONNECTION ESTABLISHED%n", Utilitaries.getTime());
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    choiceMaker(inputLine);
                }
                this.running = false;
            } catch (IOException ex) {
                closeConnection();
                exceptionCaught = true;
            }
            if (!exceptionCaught) {
                closeConnection();
            }
        }
    }
    
    public void choiceMaker(String inputLine) {
        
        if (inputLine.toLowerCase().contains("identify")) {
            if (inputLine.contains(adminPass)) {
                printToThis(identify());
            } else {
                printToThis("What's the magic word?");
            }
        } else if (inputLine.matches("<name .*>")) {
            System.out.println("NAME TAG RECEIVED");
            myName = inputLine.substring(6, inputLine.length() - 1);
            printToThis("Name changed to : " + myName);
        } else if(inputLine.matches("<whisper .*>")){
            String[] choppedString = inputLine.split(" ");
            whisper(choppedString[1], choppedString[2]);
        }
        else{
            System.out.format("%s line received: %s , sending it back%n", Utilitaries.getTime(), inputLine);
            printToAll(inputLine);
            lineLog.add(Utilitaries.getTime() + inputLine);
        }
    }
    
    public static String identify() {
        return String.format("user directory = %s%n\"os arhitecture = \" = %s", System.getProperty("user.dir"), System.getProperty("os.arch"));
    }
    
    public void closeConnection() {
        try {
            out.close();
            in.close();
            clientSocket.close();
            System.out.println("job done!");
            this.running = false;
            removeServer();
        } catch (IOException ex1) {
            this.running = false;
            removeServer();
        }
    }
    
    public static void writeToFile(List<String> list) {
        Path path = Paths.get("/input.txt");
        if (Files.notExists(path, LinkOption.NOFOLLOW_LINKS)) {
            try {
                Files.createFile(path);
                System.out.println("FILE CRATED at " + path);
            } catch (IOException ex) {
                Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            Files.write(path, list, StandardOpenOption.APPEND);
            System.out.println("INPUT SAVED");
        } catch (IOException ex) {
            Logger.getLogger(EchoServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void removeServer() {
        echoList.remove(this);
    }
    
    public void setServers(List<EchoServer> echoList) {
        this.echoList = echoList;
    }
    
    public void printToAll(String msg) {
        for (EchoServer server : echoList) {
            if (!server.equals(this)) {
                server.printToThis(myName + ": " + msg);
            }
        }
    }

    public void whisper(String name, String msg) {
        for (EchoServer server : echoList) {
            if (server.getName().equals(name)) {
                server.printToThis(myName + " whispered: " + msg);
            }
        }
    }

    public void printToThis(String msg) {
        out.println(msg);
    }
    
    public void setMyName(String name) {
        this.myName = name;
    }
    
    public String getMyName() {
        return this.myName;
    }
}
