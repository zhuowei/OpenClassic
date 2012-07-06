package ch.spacebase.openclassic.client;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import ch.spacebase.openclassic.client.util.GeneralUtils;

import com.mojang.minecraft.render.TextureManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JCheckBox;

public class Login extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;

	/**
	 * Create the frame.
	 */
	public Login() {
		try {
			setIconImage(ImageIO.read(TextureManager.class.getResourceAsStream("/icon.png")));
		} catch (IOException e) {
		}
		
		setResizable(false);
		setTitle("Login");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 227, 117);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username");
		lblUsername.setBounds(16, 0, 72, 25);
		contentPane.add(lblUsername);
		
		textField = new JTextField();
		textField.setBounds(88, 0, 131, 25);
		contentPane.add(textField);
		textField.setColumns(10);
		
		JLabel lblPassword = new JLabel("Password");
		lblPassword.setBounds(16, 24, 70, 28);
		contentPane.add(lblPassword);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(88, 26, 131, 25);
		contentPane.add(passwordField);
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(this.getLoginFile()));
			String line = "";
			while((line = reader.readLine()) != null) {
				if(this.textField.getText().equals("")) {
					this.textField.setText(line);
				} else if(String.valueOf(this.passwordField.getPassword()).equals("")) {
					this.passwordField.setText(line);
					break;
				}
			}
		} catch(IOException e) {
			System.out.println("Failed to check login file.");
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		final JCheckBox chckbxRemember = new JCheckBox("Remember");
		chckbxRemember.setBounds(16, 58, 115, 18);
		contentPane.add(chckbxRemember);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				if(chckbxRemember.isSelected()) {
					BufferedWriter writer = null;
					
					try {
						writer = new BufferedWriter(new FileWriter(getLoginFile()));
						writer.write(textField.getText());
						writer.newLine();
						writer.write(String.valueOf(passwordField.getPassword()));
					} catch (IOException e1) {
						System.out.println("Failed to create login remember file.");
						e1.printStackTrace();
					} finally {
						if(writer != null) {
							try {
								writer.close();
							} catch(IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
				
				MinecraftStandalone.init(textField.getText(), String.valueOf(passwordField.getPassword()));
				dispose();
			}
		});
		
		btnLogin.setBounds(139, 55, 73, 25);
		contentPane.add(btnLogin);
		getRootPane().setDefaultButton(btnLogin);
	}
	
	private File getLoginFile() {
		File file = new File(GeneralUtils.getMinecraftDirectory(), ".login");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e1) {
				System.out.println("Failed to create login remember file.");
				e1.printStackTrace();
			}
		}
		
		return file;
	}
}
