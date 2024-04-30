package Client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Sever1 {

    public static void main(String[] args) throws IOException {
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
        TargetDataLine microphone;

        try {
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);

            int numBytesRead;
            int chunkSize = 1024;
            byte[] data = new byte[microphone.getBufferSize() / 5];
            microphone.start();

            // configure the IP and port
            String hostname = "localhost";
            int port = 5555;

            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();
            byte[] buffer;

            while (true) {
                numBytesRead = microphone.read(data, 0, chunkSize);

                // send the mic data to the server
                DatagramPacket request = new DatagramPacket(data, numBytesRead, address, port);
                socket.send(request);
            }

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
