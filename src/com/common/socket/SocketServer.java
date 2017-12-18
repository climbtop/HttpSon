package com.common.socket;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SocketServer implements Runnable{

    private InetAddress         hostAddress;
    private int                 port;
	private ServerSocketChannel serverChannel;
	private Selector            selector;
	private ByteBuffer          readBuffer;
	private boolean             enable;
	private Map<SocketChannel,List<ByteBuffer>> pendingData;
	private SocketWorker        socketWorker;
	private boolean				logger;
	
    public SocketServer(InetAddress hostAddress, int port) throws Exception{
        this.hostAddress = hostAddress;
        this.port = port;
        this.readBuffer     = ByteBuffer.allocate(8192);
        this.selector = initSelector();
        this.enable = true;
        this.pendingData = new HashMap<SocketChannel,List<ByteBuffer>>();
        this.logger = false;
    }
    

	public SocketWorker getSocketWorker() {
		return socketWorker;
	}

	public void setSocketWorker(SocketWorker socketWorker) {
		this.socketWorker = socketWorker;
	}

	private Selector initSelector() throws Exception {
        Selector socketSelector = SelectorProvider.provider().openSelector();
        
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        InetSocketAddress isa = new InetSocketAddress(hostAddress, port);
        serverChannel.socket().bind(isa);

        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
        
        return socketSelector;
    }
	
	
    private void accept(SelectionKey key) throws Exception {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        socketChannel.register(selector, SelectionKey.OP_READ);
    }
	
    private void read(SelectionKey key) throws Exception {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        readBuffer.clear();

        int numRead;
        try {
            numRead = socketChannel.read(readBuffer);
        } catch (Exception e) {
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
            key.channel().close();
            key.cancel();
            return;
        }
        
        byte[] dataCopy = new byte[numRead];
        System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);
        
        if(getSocketWorker()!=null){
        	byte[] dataText= getSocketWorker().processData(dataCopy);
        	peddingSend(key, socketChannel, dataText);
        }
    }
    
    private void peddingSend(SelectionKey key, SocketChannel socketChannel, byte[] dataText) {
    	synchronized (pendingData) {
    		List<ByteBuffer> queue = pendingData.get(socketChannel);
            if (queue == null) {
                queue = new LinkedList<ByteBuffer>();
                pendingData.put(socketChannel, queue);
            }
            if(dataText!=null){
            	queue.add(ByteBuffer.wrap(dataText));
            }
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }
    
	private void write(SelectionKey key) throws Exception {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		synchronized (pendingData) {
            List<ByteBuffer> queue = pendingData.get(socketChannel);
            
			while (!queue.isEmpty()) {
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					break;
				}
				queue.remove(0);
			}

			if (queue.isEmpty()) {
				key.interestOps(SelectionKey.OP_READ);
			}
		
		}
	}
    
    public void run() {
        while (enable) {
            try {
                selector.select();

                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        this.accept(key);
                    } else if (key.isReadable()) {
                        this.read(key);
                    } else if (key.isWritable()) {
                        this.write(key);
                    }
                }
            } catch (Exception e) {
        		if(isLogger()){
        			e.printStackTrace();
        		}
            }
        }
    }
    
    public void destory(Thread t){
    	try{
	    	this.enable = false;
	    	try{this.serverChannel.close();}catch(Exception e){}
	    	try{this.selector.close();}catch(Exception e){}
	    	try{this.readBuffer.clear();}catch(Exception e){}
	    	t.interrupt();
    	}catch(Exception e){
    		if(isLogger()){
    			e.printStackTrace();
    		}
    	}
    }


	public boolean isLogger() {
		return logger;
	}


	public void setLogger(boolean logger) {
		this.logger = logger;
	}
    
}
