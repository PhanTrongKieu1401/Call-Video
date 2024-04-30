package server;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import model.CongKetNoi;
import model.Send;

public class Server extends JFrame {
	public static List<CongKetNoi> congList = new ArrayList<>();
	private static JLabel videoLabel;
	
	public static boolean isJpegData(byte[] data) {
		try {
            // Sử dụng ByteArrayInputStream để đọc dữ liệu hình ảnh từ byte array
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            BufferedImage image = ImageIO.read(byteArrayInputStream);
            
            if (image == null) return false;

            // Nếu không có ngoại lệ, có nghĩa là dữ liệu là hình ảnh hợp lệ
            return true;
        } catch (IOException e) {
            // Nếu có ngoại lệ IOException, có thể coi dữ liệu không phải là hình ảnh
            return false;
        }
	}
	
	public static void main(String[] args) {
		try {
			DatagramSocket informationSocket = new DatagramSocket(1111);
			DatagramSocket videoSocket = new DatagramSocket(9999);
			DatagramSocket audioSocket = new DatagramSocket(7777);
			DatagramSocket shareVideoSocket = new DatagramSocket(5555);		
			
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
                    ArrayList<Send> a = new ArrayList<>();

                    for (int i = 0; i < 5; i++) {
                        Send send = new Send();
                        a.add(send);
                    }

                    while (true) {
                        DatagramPacket receiveVideoPacket = new DatagramPacket(receiveVideoData,
                                receiveVideoData.length);
                        videoSocket.receive(receiveVideoPacket);

                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
                                receiveVideoPacket.getData());
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        Send send = (Send) objectInputStream.readObject();
                        a.set(send.getId()-1, send);

                        int dem = 0;

                        for (int i = 0; i < congList.size(); i++) {
                            if (a.get(i).getA() != null) {
                                dem++;
                            }
                        }

                        if (dem == congList.size()) {
                            for (CongKetNoi x : congList) {
                                for (int i = 0; i < congList.size(); i++) {
                                    ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
                                    ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

                                    objectStream1.writeObject(a.get(i));
                                    objectStream1.flush();
                                    byte[] data1 = byteStream1.toByteArray();

                                    DatagramPacket videoPacket = new DatagramPacket(data1, data1.length,
                                            InetAddress.getByName(x.getDiachi()), x.getReceiveVideoSocket());
                                    videoSocket.send(videoPacket);
                                }
                            }

                            for (int i = 0; i < 5; i++) {
                                Send send1 = new Send();
                                a.set(i, send1);
                            }

                            dem = 0;
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
			
			Thread receiveShareVideoThread = new Thread(() -> {
			    try {
			        while (true) {
			        	// Nhận độ dài dữ liệu
			        	byte[] lengthBuffer = new byte[10];
			        	DatagramPacket lengthReceivePacket = new DatagramPacket(lengthBuffer, lengthBuffer.length);
			        	shareVideoSocket.receive(lengthReceivePacket);
			        	
			        	String length = new String(lengthReceivePacket.getData()).trim();
			        	System.out.println(length);
			        	
			        	if (length.matches("\\d+")) {
			        		int totalLength = Integer.parseInt(length);
				        	int chunkSize = 60000; // Kích thước mỗi phần nhỏ
				        	int totalChunks = (int) Math.ceil((double) totalLength / chunkSize);
				        	
				        	byte[] lengthData = String.valueOf(totalLength).getBytes();
	
				        	// Nhận và ghép nối các phần nhỏ
				        	if (totalChunks > 0) {
					        	for (CongKetNoi x : congList) {
				                	DatagramPacket lengthPacket = new DatagramPacket(lengthData, lengthData.length,
				                            InetAddress.getByName(x.getDiachi()), x.getShareVideo());
				                	shareVideoSocket.send(lengthPacket);
					        	}
					        	
				        		for (int i = 0; i < totalChunks; i++) {
					        	    byte[] receiveBuffer = new byte[chunkSize];
					        	    DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
					        	    shareVideoSocket.receive(receivePacket);
						            
					                byte[] sendData = receivePacket.getData();
					                for (CongKetNoi x : congList) {					                	
					                    DatagramPacket screenPacket = new DatagramPacket(sendData, sendData.length,
					                            InetAddress.getByName(x.getDiachi()), x.getShareVideo());
					                    shareVideoSocket.send(screenPacket);
						            }
					        	}	        	
				        	}				        	
			        	} else {    
			        		byte[] empty = "stop".getBytes();
			                for (CongKetNoi x : congList) {					                	
			                	DatagramPacket emptyPacket = new DatagramPacket(empty, empty.length,InetAddress.getByName(x.getDiachi()), x.getShareVideo());
				                shareVideoSocket.send(emptyPacket);
				            }			        				                
			        	}
		        	}			        	
			        
			    } catch (Exception e) {
			        e.printStackTrace();
			    }
			});

			receiveVideoThread.start();
			receiveInformationThread.start();
			receiveAudioThread.start();
			receiveShareVideoThread.start();
			receiveVideoThread.join();
			receiveAudioThread.join();
			receiveInformationThread.join();
			receiveShareVideoThread.join();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}





