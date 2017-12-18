package com.httpclient.dialog;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JFrame;

public class DialogFrame extends JFrame {

	private static final long serialVersionUID = -4282615320489381805L;

	protected void clickTarget(Container btn){
        if(btn==null) return;
        MouseEvent pevent = new MouseEvent(btn,MouseEvent.MOUSE_PRESSED,0,MouseEvent.BUTTON1_MASK,1,1,1,false);
        MouseEvent revent = new MouseEvent(btn,MouseEvent.MOUSE_RELEASED,0,MouseEvent.BUTTON1_MASK,1,1,1,false);
        EventQueue eq = Toolkit.getDefaultToolkit().getSystemEventQueue();
        eq.postEvent(pevent);
        eq.postEvent(revent); 
	}
	
	protected Component getParent(Component c, String parent){
		while(c.getParent()!=null){
			c = c.getParent();
			if(parent!=null && c.getClass()
					.getCanonicalName().endsWith(parent)){
				return c;
			}
		}
		return c;
	}
	
    protected JButton getTargetButton(Component c, String name){
    	if(c == null) return null;
    	if(c instanceof JButton){
    		JButton jp = (JButton)c;
    		if(name.equals(jp.getText())){
    			return jp;
    		}
    	}
    	else if(c instanceof Container){
    		Container jp = (Container)c;
    		for(int i=0; i<jp.getComponentCount(); i++){
    			try{
    				Component c1 = jp.getComponent(i);
    				JButton j1 = getTargetButton(c1, name);
    				if(j1!=null) return j1;
    			}catch(Exception e){
    			}
    		}
    	}
    	return null;
    }
    
}
