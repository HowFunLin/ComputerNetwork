import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;

public class PingServer extends Thread{
	private DatagramSocket socket;
	private DatagramPacket packet;
	private byte[] buf = new byte[1024];
	
	public PingServer(int port) {		
		System.out.println("----------PING����������----------");	
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("�����˿�" + port + "ʧ�ܣ�");
			System.exit(0);
		}
		
		//ʹ��ѭ��ʵ�ֲ��ϼ�������
		while(true) {
			packet = new DatagramPacket(buf,buf.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				System.out.println("��������쳣��");
				e.printStackTrace();
			}
			
			//���յ��ͻ��˴����ı��ļ������߳�
			PingServer p = new PingServer(socket, packet);
			p.start();
		}
	}

	//���췽�����ڴ������Ա��������߳�
	public PingServer(DatagramSocket socket, DatagramPacket packet)
	{
		this.packet = packet;
		this.socket = socket;
	}

	public void run() {	
			long randomTime = (long)(Math.random()*1200);
			
			//���շ��鲢ģ����鶪ʧ
			String data = null;
			if(randomTime>1000) {
				data = "������Ϣ��ʧ��\n";
				System.out.println(data);
			}else {
				data = new String(packet.getData());
				System.out.println("������Ϣ��" + data);
			}

			//����reply����
			byte[] buffer = data.getBytes();
			InetAddress host = packet.getAddress();
			int port = packet.getPort();
			DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,host,port);
			try {
				sleep(randomTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			try {
				socket.send(sendPacket);
			} catch (IOException e) {
				System.out.println("���������ͻظ���Ϣʧ�ܣ�");
				e.printStackTrace();
			}
		}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PingServer ping = new PingServer(Integer.valueOf(args[0]));
	}
}