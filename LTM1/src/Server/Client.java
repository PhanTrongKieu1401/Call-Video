package Server;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Client {
    public static void main(String[] args) {
        try {
            // Khởi tạo biến cần thiết cho video
            DatagramSocket videoSocket = new DatagramSocket(8888);

            JFrame frame = new JFrame("Video Viewer");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(640, 480);
            JLabel label = new JLabel();
            frame.getContentPane().add(label);
            frame.setVisible(true);

            // Khởi tạo biến cần thiết cho âm thanh
            AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
            SourceDataLine speakers = AudioSystem.getSourceDataLine(audioFormat);
            speakers.open(audioFormat);
            speakers.start();

            DatagramSocket audioSocket = new DatagramSocket(5555);
            byte[] receiveAudioData = new byte[1024];

            // Tạo và khởi chạy luồng nhận video
            Thread videoThread = new Thread(() -> {
                try {
                    while (true) {
                        byte[] receiveVideoData = new byte[65536];
                        DatagramPacket receiveVideoPacket = new DatagramPacket(receiveVideoData, receiveVideoData.length);
                        videoSocket.receive(receiveVideoPacket);

                        ByteArrayInputStream videoStream = new ByteArrayInputStream(receiveVideoPacket.getData());
                        BufferedImage videoImage = ImageIO.read(videoStream);
                        label.setIcon(new ImageIcon(videoImage));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            videoThread.start();

            // Tạo và khởi chạy luồng nhận âm thanh
            Thread audioThread = new Thread(() -> {
                try {
                    while (true) {
                        DatagramPacket receiveAudioPacket = new DatagramPacket(receiveAudioData, receiveAudioData.length);
                        audioSocket.receive(receiveAudioPacket);

                        speakers.write(receiveAudioPacket.getData(), 0, receiveAudioPacket.getLength());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            audioThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
