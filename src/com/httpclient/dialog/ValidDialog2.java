package com.httpclient.dialog;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 弹出对话框
 */
public class ValidDialog2 extends DialogFrame {

	private static final long serialVersionUID = 1072956979978136974L;
	private String validcode = "";
	private boolean isCancel = false;
	JPanel p = new JPanel(new GridLayout(0, 1));
	private String dialogName;
	private Object imageObj;

	public ValidDialog2(Object imageObj){
		this.imageObj = imageObj;
		this.dialogName = "图文输入框";
	}
	
	public void showDialog(){
		final JTextField tfValidcode = new JTextField();
		
		tfValidcode.addKeyListener(new KeyListener() {  
		    public void keyPressed(KeyEvent e) {  
		        if (e.getKeyCode() == KeyEvent.VK_ENTER) {  
		            Component c = e.getComponent();
		            c = getParent(c,JOptionPane.class.getSimpleName());
		            
		            JButton btn = getTargetButton(c, "确定");
		            clickTarget(btn);
		        }  
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
		int option = JOptionPane.showOptionDialog(this, p, getDialogName(),
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

	public String getDialogName() {
		return dialogName;
	}

	public void setDialogName(String dialogName) {
		this.dialogName = dialogName;
	}
	
}
