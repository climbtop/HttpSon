package com.httpclient;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 弹出对话框
 */
public class ValidDialog2 extends JFrame {
	private static final long serialVersionUID = 1L;
	private String validcode = "";
	private boolean isCancel = false;
	JPanel p = new JPanel(new GridLayout(0, 1));
	
	public ValidDialog2(Object imageObj){
		final JTextField tfValidcode = new JTextField();
		
		tfValidcode.addKeyListener(new KeyListener() {  
		    public void keyPressed(KeyEvent e) {  
		        if (e.getKeyCode() == KeyEvent.VK_ENTER) {  
		            Component c = e.getComponent();
		            while(!(c instanceof JOptionPane)){
		            	c = c.getParent();
		            	if(c==null) return;
		            }
		            
		            JButton btn = getTargetButton(c);
		            if(btn==null) return;
		            
		            MouseEvent pevent = new MouseEvent(btn,MouseEvent.MOUSE_PRESSED,0,MouseEvent.BUTTON1_MASK,1,1,1,false);
		            MouseEvent revent = new MouseEvent(btn,MouseEvent.MOUSE_RELEASED,0,MouseEvent.BUTTON1_MASK,1,1,1,false);
		            EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
		            eq.postEvent(pevent);
		            eq.postEvent(revent); 
		        }  
		    }  
		    protected JButton getTargetButton(Component c){
		    	if(c == null) return null;
		    	if(c instanceof JOptionPane){
		    		JOptionPane jp = (JOptionPane)c;
		    		int i=0;
		    		do{
		    			try{
		    				Component c1 = jp.getComponent(i);
		    				JButton j1 = getTargetButton(c1);
		    				if(j1!=null) return j1;
		    			}catch(Exception e){
		    				break;
		    			}
		    			i ++;
		    		}while(true);
		    	}
		    	if(c instanceof JPanel){
		    		JPanel jp = (JPanel)c;
		    		int i=0;
		    		do{
		    			try{
		    				Component c1 = jp.getComponent(i);
		    				JButton j1 = getTargetButton(c1);
		    				if(j1!=null) return j1;
		    			}catch(Exception e){
		    				break;
		    			}
		    			i ++;
		    		}while(true);
		    	}
		    	if(c instanceof JButton){
		    		JButton jp = (JButton)c;
		    		if("确定".equals(jp.getText())){
		    			return jp;
		    		}
		    	}
		    	return null;
		    }
		    public void keyReleased(KeyEvent e) {  
		    }  
		    public void keyTyped(KeyEvent e) {  
		    }  
		});  
		
		if(imageObj!=null){
			p.add(new JLabel("Code:"));
			p.add(tfValidcode);
			if(imageObj instanceof String){
				String imageUrl = (String)imageObj;
				if(imageUrl.length()>0){
					if(imageUrl.toLowerCase().indexOf("://")>=0){
						try {
							URL inetUrl = new URL(imageUrl);
							ImageIcon iicon = new ImageIcon(inetUrl);
							p.add(new JLabel(iicon));
						} catch (MalformedURLException e) {
						}
					}else{
						ImageIcon iicon = new ImageIcon(imageUrl);
						p.add(new JLabel(iicon));
					}
				}
			}
			if(imageObj instanceof Image){
				ImageIcon iicon = new ImageIcon((Image)imageObj);
				p.add(new JLabel(iicon));
			}
			if(imageObj instanceof URL){
				ImageIcon iicon = new ImageIcon((URL)imageObj);
				p.add(new JLabel(iicon));
			}
			if(imageObj instanceof byte[]){
				ImageIcon iicon = new ImageIcon((byte[])imageObj);
				p.add(new JLabel(iicon));
			}
		}

		this.setAlwaysOnTop(true);
		Object[] options = {"确定", "取消"}; 
		int option = JOptionPane.showOptionDialog(this, p, "图文输入框",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, null);

		if (option== JOptionPane.OK_OPTION ) {
			this.validcode = tfValidcode.getText();
			this.isCancel = false;
		}
		if (option== JOptionPane.CANCEL_OPTION ||
			option== JOptionPane.CLOSED_OPTION ||
			option== JOptionPane.NO_OPTION) {
			this.isCancel = true;
		}
		
		this.dispose();
	}

	public String getValidcode() {
		return validcode;
	}
	
	public boolean isCancel(){
		return isCancel;
	}
}
