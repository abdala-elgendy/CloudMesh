package com.shopme;

import java.io.*;
import java.net.*;

public class PeerServer {
    private static final int PEER_PORT = 6000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PEER_PORT)) {
            System.out.println("Peer Server started on port " + PEER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                String request = in.readLine();
                if (request == null) {
                    System.err.println("Client disconnected unexpectedly.");
                    return;
                }

                String[] tokens = request.split(" ");
                String command = tokens[0];
                String fileName = tokens[1];

                if (command.equals("DOWNLOAD")) {
                    File file = new File(fileName);
                    if (file.exists()) {
                        try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                            String line;
                            while ((line = fileReader.readLine()) != null) {
                                out.println(line);
                            }
                        }
                        System.out.println("File " + fileName + " sent successfully.");
                    } else {
                        System.err.println("File " + fileName + " not found.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling client request: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}