package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.sql.rowset.serial.SQLOutputImpl;

import client.CongKetNoi;
import client.Send;

public class Sever1 {
	public static List<CongKetNoi> congList = new ArrayList<>();

	public static void main(String[] args) {
		try {
			DatagramSocket informationSocket = new DatagramSocket(1111);
			DatagramSocket videoSocket = new DatagramSocket(9999);
			DatagramSocket audioSocket = new DatagramSocket(7777);
			System.out.println("Server is waiting for client...");
			AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
			SourceDataLine speakers = AudioSystem.getSourceDataLine(audioFormat);
			speakers.open(audioFormat);
			speakers.start();
			Thread receiveInformationThread = new Thread(() -> {
				try {
					byte[] receiveData = new byte[1024];

					while (true) {
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						informationSocket.receive(receivePacket);
						// Kiểm tra xem có dữ liệu gửi đến hay không
						if (receivePacket.getLength() > 0) {
							// Chuyển mảng byte thành đối tượng
							ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
									receivePacket.getData());
							ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
							CongKetNoi congKetNoi = (CongKetNoi) objectInputStream.readObject();
							int stt = congList.size() + 1;
							congKetNoi.setStt(stt);
							// Thêm vào danh sách nếu có dữ liệu
							congList.add(congKetNoi);
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
							objectStream.writeObject(congKetNoi);
							objectStream.flush();
							byte[] data = byteStream.toByteArray();
							// Gửi dữ liệu lại cho client thông qua cổng đã được client sử dụng
							InetAddress clientAddress = InetAddress.getByName(congKetNoi.getDiachi());
							DatagramPacket packet = new DatagramPacket(data, data.length,
									InetAddress.getByName(congKetNoi.getDiachi()), congKetNoi.getInformationSocket());
							informationSocket.send(packet);
						}

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			// Tạo luồng nhận video
			Thread receiveVideoThread = new Thread(() -> {
				try {
					byte[] receiveVideoData = new byte[65536];

					while (true) {
						DatagramPacket receiveVideoPacket = new DatagramPacket(receiveVideoData,
								receiveVideoData.length);
						videoSocket.receive(receiveVideoPacket);
						if (receiveVideoPacket.getLength() > 0) {
							for (CongKetNoi x : congList) {
									DatagramPacket videoPacket = new DatagramPacket(receiveVideoPacket.getData(), receiveVideoPacket.getLength(),
									InetAddress.getByName(x.getDiachi()), x.getReceiveVideoSocket());
									videoSocket.send(videoPacket);
							}

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			

			Thread receiveAudioThread = new Thread(() -> {
			    try {
			        byte[] receiveData = new byte[65536];
			        while (true) {
			            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			            audioSocket.receive(receivePacket);

			            for (CongKetNoi x : congList) {
			                DatagramPacket audioPacket = new DatagramPacket(receivePacket.getData(),
			                		receivePacket.getLength(), InetAddress.getByName(x.getDiachi()),
			                        x.getReceiveAudioSocket());
			                audioSocket.send(audioPacket);
			            }


			        }
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			});

			receiveVideoThread.start();
			receiveInformationThread.start();
			receiveAudioThread.start();
			receiveVideoThread.join();
			receiveAudioThread.join();
			receiveInformationThread.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
