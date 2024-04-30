package Server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Client1 {

    public static void main(String[] args) {
        // Định dạng âm thanh bạn muốn sử dụng
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
        SourceDataLine speakers;

        try {
            // Mô tả thông tin về dòng âm thanh đầu ra (loa)
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // Lấy dòng âm thanh đầu ra (loa) với định dạng đã chọn
            speakers = (SourceDataLine) AudioSystem.getLine(info);
            speakers.open(format);
            speakers.start();

            // Địa chỉ và cổng của máy chủ
            String hostname = "localhost";
            int port = 5555;

            // Tạo DatagramSocket để nhận dữ liệu âm thanh từ máy chủ
            DatagramSocket serverSocket = new DatagramSocket(5555);
            byte[] receiveData = new byte[1024];

            // Vòng lặp vô hạn để liên tục nhận và phát lại dữ liệu âm thanh từ máy chủ
            while (true) {
                // Nhận gói dữ liệu âm thanh từ máy chủ
                DatagramPacket response = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(response);

                // Phát lại dữ liệu qua loa
                speakers.write(response.getData(), 0, response.getLength());
            }

        } catch (LineUnavailableException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
