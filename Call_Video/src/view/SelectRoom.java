package view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import javax.swing.JButton;

public class SelectRoom extends JFrame implements ActionListener {

	private JPanel contentPane;
	private JTextField txtRoom;
	private JButton btnJoin, btnCreate;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SelectRoom frame = new SelectRoom();
			frame.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the frame.
	 */
	public SelectRoom() {
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 632, 428);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

		JPanel title = new JPanel();
		contentPane.add(title);
		title.setLayout(null);

		JLabel lblTitle = new JLabel("Select Room");
		lblTitle.setFont(new Font("Tahoma", Font.BOLD, 20));
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setBounds(202, 36, 177, 43);
		title.add(lblTitle);

		JPanel content = new JPanel();
		contentPane.add(content);
		content.setLayout(null);

		JLabel lblRoom = new JLabel("Room");
		lblRoom.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblRoom.setHorizontalAlignment(SwingConstants.CENTER);
		lblRoom.setBounds(72, 12, 118, 76);
		content.add(lblRoom);

		txtRoom = new JTextField();
		txtRoom.setBounds(255, 10, 294, 78);
		txtRoom.setFont(new Font("Tahoma", Font.PLAIN, 16));
		txtRoom.setHorizontalAlignment(SwingConstants.CENTER);
		content.add(txtRoom);
		txtRoom.setColumns(10);

		JPanel button = new JPanel();
		contentPane.add(button);
		button.setLayout(null);

		btnJoin = new JButton("Join");
		btnJoin.addActionListener(this);
		btnJoin.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnJoin.setBounds(431, 10, 111, 41);
		button.add(btnJoin);

		btnCreate = new JButton("Create");
		btnCreate.addActionListener(this);
		btnCreate.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnCreate.setBounds(321, 10, 92, 42);
		button.add(btnCreate);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton btnChecked = (JButton) e.getSource();
		if (btnChecked.equals(btnJoin) || btnChecked.equals(btnCreate)) {
			try {
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}     
		}

	}
}
