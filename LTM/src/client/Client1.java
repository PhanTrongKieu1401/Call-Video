package client;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

public class Client1 {
	public static void main(String[] args) {
		try {
			DatagramSocket informationSocket = new DatagramSocket();
			DatagramSocket videoSocket = new DatagramSocket();
			DatagramSocket audioSocket = new DatagramSocket();
			DatagramSocket receiveVideoSocket = new DatagramSocket();
			DatagramSocket receiveAudioSocket = new DatagramSocket();
			InetAddress localhost = InetAddress.getLocalHost();
			System.out.println(localhost.getHostAddress());
			CongKetNoi congKetNoi = new CongKetNoi(informationSocket.getLocalPort(), videoSocket.getLocalPort(),
					audioSocket.getLocalPort(), receiveVideoSocket.getLocalPort(), receiveAudioSocket.getLocalPort(), 1,
					"169.254.20.66");

			// Gửi đối tượng thông tin kết nối
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(congKetNoi);
			objectStream.flush();
			byte[] data = byteStream.toByteArray();
			int receiverPort = 1111;
			DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName("169.254.20.66"),
					receiverPort);
			informationSocket.send(packet);

			// Nhận đối tượng thông tin kết nối từ máy chủ
			byte[] receiveData = new byte[1024];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			informationSocket.receive(receivePacket);

			// Kiểm tra xem có dữ liệu gửi đến hay không
			if (receivePacket.getLength() > 0) {
				// Chuyển mảng byte thành đối tượng
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receivePacket.getData());
				ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
				CongKetNoi congKetNoi1 = (CongKetNoi) objectInputStream.readObject();
				congKetNoi.setStt(congKetNoi1.getStt());
			}

			// Tạo microphone để thu âm
			AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true);
			final TargetDataLine microphone = AudioSystem.getTargetDataLine(audioFormat);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
			microphone.open(audioFormat);

			int chunkSize = 1024;
			byte[] audioData = new byte[microphone.getBufferSize() / 5];
			microphone.start();

//        Tạo loa để phát lại âm thanh
			SourceDataLine speakers = AudioSystem.getSourceDataLine(audioFormat);
			speakers.open(audioFormat);
			speakers.start();

			System.out.println(congKetNoi.getStt());

			// Tạo frame hiển thị video
			JFrame frame = new JFrame("Video Viewer");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));
			frame.setSize(640, 480);
			JLabel label = new JLabel("1");
			frame.getContentPane().add(label);

			JLabel label2 = new JLabel("1");
			frame.getContentPane().add(label2);
			JLabel label3 = new JLabel("1");
			frame.getContentPane().add(label3);
			frame.setVisible(true);

			// Sử dụng JavaCV để lấy video từ webcam
			OpenCVFrameGrabber videoGrabber = new OpenCVFrameGrabber(0);
			videoGrabber.start();
			Java2DFrameConverter videoConverter = new Java2DFrameConverter();

			// ... (Phần khai báo và khởi tạo microphone)

			// Bắt đầu luồng xử lý video
			Thread videoThread = new Thread(() -> {
				try {
					while (true) {
						BufferedImage videoImage = videoConverter.convert(videoGrabber.grab());
						if (videoImage != null) {
							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(videoImage, "jpg", baos);
							byte[] videoImageData = baos.toByteArray();

							Send sendObject = new Send(congKetNoi.getStt(), videoImageData, 0);

							ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
							ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

							// Gửi đối tượng Send chứa ID và dữ liệu video
							objectStream1.writeObject(sendObject);
							objectStream1.flush();
							byte[] data1 = byteStream1.toByteArray();

							DatagramPacket videoPacket = new DatagramPacket(data1, data1.length,
									InetAddress.getByName("169.254.20.66"), 9999);
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
				    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				    while (true) {
				        numBytesRead = microphone.read(audioData, 0, chunkSize);
				        Send send = new Send(congKetNoi.getStt(), audioData, numBytesRead);
				        ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
						ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

						// Gửi đối tượng Send chứa ID và dữ liệu video
						objectStream1.writeObject(send);
						objectStream1.flush();
						byte[] data1 = byteStream1.toByteArray();

				        DatagramPacket audioPacket = new DatagramPacket(data1, data1.length,
				                    InetAddress.getByName("169.254.20.66"), 7777);
				            audioSocket.send(audioPacket);

				        }
				    
				} catch (Exception e) {
				    e.printStackTrace();
				}

			});

			Thread receiveVideoThread = new Thread(() -> {
				try {
					byte[] receiveVideoData = new byte[65536];

					while (true) {
						DatagramPacket receiveVideoPacket = new DatagramPacket(receiveVideoData,
								receiveVideoData.length);
						receiveVideoSocket.receive(receiveVideoPacket);

						if (receiveVideoPacket.getLength() > 0) {
							ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
									receiveVideoPacket.getData());
							ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
							Send send = (Send) objectInputStream.readObject();
							ByteArrayInputStream videoStream = new ByteArrayInputStream(send.getA());
							BufferedImage videoImage = ImageIO.read(videoStream);
							if (send.getId() == 1) {
								label.setIcon(new ImageIcon(videoImage));
							}
							if (send.getId() == 2) {
								label2.setIcon(new ImageIcon(videoImage));
							}
							if (send.getId() == 3) {
								label3.setIcon(new ImageIcon(videoImage));
							}
//							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			Thread receiveAudioThread = new Thread(() -> {
				try {
					byte[] receiveAudioData = new byte[65536];

					while (true) {
						DatagramPacket receiveAudioPacket = new DatagramPacket(receiveAudioData,
								receiveAudioData.length);
						receiveAudioSocket.receive(receiveAudioPacket);
			            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(receiveAudioPacket.getData());
			            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			            Send send = (Send) objectInputStream.readObject();
			            if(send.getId() != congKetNoi.getStt()) {
			            	speakers.write(send.getA(), 0, send.getSoketnoi());
			            }
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			// Bắt đầu luồng xử lý video
			videoThread.start();
			audioThread.start();
			receiveVideoThread.start();
			receiveAudioThread.start();
			// Đợi cho tất cả các luồng hoàn thành (khi chương trình kết thúc)
			videoThread.join();
			audioThread.join();
			receiveVideoThread.join();
			receiveAudioThread.join();
			// Đóng tài nguyên
			videoSocket.close();
			audioSocket.close();
			receiveVideoSocket.close();
			receiveAudioSocket.close();
			videoGrabber.stop();
			// Đóng tài nguyên microphone
			microphone.close();
            speakers.close();
		}catch(

	Exception e)
	{
		e.printStackTrace();
	}
}}
