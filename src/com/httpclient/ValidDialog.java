package com.httpclient;

import java.awt.GridLayout;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 弹出对话框
 */
public class ValidDialog extends JFrame {
	private String validcode = "";
	private boolean isCancel = false;
	JPanel p = new JPanel(new GridLayout(0, 1));
	
	public ValidDialog(String imageUrl){
		JTextField tfValidcode = new JTextField();
		if(imageUrl!=null&&imageUrl.length()>0){
			p.add(new JLabel("Code:"));
			p.add(tfValidcode);
		}
		//验证码图片
		if(imageUrl!=null && imageUrl.length()>0){
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
		
		this.setAlwaysOnTop(true);
		int option = JOptionPane.showConfirmDialog(this, p, "图文输入框",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
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
