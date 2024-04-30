package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.bytedeco.flycapture.FlyCapture2_C.fc2Format7ImageSettings;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.librealsense.intrinsics;

import model.Send;
import model.CongKetNoi;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.Graphics2D;

import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.ScrollPaneConstants;

public class Room extends JFrame implements ActionListener{

	private JPanel contentPane;
	private static JPanel main_video;
	private JTextField txtRoomName;
	private static JLabel lblVideoShare;
	private JButton btnCam;
	private JButton btnVoice;
	private JButton btnShare;
	private JButton btnEnd;
	private JPanel footer, client_bar;
	private static List<JLabel> listVideoClient = new ArrayList<>();
	private static volatile boolean sharing = false;
	private static DatagramSocket shareVideo;
	private static DatagramSocket receiveShareVideo;
	private static String s = "26.102.64.65";
	private static volatile boolean captureVideo = true;
	private static volatile boolean isMicrophoneOn = true;
	private static Thread videoThread;
	private static Thread audioThread;
	private static TargetDataLine microphone;
	private static Java2DFrameConverter videoConverter;
	private static OpenCVFrameGrabber videoGrabber;
	private static CongKetNoi congKetNoi;
	private static DatagramSocket videoSocket;
	private static DatagramSocket audioSocket;
	private static byte[] audioData;
	private static int chunkSize = 1024;
	public static boolean isJpegData(byte[] data) {
		try {
            //Sử dụng ByteArrayInputStream để đọc dữ liệu hình ảnh từ byte array
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            BufferedImage image = ImageIO.read(byteArrayInputStream);
            
            if (image == null) return false;

            //Nếu không có ngoại lệ, có nghĩa là dữ liệu là hình ảnh hợp lệ
            return true;
        } catch (IOException e) {
            //Nếu có ngoại lệ IOException, có thể coi dữ liệu không phải là hình ảnh
            return false;
        }
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			// Tạo socket để gửi và nhận video, âm thanh
			DatagramSocket informationSocket = new DatagramSocket();
			videoSocket = new DatagramSocket();
			audioSocket = new DatagramSocket();
			DatagramSocket receiveVideoSocket = new DatagramSocket();
			DatagramSocket receiveAudioSocket = new DatagramSocket();
			shareVideo = new DatagramSocket();
			receiveShareVideo = new DatagramSocket();
			InetAddress localhost = InetAddress.getLocalHost();
//			CongKetNoi congKetNoi = new CongKetNoi(informationSocket.getLocalPort(), videoSocket.getLocalPort(),
//					audioSocket.getLocalPort(), receiveVideoSocket.getLocalPort(), receiveAudioSocket.getLocalPort(), 1,
//					localhost.getHostAddress(), receiveShareVideo.getLocalPort());
			congKetNoi = new CongKetNoi(informationSocket.getLocalPort(), videoSocket.getLocalPort(),
					audioSocket.getLocalPort(), receiveVideoSocket.getLocalPort(), receiveAudioSocket.getLocalPort(), 1,
					s, receiveShareVideo.getLocalPort());
			
			System.out.println("Client is joining...");

			// Sử dụng JavaCV để lấy video từ webcam
			videoGrabber = new OpenCVFrameGrabber(0);
			videoGrabber.start();
			// Chuyển đổi frame thành ảnh BufferedImage
			videoConverter = new Java2DFrameConverter();

			// Gửi đối tượng thông tin kết nối
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
			objectStream.writeObject(congKetNoi);
			objectStream.flush();
			byte[] data = byteStream.toByteArray();
			int receiverPort = 1111;
			DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(s),
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
			microphone = AudioSystem.getTargetDataLine(audioFormat);
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
			microphone.open(audioFormat);

			audioData = new byte[microphone.getBufferSize() / 5];
			microphone.start();

			Room frame = new Room();
			frame.setVisible(true);

			//Tạo loa để phát lại âm thanh
			SourceDataLine speakers = AudioSystem.getSourceDataLine(audioFormat);
			speakers.open(audioFormat);
			speakers.start();

			//Tạo luồng xử lý riêng cho video
			videoThread = new Thread(() -> {
				try {
					while (captureVideo) {
						
						BufferedImage videoImage = videoImage = videoConverter.convert(videoGrabber.grab());
						if (videoImage != null) {
		                    //Giảm kích thước ảnh
		                    int newWidth = 200; 
		                    int newHeight = 200; 
		                    BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		                    Graphics2D g = scaledImage.createGraphics();
		                    g.drawImage(videoImage, 0, 0, newWidth, newHeight, null);
		                    g.dispose();

							ByteArrayOutputStream baos = new ByteArrayOutputStream();
							ImageIO.write(scaledImage, "jpg", baos);
							
							listVideoClient.get(congKetNoi.getStt()-1).setIcon(new ImageIcon(scaledImage));
							
							byte[] videoImageData = baos.toByteArray();
							
							Send sendObject = new Send(congKetNoi.getStt(), videoImageData, 0);

							ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
							ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

							// Gửi đối tượng Send chứa ID và dữ liệu video
							objectStream1.writeObject(sendObject);
							objectStream1.flush();
							byte[] data1 = byteStream1.toByteArray();

							DatagramPacket videoPacket = new DatagramPacket(data1, data1.length,
									InetAddress.getByName(s), 9999);
							videoSocket.send(videoPacket);
							
							Thread.sleep(50);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			//Tạo luồng xử lý riêng cho âm thanh
			audioThread = new Thread(() -> {
				try {
				    int numBytesRead;
				    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

				    while (isMicrophoneOn) {

						numBytesRead = microphone.read(audioData, 0, chunkSize);
			           
				        Send send = new Send(congKetNoi.getStt(), audioData, numBytesRead);
				        ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
						ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

						// Gửi đối tượng Send chứa ID và dữ liệu video
						objectStream1.writeObject(send);
						objectStream1.flush();
						byte[] data1 = byteStream1.toByteArray();

				        DatagramPacket audioPacket = new DatagramPacket(data1, data1.length, InetAddress.getByName(s), 7777);
			            audioSocket.send(audioPacket);
//			            Thread.sleep(5);
				        }
				    	
				} catch (Exception e) {
				    e.printStackTrace();
				}
				
			});

			//Tạo luồng nhận video
			Thread receiveVideoThread = new Thread(() -> {
				try {
					byte[] receiveVideoData = new byte[65536];

//					System.out.println("client port: " + receiveVideoSocket.getLocalPort());
					while (true) {
						DatagramPacket receiveVideoPacket = new DatagramPacket(receiveVideoData,
								receiveVideoData.length);
						receiveVideoSocket.receive(receiveVideoPacket);
//						System.out.println("client port after received data: " + receiveVideoSocket.getLocalPort());

						if (receiveVideoPacket.getLength() > 0) {
							ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
									receiveVideoPacket.getData());
							ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
							Send send = (Send) objectInputStream.readObject();
							System.out.println(send.getSoketnoi());
							ByteArrayInputStream videoStream = new ByteArrayInputStream(send.getA());
							BufferedImage videoImage = ImageIO.read(videoStream);
							if(send.getId() != congKetNoi.getStt())
								listVideoClient.get(send.getId()-1).setIcon(new ImageIcon(videoImage));

						}
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});			
			
			//Luồng nhận video share màn hình
			Thread receiveScreenThread = new Thread(() -> {
			    try {
			        BufferedImage receivedImage;
			    	
			    	while(true) {
			    		// Nhận độ dài dữ liệu
			        	byte[] lengthBuffer = new byte[10];
			        	DatagramPacket lengthReceivePacket = new DatagramPacket(lengthBuffer, lengthBuffer.length);
			        	receiveShareVideo.receive(lengthReceivePacket);
			        	
			        	String length = new String(lengthReceivePacket.getData()).trim();
			        	System.out.println(length);
			        	
			        	if (length.matches("\\d+")) {
			        		int totalLength = Integer.parseInt(length);
				        	int chunk = 60000; // Kích thước mỗi phần nhỏ
				        	int total = (int) Math.ceil((double) totalLength / chunk);
	
				        	// Nhận và ghép nối các phần nhỏ
				        	if (total > 0) {
//				        		lblVideoShare.setVisible(true);
				        		ByteArrayOutputStream receivedData = new ByteArrayOutputStream();
//					        	System.out.println("total: " + total);

				        		for (int i = 0; i < total; i++) {
					        	    byte[] receiveBuffer = new byte[chunk];
					        	    DatagramPacket receiveScreenPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
					        	    receiveShareVideo.receive(receiveScreenPacket);

					        	    receivedData.write(receiveScreenPacket.getData(), 0, receiveScreenPacket.getLength());
					        	}
					        	// Chuyển đổi dữ liệu nhận được thành ảnh và cập nhật giao diện
					        	byte[] receivedImageData = receivedData.toByteArray();
					        	if (isJpegData(receivedImageData)) {
					        		ByteArrayInputStream receivedImageStream = new ByteArrayInputStream(receivedImageData);
						        	receivedImage = ImageIO.read(receivedImageStream);

						        	// Cập nhật giao diện với ảnh nhận được
						        	updateScreen(new ImageIcon(receivedImage).getImage());
					        	}				        	
				        	}				        	
			        	} else {	   
			        		System.out.println("Khong nhan duoc data");
			        		lblVideoShare.setVisible(false);
			        	}
			        }
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
			});

			// Tạo luồng nhận âm thanh
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
//			            if(send.getId() != congKetNoi.getStt()) {
			            	speakers.write(send.getA(), 0, send.getSoketnoi());
//			            }
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			// Bắt đầu tất cả các luồng xử lý
			videoThread.start();
			audioThread.start();
			receiveVideoThread.start();
			receiveAudioThread.start();
			receiveScreenThread.start();

			// Đợi cho tất cả các luồng hoàn thành (khi chương trình kết thúc)
			videoThread.join();
			audioThread.join();
			receiveVideoThread.join();
			receiveAudioThread.join();
			receiveScreenThread.join();

			// Đóng tài nguyên
			videoSocket.close();
			audioSocket.close();
			receiveVideoSocket.close();
			receiveAudioSocket.close();
			shareVideo.close();
			receiveVideoSocket.close();
			videoGrabber.stop();
			microphone.close();
			speakers.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	

	private static void startSharing() throws IOException {
        sharing = true;
        
        System.out.println("Bắt đầu share màn hình");
        
        // Tạo một thread để liên tục cập nhật hiển thị màn hình
        Thread thread = new Thread(() -> {
            try {                
                while (sharing) {
                    // Chụp ảnh màn hình
                    BufferedImage screenCapture = captureScreen();

                    // Giảm kích thước ảnh
                    int newWidth = 800;  // Chiều rộng mới
                    int newHeight = 600; // Chiều cao mới
                    BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = scaledImage.createGraphics();
                    g.drawImage(screenCapture, 0, 0, newWidth, newHeight, null);
                    g.dispose();

                    // Convert ảnh sang ImageIcon và cập nhật hiển thị
                    Image image = new ImageIcon(scaledImage).getImage();
//                    updateScreen(image);

                    // Nén ảnh trước khi gửi
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(scaledImage, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();

                    // Gửi độ dài dữ liệu
                    int totalLength = imageBytes.length;
                    byte[] lengthBytes = String.valueOf(totalLength).getBytes();
                    DatagramPacket lengthPacket = new DatagramPacket(lengthBytes, lengthBytes.length, InetAddress.getByName(s), 5555);
                    shareVideo.send(lengthPacket);
                    
                    int chunkSize = 60000; // Kích thước mỗi phần nhỏ
                    int totalChunks = (int) Math.ceil((double) totalLength / chunkSize);
                    
                    for (int i = 0; i < totalChunks; i++) {
                        int offset = i * chunkSize;
                        int length = Math.min(chunkSize, imageBytes.length - offset);

                        byte[] chunk = Arrays.copyOfRange(imageBytes, offset, offset + length);

                        // Gửi phần nhỏ
                        DatagramPacket sendPacket = new DatagramPacket(chunk, chunk.length, InetAddress.getByName(s), 5555);
                        shareVideo.send(sendPacket);
//                        System.out.println("Data send: " + sendPacket.getLength());
                    }                  
                }   
                byte[] empty = "stop".getBytes();
                DatagramPacket emptyPacket = new DatagramPacket(empty, empty.length,InetAddress.getByName(s), 5555);
                shareVideo.send(emptyPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void stopSharing() throws IOException {
        sharing = false;
        System.out.println("Kết thúc share màn hình");
        // Clear the screen
        updateScreen(null);
    }

    private static BufferedImage captureScreen() {
        try {
            // Sử dụng AWT Robot để chụp ảnh màn hình
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            return robot.createScreenCapture(screenRect);
        } catch (AWTException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void updateScreen(Image image) {
        if (image != null) {
            // Nếu chưa có JLabel, tạo mới
            if (lblVideoShare == null) {
                lblVideoShare = new JLabel();
                main_video.add(new JScrollPane(lblVideoShare));
            }

            // Cập nhật hình ảnh của JLabel
            lblVideoShare.setIcon(new ImageIcon(image));
        } 

        // Yêu cầu panel vẽ lại
        main_video.revalidate();
        main_video.repaint();
    }

	/**
	 * Create the frame.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public Room() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1099, 668);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(1, 0, 0, 0));
		
		JPanel main = new JPanel();
		contentPane.add(main);
		main.setLayout(new BorderLayout(0, 0));
		
		JPanel header = new JPanel();
		main.add(header, BorderLayout.NORTH);
		header.setLayout(new GridLayout(1, 0, 0, 0));
		
		main_video = new JPanel(); //main_video
		main.add(main_video, BorderLayout.CENTER);
		main_video.setLayout(new BorderLayout(1, 1));
		
		lblVideoShare = new JLabel();
		lblVideoShare.setHorizontalAlignment(SwingConstants.CENTER);
		main_video.add(lblVideoShare);
		
		txtRoomName = new JTextField();
		txtRoomName.setText("Room Meet");
		txtRoomName.setHorizontalAlignment(SwingConstants.CENTER);
		txtRoomName.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtRoomName.setEnabled(false);
		header.add(txtRoomName);
		txtRoomName.setColumns(10);
		
		JPanel right = new JPanel();
		main.add(right, BorderLayout.EAST);
		right.setLayout(new GridLayout(4, 1, 0, 0));
		
		btnCam = new JButton("Camera");
		btnCam.addActionListener(this);
		right.add(btnCam);
		
		btnVoice = new JButton("Voice");
		btnVoice.addActionListener(this);
		right.add(btnVoice);
		
		btnShare = new JButton("Share");
		btnShare.addActionListener(e -> {
            if (!sharing) {
                try {
					startSharing();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            } else {
                try {
					stopSharing();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            }
        });
		right.add(btnShare);
		
		btnEnd = new JButton("End");
		right.add(btnEnd);
		
		footer = new JPanel();
		footer.setMinimumSize(new Dimension(1000, 200));
		footer.setMaximumSize(new Dimension(1000, 200));
		footer.setPreferredSize(new Dimension(1000, 200));
		main.add(footer, BorderLayout.SOUTH);
		footer.setLayout(new BorderLayout(0, 0));
		
		client_bar = new JPanel();
        for (int i = 1; i <= 3; i++) {
        	JLabel lblVideoClient = new JLabel();
        	lblVideoClient.setPreferredSize(new Dimension(200, 200));
        	client_bar.add(lblVideoClient);
        	listVideoClient.add(lblVideoClient);
        }
		JScrollPane scrollPane = new JScrollPane(client_bar);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBounds(50, 30, 300, 50);
        footer.add(scrollPane);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton btnChecked = (JButton) e.getSource();
		if(btnChecked.equals(btnCam)) {
			captureVideo = !captureVideo;
		    if (captureVideo) {
		        System.out.println("Bật camera");
		        videoThread = new Thread(() -> {
					try {
						while (captureVideo) {
							
							BufferedImage videoImage = videoImage = videoConverter.convert(videoGrabber.grab());
							if (videoImage != null) {
			                    //Giảm kích thước ảnh
			                    int newWidth = 200; 
			                    int newHeight = 200; 
			                    BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
			                    Graphics2D g = scaledImage.createGraphics();
			                    g.drawImage(videoImage, 0, 0, newWidth, newHeight, null);
			                    g.dispose();

								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								ImageIO.write(scaledImage, "jpg", baos);
								
								listVideoClient.get(congKetNoi.getStt()-1).setIcon(new ImageIcon(scaledImage));
								
								byte[] videoImageData = baos.toByteArray();
								
								Send sendObject = new Send(congKetNoi.getStt(), videoImageData, 0);

								ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
								ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

								// Gửi đối tượng Send chứa ID và dữ liệu video
								objectStream1.writeObject(sendObject);
								objectStream1.flush();
								byte[] data1 = byteStream1.toByteArray();

								DatagramPacket videoPacket = new DatagramPacket(data1, data1.length,
										InetAddress.getByName(s), 9999);
								videoSocket.send(videoPacket);
								
								Thread.sleep(50);
							}
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				});
		        videoThread.start();
		    } else {
		        System.out.println("Tắt camera");
		    }
		}
		else {
			if(btnChecked.equals(btnVoice)) {
				isMicrophoneOn = !isMicrophoneOn; // Chuyển đổi trạng thái của biến captureVideo
			    if (isMicrophoneOn) {
			        System.out.println("Bật mic");
			        audioThread = new Thread(() -> {
						try {
						    int numBytesRead;
						    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

						    while (isMicrophoneOn) {

								numBytesRead = microphone.read(audioData, 0, chunkSize);
					           
						        Send send = new Send(congKetNoi.getStt(), audioData, numBytesRead);
						        ByteArrayOutputStream byteStream1 = new ByteArrayOutputStream();
								ObjectOutputStream objectStream1 = new ObjectOutputStream(byteStream1);

								// Gửi đối tượng Send chứa ID và dữ liệu video
								objectStream1.writeObject(send);
								objectStream1.flush();
								byte[] data1 = byteStream1.toByteArray();

						        DatagramPacket audioPacket = new DatagramPacket(data1, data1.length, InetAddress.getByName(s), 7777);
					            audioSocket.send(audioPacket);
					            //Thread.sleep(5);
						        }
						    	
						} catch (Exception e1) {
						    e1.printStackTrace();
						}

					});
			        audioThread.start();
			    } else {
			        System.out.println("Tắt mic");
			    }
			}
			else {
				if(btnChecked.equals(btnShare)) {
					
				}
				else {
					if(btnChecked.equals(btnEnd)) {
						
					}
				}
			}
		}
	}
}
