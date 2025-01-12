package com.shopme;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CentralServer {
    private static Map<String, List<String>> fileToPeersMap = new HashMap<>();
    private static Map<String, Long> peerLastSeen = new HashMap<>();
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 5000;
    private static final int THREAD_POOL_SIZE = 10;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Central Server started on port " + SERVER_PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("connected "+ clientSocket.getPort());
              threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Central Server stopped");
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            System.out.println(clientSocket.getInetAddress() + " " + clientSocket.getPort());
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                // out.println("hello");
                checkPeerStatus();
               while(true){ String request = in.readLine();
                if (request == null) {
break;
                }
                String[] tokens = request.split(" ");

                String command = tokens[0];
                   System.out.println(tokens.length + " " + command);
                   if(command=="close") {
                       System.out.println("clossing the thread");
                       break;
                   }
                String peerIP = tokens[1];

                switch (command) {
                    case "REGISTER":
                        String fileName = tokens[2];
                        registerFile(peerIP, fileName);
                        out.println(fileName + " has been registered");
                        break;
                    case "QUERY":
                        String queryFileName = tokens[2];
                        List<String> peers = getPeersWithFile(queryFileName);
                        out.println(String.join(",", peers));
                        break;
                    case "KEEPALIVE":
                        updatePeerLastSeen(peerIP);
                        break;
                    default:
                        System.err.println("Unknown command: " + command);
                        out.println("Error: Unknown command.");
                }

            }
            } catch (IOException e) {
                System.out.println("error handling client request");
                e.printStackTrace();
            }

                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }

        }


        private void registerFile(String peerIP, String fileName) {
            fileToPeersMap.computeIfAbsent(fileName, k -> new ArrayList<>()).add(peerIP);
            peerLastSeen.put(peerIP, System.currentTimeMillis());
        }

        private List<String> getPeersWithFile(String fileName) {
            return fileToPeersMap.getOrDefault(fileName, new ArrayList<>());
        }

        private void updatePeerLastSeen(String peerIP) {
            peerLastSeen.put(peerIP, System.currentTimeMillis());
        }
    }

    public static void checkPeerStatus() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (Map.Entry<String, Long> entry : peerLastSeen.entrySet()) {
                    if (currentTime - entry.getValue() > 60000) { // 60 seconds timeout
                        String peerIP = entry.getKey();
                        peerLastSeen.remove(peerIP);
                        for (List<String> peers : fileToPeersMap.values()) {
                            peers.remove(peerIP);
                        }
                    }
                }
            }
        }, 0, 300000); // Check every 300 seconds
    }
}