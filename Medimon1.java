import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
 
class BattleFrame extends JFrame {
  public BattleFrame(boolean server){
  JPanel p1=new JPanel(),p2=new JPanel(),p6=new JPanel(),p7=new JPanel();

	JLabel l1=new JLabel("<html><img src='file:Background.png' width=100 height=200></html>",JLabel.CENTER);
	JLabel l2=new JLabel("<html><span style='font-size:20pt; color:black;'>" + "</span></html>",JLabel.LEFT); //自分側のモンスターの名前
  JLabel l3=new JLabel("<html><img src='file:Thogg_b.png' width=500 height=500></html>",JLabel.CENTER); //自分側のモンスターの画像
	JLabel l4=new JLabel("<html><span style='font-size:20pt; color:black;'>トーグ</span></html>",JLabel.RIGHT); //相手側のモンスターの名前
  JLabel l5=new JLabel("<html><img src='file:Thogg_f.png' width=500 height=500></html>",JLabel.CENTER); //相手側のモンスターの画像
	JLabel l6=new JLabel("<html><span style='font-size:20pt; color:black;'>HP 100/100</span></html>",JLabel.LEFT); //自分側のモンスターのHP
	JLabel l7=new JLabel("<html><span style='font-size:20pt; color:black;'>HP 100/100</span></html>",JLabel.RIGHT); //相手側のモンスターのHP

	JProgressBar bar1 = new JProgressBar();
	bar1.setValue(100);
  bar1.setForeground(Color.GREEN);
	JProgressBar bar2 = new JProgressBar();
	bar2.setValue(100);
  bar2.setForeground(Color.GREEN);
 
  this.setTitle("メディアモンスター");

  p1.setLayout(new BorderLayout());
  p2.setLayout(new BorderLayout());
	p6.setLayout(new GridLayout(3,1));
	p7.setLayout(new GridLayout(3,1));

	p1.add(p6, BorderLayout.NORTH);
	p2.add(p7, BorderLayout.NORTH);

  this.add(p1,BorderLayout.WEST);
  this.add(p2,BorderLayout.EAST);
  this.add(l1,BorderLayout.CENTER);
	p1.add(l3,BorderLayout.CENTER);
	p2.add(l5,BorderLayout.CENTER);
	p6.add(l2);
	p6.add(bar1);
	p6.add(l6);
	p7.add(l4);
	p7.add(bar2);
	p7.add(l7);
  
  this.setSize(1200, 600); 
  this.setLocation(360, 140);
  this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  this.setVisible(true);
  }
}

class CommandFrame extends JFrame{
  public CommandFrame(boolean server){
    JPanel p1=new JPanel(),p2=new JPanel();
    // JPanelに貼り付けるためのJButtonを5つ生成
    JButton b1=new JButton("button 1"),b2=new JButton("button 2");
    JButton b3=new JButton("button 3"),b4=new JButton("button 4");
    JButton b5=new JButton("button 5");
    b1.setActionCommand("0");
    b2.setActionCommand("1");
    b3.setActionCommand("2");
    b4.setActionCommand("3");
    b5.setActionCommand("4");

    this.setTitle("コマンド");

    this.setLayout(new GridLayout(1,3));

    this.add(p1);
    this.add(p2);
    this.add(b5);

    p1.setLayout(new GridLayout(2,1));
    p2.setLayout(new GridLayout(2,1));

    p1.add(b1); p1.add(b2); p2.add(b3); p2.add(b4);
   
    this.setSize(1200,200);
    this.setLocation(360, 740);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
    }
}

public class Medimon1 {
  public static void main(String[] args) {
    String str;
    boolean server=false;
    if (args.length<2){
      System.out.println("Usage : java Medimon1 {server/{host name}} {port no.} \n");     
      System.exit(1);
    }
    MedimonModel mm;
    if (args[0].equals("server")){
       server=true;
       System.out.println("Server mode");
       str="server";
       mm = new MedimonModel();
    }else{
       server=false;
       System.out.println("Client mode");
       str="cilent";
       mm = new MedimonModel();
    }
    new BattleFrame(server);
    new CommandFrame(server);
  }
}
