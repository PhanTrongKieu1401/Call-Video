package server;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class SeverTong {
    private static final int MAX_DEVICES = 10; // Số lượng tối đa các thiết bị có thể kết nối
    private static List<DeviceInfo> videoSocketList = new ArrayList<>();
    private static List<DeviceInfo> audioSocketList = new ArrayList<>();
    private static DatagramSocket serverSocket;

    public static void main(String[] args) {
        try {
            serverSocket = new DatagramSocket(9876);
            System.out.println("Server is waiting for connections...");
            
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        }
    }

    private static int findConnectedDeviceIndex(InetAddress clientAddress, int clientPort) {
        for (int i = 0; i < videoSocketList.size(); i++) {
            if (videoSocketList.get(i).getClientAddress().equals(clientAddress) &&
            		videoSocketList.get(i).getClientPort() == clientPort) {
                return i; // Trả về chỉ số của thiết bị nếu đã kết nối
            }
        }
        return -1; // Trả về -1 nếu thiết bị chưa kết nối
    }
}
