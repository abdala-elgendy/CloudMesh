package com.shopme;

import java.io.*;
import java.net.*;
import java.util.*;

public class Peer {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    private static final int PEER_PORT = 6000;

    public static void main(String[] args) {
        new Peer().start();

    }

    public void start() {
        try (Socket serverSocket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true)) {
            Scanner sc = new Scanner(System.in);
           while(true) {
               System.out.println("give me request do you wan't to make in server");
               System.out.println("1- REGISTER file into server");
               System.out.println("2- QUERY file into server");
               System.out.println("3-EXIT");
               int order=sc.nextInt();
               if(order==1) {
                   System.out.println("give me file name");
                   String fileName=sc.next();
                   if(fileName!=null){
                   out.println("REGISTER " + InetAddress.getLocalHost().getHostAddress() + " "+fileName);
                   String response = in.readLine();
                   System.out.println(response);
                   }
               }


               else if(order==2) {
                   System.out.println("give me file name");
                   String fileName=sc.nextLine();
                   if(fileName!=null){
                       System.out.println("sending "+fileName);
                       out.println("QUERY " + InetAddress.getLocalHost().getHostAddress() + " "+fileName);
                       String response = in.readLine();
                       List<String> peers = Arrays.asList(response.split(" "));
                       if (!peers.isEmpty()) {
                           String peerIP = peers.get(0);
                           downloadFile(peerIP, "file1.txt");
                       }
                   }

               }
             else if(order==3)break;
            else {
                   System.out.println("give me correct input ");
               }

            // Keep alive
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try (Socket keepAliveSocket = new Socket(SERVER_IP, SERVER_PORT);
                         PrintWriter keepAliveOut = new PrintWriter(keepAliveSocket.getOutputStream(), true)) {
                        keepAliveOut.println("KEEPALIVE " + InetAddress.getLocalHost().getHostAddress());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 0, 30000); // Send keep alive every 30 seconds
           }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(String peerIP, String fileName) {
        try (Socket peerSocket = new Socket(peerIP, PEER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
             PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {

            out.println("DOWNLOAD " + fileName);
            String line;
            while ((line = in.readLine()) != null) {
                fileOutputStream.write(line.getBytes());
            }
        } catch (IOException e) {

            System.err.println("Error: Server did not respond.");
        }
    }
}