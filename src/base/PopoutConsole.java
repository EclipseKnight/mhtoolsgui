package base;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class PopoutConsole {

	private JFrame frame;
	private JTextArea textArea;
	public PopoutConsole(JTextArea textField) {
		initialize();
		setText(textField.getText());
	}
	
	private void initialize() {
		frame = new JFrame("Console");
		frame.setBounds(100, 100, 550, 450);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);
		
		frame.setVisible(true);
	}
	
	private void setText(String text) {
		textArea.setText(text);
	}
	
	public void append(String text) {
		textArea.append(text);
	}

}
