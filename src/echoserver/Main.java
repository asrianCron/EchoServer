/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package echoserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author asrianCron
 */
public class Main {

    public static int rand(int bot, int top) {
        return (int) (bot + Math.round((top - bot) * Math.random()));
    }

    public static String randomNameReader(Path path) {
        List<String> nameList = null;
        if (Files.exists(path)) {
            System.out.println("Found names");
            try {
                nameList = new ArrayList<>();
                BufferedReader fileReader = new BufferedReader(new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ)));
                String str;
                while ((str = fileReader.readLine()) != null) {
                    nameList.add(str);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return nameList.get(rand(0, nameList.size() - 1));
        } else {
            System.out.println("Names not found");
            return "GenericName";
        }

    }

    public static void tooManyClients(boolean alreadyPrintedtooManyClients) {
        if (!alreadyPrintedtooManyClients) {
            System.out.println("TOO MANY CLIENTS");
        }
    }

    private static void initialiseServer(String[] args) {

        boolean running = true;
        boolean alreadyPrintedtooManyClients = false;
        int MAX = 5;
        ServerSocket serverSocket = null;
        List<EchoServer> echoList = new ArrayList<>();
        while (running) {
            try {
                if (echoList.size() > MAX) {
                    tooManyClients(alreadyPrintedtooManyClients);
                    alreadyPrintedtooManyClients = true;
                    Thread.sleep(500);
                } else {
                    serverSocket = new ServerSocket(Integer.parseInt(args[0]));
                    System.out.format("SOCKET %s%n", serverSocket.toString());
                    System.out.format("%s WAITING FOR CONNECTION%n", Utilitaries.getTime());
                    echoList.add(new EchoServer(serverSocket.accept()));
                    echoList.get(echoList.size() - 1).setServers(echoList);
                    echoList.get(echoList.size() - 1).start();
                    alreadyPrintedtooManyClients = false;
                }
            } catch (IOException ex) {
//                ex.printStackTrace();
            } catch (InterruptedException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        initialiseServer(args);
    }
}
