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
			System.out.println("非法端口号！");
			System.exit(0);
		}
		this.port = port;
		try {
			client = new DatagramSocket();
			hostAddress = InetAddress.getByName(host);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println("非法主机名称！");
		}	
	}

	public void run() {
		String dateFormat = "yyyy-MM-dd HH:mm:ss.SS";
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);	
		System.out.println("Pinging " + hostAddress + ":");

		for(int i=0; i<10; i++) {

			Date sendTime = new Date(); //发送该消息的机器时间
			String outMessage = "请求报文" + i +"\n" + "PingUDP SequenceNumber:" + i + " TimeStamp:" + sdf.format(sendTime) + "\n";		

			//发送报文
			byte[] buffer = outMessage.getBytes();
			DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,hostAddress,port);
			
			//接收报文
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
			
			//等待时间不超过1s
			if(rtt[i]>1000) {
				receiveData = "\n请求报文"+ i + "的响应信息丢失或请求超时！";
			}else {
				receiveData = "\n" + receiveData + "折返时间：" + rtt[i] +"ms";
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
			
			System.out.println("\nPing主机" + hostAddress + "的结果为：");
			System.out.println("数据报：发送10， 接收" + replyNumber +"， 丢失" + (10-replyNumber));
			System.out.println("最小折返时间：" + minRtt + "ms\n最大折返时间：" + maxRtt + "ms\n平均折返时间：" + averRtt + "ms");
		}else {
			System.out.println("发送请求失败！无法返回信息！");
		}
	}
	
	public static void main(String[] args) {
		PingClient clientThread = new PingClient(args[0], Integer.valueOf(args[1]));
		clientThread.start();
	}
}