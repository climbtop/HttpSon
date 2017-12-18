package com.httpclient;

import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * 文本输入框
 */
public class InputDialog extends JFrame {
	private String inputText = "";
	private String inputLabel;
	private boolean isCancel = false;
	private JPanel p;

	public InputDialog() {
		this("内容输入框", 5);
	}
	
	public InputDialog(String label) {
		this(label, 5);
	}
	
	public InputDialog(int rows) {
		this("内容输入框", rows);
	}
	
	public InputDialog(String label, int rows) {
		p = new JPanel(new GridLayout(0, 1));
		JTextArea taInputText = new JTextArea();
		if ((label != null) && (label.length() > 0)) {
			inputLabel = label;
		}
		taInputText.setRows(rows);
		this.p.add(taInputText);
		
		this.setAlwaysOnTop(true);
		int option = JOptionPane.showConfirmDialog(this, this.p, inputLabel, 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option== JOptionPane.OK_OPTION ) {
			this.inputText = taInputText.getText();
			this.isCancel = false;
		}
		if (option== JOptionPane.CANCEL_OPTION ||
			option== JOptionPane.CLOSED_OPTION ||
			option== JOptionPane.NO_OPTION) {
			this.isCancel = true;
		}
		dispose();
	}

	public String getInputText() {
		return this.inputText;
	}
	
	public boolean isCancel(){
		return isCancel;
	}
}