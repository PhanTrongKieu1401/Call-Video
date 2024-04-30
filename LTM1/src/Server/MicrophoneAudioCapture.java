package Server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sound.sampled.*;

public class MicrophoneAudioCapture {

    private TargetDataLine targetDataLine;
    private SourceDataLine sourceDataLine;

    public MicrophoneAudioCapture() {
        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);

        try {
            DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, format);
            targetDataLine = (TargetDataLine) AudioSystem.getLine(targetInfo);
            targetDataLine.open(format);

            DataLine.Info sourceInfo = new DataLine.Info(SourceDataLine.class, format);
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(sourceInfo);
            sourceDataLine.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        targetDataLine.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try (DatagramSocket socket = new DatagramSocket(2223)) {
                byte[] buffer = new byte[1024];

                while (true) {
                    System.out.println("Audio sent successfully!");
                    int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                    DatagramPacket packet = new DatagramPacket(buffer, bytesRead, InetAddress.getByName("localhost"), 1112);
                    socket.send(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        targetDataLine.stop();
        targetDataLine.close();
        sourceDataLine.stop();
        sourceDataLine.close();
    }
}
