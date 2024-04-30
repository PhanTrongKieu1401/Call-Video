package client;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class TEst
{
    public static void main(String[] args) {
        try {
            // Lấy tất cả các giao diện mạng (network interfaces) trên máy tính
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            // Duyệt qua từng giao diện mạng
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Lấy tất cả địa chỉ IP của giao diện mạng đó
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();

                // Duyệt qua từng địa chỉ IP
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // Kiểm tra xem địa chỉ IP có phải là địa chỉ IP trên VPN không (ví dụ: kiểm tra địa chỉ IP có định dạng nào đó)
                    if (isVPNAddress(inetAddress.getHostAddress())) {
                        System.out.println("VPN Address: " + inetAddress.getHostAddress());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Phương thức kiểm tra xem địa chỉ IP có phải là địa chỉ IP trên VPN không
    private static boolean isVPNAddress(String ipAddress) {
        // Thực hiện các kiểm tra phù hợp với địa chỉ IP trên VPN của bạn
        // Ví dụ: kiểm tra địa chỉ IP theo định dạng hoặc các điều kiện khác
        // Trong trường hợp đơn giản, bạn có thể chỉ kiểm tra định dạng của địa chỉ IP.
        // Định dạng có thể khác nhau tùy thuộc vào cấu hình của VPN.

        // Ở đây, một ví dụ đơn giản kiểm tra địa chỉ IP có bắt đầu bằng "192.168." hay không
        return ipAddress.startsWith("192.168.");
    }
}
