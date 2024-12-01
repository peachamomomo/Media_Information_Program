import java.awt.*;
import javax.swing.*;

class ColorLabel extends JLabel {
	ColorLabel(int no, Color border, Color back) {
		super("No." + String.valueOf(no), SwingConstants.CENTER);
		setBackground(back);
		setOpaque(true);
		setBorder(BorderFactory.createLineBorder(border,4));
	}
}

class Medimon1 extends JFrame {
  public Medimon1(){
    GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();

		JPanel panel = new JPanel();
		panel.setLayout(gridbag);

		constraints.fill = GridBagConstraints.BOTH;
		constraints.weightx = 100.0;
		constraints.weighty = 100.0;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth= 1;
		constraints.gridheight = GridBagConstraints.REMAINDER;

		ColorLabel p1 = new ColorLabel(1, Color.red, Color.lightGray);
		gridbag.setConstraints(p1, constraints);
		panel.add(p1);


		constraints.gridx = GridBagConstraints.RELATIVE;
		constraints.gridheight = 1;
		ColorLabel p2 = new ColorLabel(2, Color.blue, Color.cyan);
		gridbag.setConstraints(p2, constraints);
		panel.add(p2);

		ColorLabel p3 = new ColorLabel(3, Color.green, Color.cyan);
		gridbag.setConstraints(p3, constraints);
		panel.add(p3);

		ColorLabel p4 = new ColorLabel(4, Color.blue, Color.cyan);
		gridbag.setConstraints(p4, constraints);
		panel.add(p4);

		constraints.gridheight = GridBagConstraints.REMAINDER;
		ColorLabel p5 = new ColorLabel(5, Color.red, Color.lightGray);
		panel.add(p5, constraints);

		constraints.gridx = 1;
		constraints.gridy = 1;
		constraints.gridwidth = GridBagConstraints.RELATIVE ;
		constraints.gridheight = 1;
		ColorLabel p6 = new ColorLabel(6, Color.magenta, Color.yellow);
		panel.add(p6, constraints);

		constraints.gridy = GridBagConstraints.RELATIVE;
		ColorLabel p7 = new ColorLabel(7, Color.orange, Color.yellow);
		panel.add(p7, constraints);

		this.pack(); 

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }
  public static void main(String argv[]) {
    new Medimon1();
  }
}
