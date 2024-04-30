package client;

import java.io.Serializable;

//Lớp đối tượng cần gửi
public class CongKetNoi implements Serializable {

	private int  videoSocket, audioSocket, receiveVideoSocket, receiveAudioSocket, stt, informationSocket;
	private String diachi;
	public int getVideoSocket() {
		return videoSocket;
	}
	
	
	public CongKetNoi(int informationSocket, int videoSocket, int audioSocket, int receiveVideoSocket, int receiveAudioSocket, int stt,
			String diachi) {
		super();
		this.informationSocket = informationSocket;
		this.videoSocket = videoSocket;
		this.audioSocket = audioSocket;
		this.receiveVideoSocket = receiveVideoSocket;
		this.receiveAudioSocket = receiveAudioSocket;
		this.stt = stt;
		this.diachi = diachi;
	}
	
	

	public int getInformationSocket() {
		return informationSocket;
	}


	public void setInformationSocket(int informationSocket) {
		this.informationSocket = informationSocket;
	}


	public void setVideoSocket(int videoSocket) {
		this.videoSocket = videoSocket;
	}
	public int getAudioSocket() {
		return audioSocket;
	}
	public void setAudioSocket(int audioSocket) {
		this.audioSocket = audioSocket;
	}
	public int getReceiveVideoSocket() {
		return receiveVideoSocket;
	}
	public void setReceiveVideoSocket(int receiveVideoSocket) {
		this.receiveVideoSocket = receiveVideoSocket;
	}
	public int getReceiveAudioSocket() {
		return receiveAudioSocket;
	}
	public void setReceiveAudioSocket(int receiveAudioSocket) {
		this.receiveAudioSocket = receiveAudioSocket;
	}
	public int getStt() {
		return stt;
	}
	public void setStt(int stt) {
		this.stt = stt;
	}
	public String getDiachi() {
		return diachi;
	}
	public void setDiachi(String diachi) {
		this.diachi = diachi;
	}
	
	@Override
    public String toString() {
        return "CongKetNoi{" +
                "informationSocket=" + informationSocket +
                ", videoSocket=" + videoSocket +
                ", audioSocket=" + audioSocket +
                ", receiveVideoSocket=" + receiveVideoSocket +
                ", receiveAudioSocket=" + receiveAudioSocket +
                ", stt=" + stt +
                ", diachi='" + diachi + '\'' +
                '}';
    }
	
}

