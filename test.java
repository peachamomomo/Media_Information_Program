import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

class Frame extends JFrame {
    public Frame(int judge) {
        JFrame frame = new JFrame();
        JPanel p1; 
        JLabel l1;
        p1 = new JPanel();
        l1 = new JLabel();
        if(judge == 1){
            l1.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>YOU WIN!</span></html>");
        } else {
            l1.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>YOU LOSE...</span></html>");
        }
        p1.add(l1);
        frame.add(p1);
        frame.setSize(1000, 600);
        frame.setLocation(460, 140);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
}

public class test {
    public static void main(String[] args) {
        Frame frame = new Frame(1);
    }
    
}
