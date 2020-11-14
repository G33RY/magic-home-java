package me.g33ry.magic_home_java;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.HashSet;

public class Discover {
    private final int DISCOVERY_PORT = 48899;
    private final String BROADCAST_ADDR = "255.255.255.255";
    private final byte[] DATA = "HF-A11ASSISTHREAD".getBytes();

    private boolean Scanned;
    private HashSet<Controller> Controllers;

    public Discover() {
        this.Controllers = new HashSet<>();
        this.Scanned = false;
    }

    public boolean isScanned() {
        return Scanned;
    }

    public HashSet<Controller> getControllers() {
        return Controllers;
    }

    public static Controller[] Scan() throws IOException {
        return new Discover().scan();
    }

    private Controller[] scan() throws IOException {

        DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT);
        socket.setBroadcast(true);
        socket.setSoTimeout(500);
        byte[] buf = new byte[512];
        HashSet<String> addresses = new HashSet<>();

//      GET EVERY INTERFACE ADDRESS
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while(interfaces.hasMoreElements()){
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

            while(inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                String[] address = inetAddress.getHostAddress().split("\\.");
                if(address.length != 4) continue;
                address[3] = "255";
                addresses.add(String.join(".", address));
            }
        }
        if(addresses.size() == 0) addresses.add(BROADCAST_ADDR);


//      SEND DATA TO EVERY INTERFACE
        for (String address : addresses){
            DatagramPacket packet = new DatagramPacket(DATA, DATA.length, InetAddress.getByName(address), DISCOVERY_PORT);
            socket.send(packet);
        }

        final long deadline = System.currentTimeMillis() + 500;
        while((deadline - System.currentTimeMillis()) > 0){
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            }catch (Exception e) {
                break;
            }

            String received = new String(packet.getData(), 0, packet.getLength());
            String[] tmpInfos = received.split(",");
            if(tmpInfos.length == 3) Controllers.add(new Controller(tmpInfos[0], tmpInfos[1], tmpInfos[2]));
        }

        socket.close();
        this.Scanned = true;

        return this.Controllers.toArray(Controller[]::new);
    }

}