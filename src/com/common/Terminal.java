package com.common;

import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class Terminal {

	private DatagramSocket socket;
	private PacketReceiver packetReceiver;
	private Thread receiverThread;
	private int localport;
	private final String localhost = "127.0.0.1";
	private final String charSet = "UTF-8";
	private final int packetLength = 2048;

	public Terminal() {
		this.localport = getLocalPort();
	}

	protected void openSocket() {
		try {
			socket = new DatagramSocket(localport);
			socket.setReuseAddress(true);
			packetReceiver = new PacketReceiver(socket);
			receiverThread = new Thread(packetReceiver);
			receiverThread.setDaemon(true);
			receiverThread.start();
		} catch (SocketException e) {
		}
	}

	protected void closeSocket() {
		if (socket != null) {
			socket.close();
			socket = null;
		}
		if (packetReceiver != null) {
			packetReceiver.stop();
			packetReceiver = null;
		}
		if (receiverThread != null) {
			receiverThread.interrupt();
			receiverThread = null;
		}
	}

	protected String serviceHandle(String dataStr) throws Exception {
		String retData = null;
		String[] args = toDataArgs(dataStr);
		String name = getFirstArg(args);
		if (name.length() > 0) {
			Method m = this.getClass().getMethod(name, new Class[] { String[].class });
			if (m != null) {
				Object v = m.invoke(this, new Object[] { removeFirstArg(args) });
				if (v != null && v instanceof String) {
					retData = (String)v;
				} else if(v!=null) {
					retData = String.valueOf(v);
				}
			}
		}
		return retData;
	}

	protected void sendData(String dataStr) throws Exception {
		DatagramSocket socket = new DatagramSocket();
		socket.setReuseAddress(true);
		byte[] data = dataStr.getBytes(charSet);
		InetAddress targetAddr = InetAddress.getByName(localhost);
		DatagramPacket sendPkt = new DatagramPacket(data, data.length, targetAddr, localport);
		socket.send(sendPkt);
		socket.close();
	}

	protected void sendData(String dataStr, final int waitCompleted, final Callback callback) {
		Thread t = null;
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket();
			socket.setReuseAddress(true);
			byte[] data = dataStr.getBytes(charSet);
			InetAddress targetAddr = InetAddress.getByName(localhost);
			DatagramPacket sendPkt = new DatagramPacket(data, data.length, targetAddr, localport);
			socket.send(sendPkt);

			final DatagramSocket curSocket = socket;
			final Set<String> isCompleted = new HashSet<String>();
			Runnable r = new Runnable() {
				public void run() {
					DatagramPacket packet = new DatagramPacket(new byte[packetLength], packetLength);
					try {
						curSocket.setSoTimeout(waitCompleted);
						curSocket.receive(packet);
						byte[] resp = new byte[packet.getLength()];
						System.arraycopy(packet.getData(), 0, resp, 0, resp.length);
						String dataStr = new String(resp, charSet).trim();
						callback.handle(dataStr);
						isCompleted.add(dataStr);
					} catch (Exception e) {
						isCompleted.add(String.valueOf(e));
					}
				}
			};
			t = new Thread(r);
			t.start();

			long start = System.currentTimeMillis(), end;
			while (isCompleted.size()==0) {
				end = System.currentTimeMillis();
				if ((end - start) > 0 && (end - start) > waitCompleted) {
					break;
				}
				try {
					Thread.sleep(10);
					Thread.yield();
				} catch (InterruptedException e) {
					break;
				}
			}
		} catch (Exception e) {
		} finally {
			if( socket != null){
				socket.close();
			}
			if (t != null) {
				t.interrupt();
			}
		}
	}

	protected class PacketReceiver implements Runnable {
		boolean isRunning;
		DatagramPacket packet;
		DatagramSocket socket;

		public PacketReceiver(DatagramSocket socket) {
			this.socket = socket;
			this.packet = new DatagramPacket(new byte[packetLength], packetLength);
		}

		public void stop() {
			isRunning = false;
			if (socket != null) {
				socket.disconnect();
				socket.close();
				socket = null;
				packet = null;
			}
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void run() {
			if (isRunning)
				return;
			isRunning = true;
			try {
				while (isRunning && socket != null) {
					try {
						socket.setSoTimeout(15 * 1000);
						socket.receive(packet);
						byte[] data = new byte[packet.getLength()];
						System.arraycopy(packet.getData(), 0, data, 0, data.length);

						String dataStr = new String(data, charSet).trim();
						String address = packet.getAddress().getHostAddress();

						if (localhost.equals(address)) {
							responseHandle(packet, dataStr);
						}

					} catch (Exception e) {
					}
					try {
						Thread.sleep(10);
						Thread.yield();
					} catch (InterruptedException e) {
						break;
					}
				}
			} finally {
				isRunning = false;
			}
		}

		protected void responseHandle(DatagramPacket packet, String dataStr) throws Exception {
			String respStr = "";
			try {
				respStr = Terminal.this.serviceHandle(dataStr);
			} catch (Exception e) {
				respStr = String.valueOf(e);
			}
			byte[] respData = String.valueOf(respStr).getBytes(charSet);
			DatagramPacket sendPkt = new DatagramPacket(respData, respData.length, packet.getSocketAddress());
			socket.send(sendPkt);
		}
	}

	protected static interface Callback {
		void handle(String data);
	}

	protected int getLocalPort() {
		String path = this.getClass().getName();
		try{
			path = this.getClass().getCanonicalName();
		}catch(Throwable e){
		}
		try{
			URL url = this.getClass().getResource("/");
			if (url != null && url.getPath()!=null ) {
				path = url.getPath();
			}
		}catch(Throwable e){
		}
		int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		int low = 10000;
		int len = 60000 - low;
		int ret = Math.abs(result) % len + low;
		return ret;
	}

	protected boolean testTcpPort(int port) {
		boolean flag = false;
		try {
			InetAddress address = InetAddress.getByName(localhost);
			Socket socket = new Socket(address, port);
			socket.setReuseAddress(true);
			socket.close();
			flag = true;
		} catch (Exception e) {
		}
		return flag;
	}

	protected boolean testUdpPort(int port) {
		boolean flag = false;
		try {
			DatagramSocket socket = new DatagramSocket(port);
			socket.setReuseAddress(true);
			socket.close();
			flag = true;
		} catch (Exception e) {
		}
		return flag;
	}

	protected String toDataStr(String[] args) {
		StringBuffer sb = new StringBuffer();
		if (args != null && args.length > 0) {
			for (int i = 0; i < args.length; i++) {
				sb.append(sb.length() > 0 ? "\n" : "");
				sb.append(args[i]);
			}
		}
		return sb.toString();
	}

	protected String[] toDataArgs(String dataStr) {
		if (dataStr == null)
			return new String[0];
		String[] args = dataStr.split("\n");
		return args;
	}

	protected String[] removeFirstArg(String[] args) {
		if (args == null || args.length < 2) {
			return new String[0];
		}
		String[] result = new String[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			result[i - 1] = args[i];
		}
		return result;
	}

	protected String getFirstArg(String[] args) {
		if (args == null || args.length < 1) {
			return "";
		}
		return args[0];
	}
	
	protected boolean isValidArgs(String[] args) {
		return args!=null && args.length>0;
	}
	
	protected void setLocalPort(int port){
		this.localport = port;
	}
	
	protected void shutdown(int delay){
		final Timer timer = new Timer();
		timer.schedule(new TimerTask(){
			public void run(){
				Terminal.this.closeSocket();
				timer.cancel();
				System.exit(0);
			}
		}, delay);
	}
}
