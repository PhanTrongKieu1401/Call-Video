package Client;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    public static void main(String[] args) {
        try {
            // Tạo socket để gửi video
            DatagramSocket videoSocket = new DatagramSocket();
            System.out.println("Video Server is waiting for client...");

            // Tạo socket để gửi âm thanh
            DatagramSocket audioSocket = new DatagramSocket();
            System.out.println("Audio Server is waiting for client...");

            // Sử dụng JavaCV để lấy video từ webcam
            OpenCVFrameGrabber videoGrabber = new OpenCVFrameGrabber(0);
            videoGrabber.start();

         // Tạo microphone để thu âm
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
            final TargetDataLine microphone = AudioSystem.getTargetDataLine(audioFormat);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            microphone.open(audioFormat);

            int chunkSize = 1024;
            byte[] audioData = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            // Chuyển đổi frame thành ảnh BufferedImage
            Java2DFrameConverter videoConverter = new Java2DFrameConverter();

            // Tạo luồng xử lý riêng cho video
            Thread videoThread = new Thread(() -> {
                try {
                    while (true) {
                        BufferedImage videoImage = videoConverter.convert(videoGrabber.grab());
                        if (videoImage != null) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(videoImage, "jpg", baos);
                            byte[] videoImageData = baos.toByteArray();

                            DatagramPacket videoPacket = new DatagramPacket(videoImageData, videoImageData.length,
                                    InetAddress.getByName("localhost"), 8888);
                            videoSocket.send(videoPacket);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Tạo luồng xử lý riêng cho âm thanh
            Thread audioThread = new Thread(() -> {
                try {
                    int numBytesRead;
                    while (true) {
                        numBytesRead = microphone.read(audioData, 0, chunkSize);
                        DatagramPacket audioPacket = new DatagramPacket(audioData, numBytesRead,
                                InetAddress.getByName("localhost"), 5555);
                        audioSocket.send(audioPacket);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Bắt đầu cả hai luồng xử lý
            videoThread.start();
            audioThread.start();

            // Đợi cả hai luồng xử lý hoàn thành (khi chương trình kết thúc)
            videoThread.join();
            audioThread.join();
            
            // Đóng tài nguyên
            videoSocket.close();
            audioSocket.close();
            videoGrabber.stop();
            microphone.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
