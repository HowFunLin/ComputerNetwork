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
		System.out.println("----------PING服务器启动----------");	
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.out.println("监听端口" + port + "失败！");
			System.exit(0);
		}
		
		//使用循环实现不断监听报文
		while(true) {
			packet = new DatagramPacket(buf,buf.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				System.out.println("请求接收异常！");
				e.printStackTrace();
			}
			
			//接收到客户端传来的报文即启动线程
			PingServer p = new PingServer(socket, packet);
			p.start();
		}
	}

	//构造方法用于传引用以便于启动线程
	public PingServer(DatagramSocket socket, DatagramPacket packet)
	{
		this.packet = packet;
		this.socket = socket;
	}

	public void run() {	
			long randomTime = (long)(Math.random()*1200);
			
			//接收分组并模拟分组丢失
			String data = null;
			if(randomTime>1000) {
				data = "接收信息丢失！\n";
				System.out.println(data);
			}else {
				data = new String(packet.getData());
				System.out.println("接收信息：" + data);
			}

			//发送reply报文
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
				System.out.println("服务器发送回复信息失败！");
				e.printStackTrace();
			}
		}

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		PingServer ping = new PingServer(Integer.valueOf(args[0]));
	}
}