package com.httpclient.dialog;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * 文本输入框
 */
public class InputDialog2 extends DialogFrame {

	private static final long serialVersionUID = 5691878477215642577L;
	private String inputText = "";
	private boolean isCancel = false;
	private String dialogName;
	private JPanel p;
	private int rows;

	public InputDialog2() {
		this("内容输入框", 5);
	}
	
	public InputDialog2(String dialogName) {
		this(dialogName, 5);
	}
	
	public InputDialog2(int rows) {
		this("内容输入框", rows);
	}
	
	public InputDialog2(String dialogName, int rows) {
		this.dialogName = dialogName;
		this.rows = rows;
	}
	
	public void showDialog() {
		p = new JPanel(new GridLayout(0, 1));
		JTextArea taInputText = new JTextArea();
		taInputText.setRows(rows);
		this.p.add(taInputText);
		
		this.setAlwaysOnTop(true);
		
		Object[] options = {"确定", "取消"}; 
		int option = JOptionPane.showOptionDialog(this, this.p, getDialogName(), 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		
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
	
	public void autoFillInputText(String text){
        JTextArea taInputText = (JTextArea)p.getComponent(0);
        taInputText.setText(text);
        Component c = getParent(p,JOptionPane.class.getSimpleName());
        JButton btn = getTargetButton(c, "确定");
        clickTarget(btn);
	}

	public String getInputText() {
		return this.inputText;
	}
	
	public String getDialogName() {
		return dialogName;
	}

	public void setDialogName(String dialogName) {
		this.dialogName = dialogName;
	}

	public boolean isCancel(){
		return isCancel;
	}
}