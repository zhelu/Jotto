package jotto.gui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

public class Help extends JFrame {

	private static final long serialVersionUID = 4118831702674022233L;
	private JPanel contentPane;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Help frame = new Help();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Create the frame.
	 * 
	 * @throws IOException
	 */
	public Help() throws IOException {
		setTitle("Help");
		setResizable(false);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		InputStreamReader isr = new InputStreamReader(getClass()
				.getClassLoader().getResourceAsStream(
						"jotto/resources/help.html"));
		BufferedReader bw = new BufferedReader(isr);
		String html = "";
		String line = bw.readLine();
		while (line != null) {
			html += line;
			line = bw.readLine();
		}

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 11, 424, 250);
		contentPane.add(scrollPane);

		JEditorPane editorPane = new JEditorPane();
		scrollPane.setViewportView(editorPane);
		editorPane.setContentType("text/html");
		editorPane.setEditable(false);
		editorPane.setText(html);
		editorPane.setCaretPosition(0);

		bw.close();
	}
}
