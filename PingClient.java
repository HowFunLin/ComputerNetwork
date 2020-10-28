import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PingClient extends Thread{
	private DatagramSocket client;
	private InetAddress hostAddress;
	private int port;
	private int replyNumber = 0;
	private long minRtt = 0, maxRtt = 0, averRtt = 0,sumRtt = 0;
	private long[] rtt = new long[10];
	
	public PingClient(String host, int port) {
		if(port < 0 || port > 65535) {
			System.out.println("�Ƿ��˿ںţ�");
			System.exit(0);
		}
		this.port = port;
		try {
			client = new DatagramSocket();
			hostAddress = InetAddress.getByName(host);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("�Ƿ��������ƣ�");
		}	
	}

	public void run() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss.SS";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);	
		System.out.println("Pinging " + hostAddress + ":");

		for(int i=0; i<10; i++) {

			Date sendTime = new Date(); //���͸���Ϣ�Ļ���ʱ��
			String outMessage = "������" + i +"\n" + "PingUDP SequenceNumber:" + i + " TimeStamp:" + sdf.format(sendTime) + "\n";		

			//���ͱ���
			byte[] buffer = outMessage.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,hostAddress,port);
			
			//���ձ���
			byte[] buf = new byte[buffer.length];		
			DatagramPacket receivePacket = new DatagramPacket(buf,buf.length);
			
			String receiveData = null;
			try {
				client.send(sendPacket);
				
				client.receive(receivePacket);
				receiveData = new String(receivePacket.getData());
				Date receiveTime = new Date();
				rtt[i] = receiveTime.getTime()-sendTime.getTime();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//�ȴ�ʱ�䲻����1s
			if(rtt[i]>1000) {
				receiveData = "\n������"+ i + "����Ӧ��Ϣ��ʧ������ʱ��";
			}else {
				receiveData = "\n" + receiveData + "�۷�ʱ�䣺" + rtt[i] +"ms";
			}
			System.out.println(receiveData);			
		}
		
		minRtt = rtt[0];
		for(int i=0;i<10;i++) {
			if(rtt[i] > 1000)  continue;	
			if(minRtt > rtt[i]) minRtt = rtt[i];
			if(maxRtt < rtt[i]) maxRtt = rtt[i];
			sumRtt += rtt[i];
			replyNumber++;
		}
		
		if(replyNumber!=0) {
			averRtt = sumRtt/replyNumber;
			
			System.out.println("\nPing����" + hostAddress + "�Ľ��Ϊ��");
			System.out.println("���ݱ�������10�� ����" + replyNumber +"�� ��ʧ" + (10-replyNumber));
			System.out.println("��С�۷�ʱ�䣺" + minRtt + "ms\n����۷�ʱ�䣺" + maxRtt + "ms\nƽ���۷�ʱ�䣺" + averRtt + "ms");
		}else {
			System.out.println("��������ʧ�ܣ��޷�������Ϣ��");
		}
	}
	
	public static void main(String[] args) {
		PingClient clientThread = new PingClient(args[0], Integer.valueOf(args[1]));
		clientThread.start();
	}
}