package com.shopme;

import java.io.*;
import java.net.*;

public class TempFileCreator {
    private static final String SERVER_IP = "127.0.0.1"; // Central Server IP
    private static final int SERVER_PORT = 5000; // Central Server Port

    public static void main(String[] args) {
        System.out.println("strat now ");
        try (Socket serverSocket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             PrintWriter out = new PrintWriter(serverSocket.getOutputStream(), true)) {

            System.out.println("Connected to Central Server.");

            // Register a file with the Central Server
            String fileName = "file1.txt";

            out.println("REGISTER " + InetAddress.getLocalHost().getHostAddress() + " " + fileName);
            System.out.println("Sent REGISTER request for file: " + fileName);
           //Thread.sleep(1000);
            // Wait for server response
            String response = in.readLine();
            if (response == null) {
              //  System.err.println("Error: Server did not respond.");
               // return;
                response = "no";
            }
    out.println("REGISTER file1.txt");
            System.out.println("Server response: " + response);

            // Query the Central Server for the file
            System.out.println("Sent QUERY request for file: " + fileName);
            out.println("QUERY " + InetAddress.getLocalHost().getHostAddress() + " " + fileName);
            response = in.readLine();


            // Wait for server response



            System.out.println("Server response: " + response);

        } catch (IOException e) {
            System.err.println("Error communicating with the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}