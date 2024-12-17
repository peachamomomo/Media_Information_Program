import java.awt.*;
import java.awt.event.ActionListener;

import javax.swing.*;
 
class BattleFrame extends JFrame {
  public BattleFrame(boolean server, MedimonModel mm){
  int me, you;
  if(server == true){
    me = 1; you = 2;
  }else{
    me = 2; you = 1;
  }
  Medimon nowmymedimon = mm.getMedimon(me, 1), nowyourmedimon = mm.getMedimon(you, 1);

  JPanel p1=new JPanel(),p2=new JPanel(),p6=new JPanel(),p7=new JPanel();

	JLabel l1=new JLabel("<html><img src='file:Background.png' width=100 height=200></html>",JLabel.CENTER);
	JLabel l2=new JLabel("<html><span style='font-size:20pt; color:black;'>" + nowmymedimon.getName() +  "</span></html>",JLabel.LEFT); //自分側のモンスターの名前
  JLabel l3=new JLabel("<html><img src='file:Thogg_b.png' width=500 height=500></html>",JLabel.CENTER); //自分側のモンスターの画像
	JLabel l4=new JLabel("<html><span style='font-size:20pt; color:black;'>" + nowyourmedimon.getName() + "</span></html>",JLabel.RIGHT); //相手側のモンスターの名前
  JLabel l5=new JLabel("<html><img src='file:Thogg_f.png' width=500 height=500></html>",JLabel.CENTER); //相手側のモンスターの画像
	JLabel l6=new JLabel("<html><span style='font-size:20pt; color:black;'>HP " + nowmymedimon.getHp() + "/" + nowmymedimon.getMaxHp() + "</span></html>",JLabel.LEFT); //自分側のモンスターのHP
	JLabel l7=new JLabel("<html><span style='font-size:20pt; color:black;'>HP " + nowyourmedimon.getHp() + "/" + nowyourmedimon.getMaxHp() + "</span></html>",JLabel.RIGHT); //相手側のモンスターのHP

	JProgressBar bar1 = new JProgressBar(0, nowmymedimon.getMaxHp());
	bar1.setValue(nowmymedimon.getHp());
  bar1.setForeground(Color.GREEN);
	JProgressBar bar2 = new JProgressBar(0, nowyourmedimon.getMaxHp());
	bar2.setValue(nowyourmedimon.getHp());
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
  MedimonController mc;
  JButton b1, b2, b3, b4, b5;
  public CommandFrame(boolean server, MedimonModel mm){
    JPanel p1=new JPanel(),p2=new JPanel();
    // JPanelに貼り付けるためのJButtonを5つ生成
    b1=new JButton("Skill 1"); b2=new JButton("Skill 2");
    b3=new JButton("Skill 3"); b4=new JButton("Skill 4");
    b5=new JButton("こうたい");
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

    if(mc.getExistCommandWindow() == false){
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    this.setVisible(true);
  }

  public void setMedimonController(MedimonController mc){
    this.mc = mc;
  }

  public JButton getButton(int i){
    if(i==1){return b1;}
    else if(i==2){return b2;}
    else if(i==3){return b3;}
    else if(i==4){return b4;}
    else{return b5;}
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
    BattleFrame mv1;
    MedimonController mc;
    CommandFrame mv2;
    if (args[0].equals("server")){
       server=true;
       System.out.println("Server mode");
       str="server";
       mm = new MedimonModel();
       mv1 = new BattleFrame(server, mm);
       mv2 = new CommandFrame(server, mm);
       mc = new MedimonController(mm, mv2, server);
       mv2.setMedimonController(mc);
    }else{
       server=false;
       System.out.println("Client mode");
       str="cilent";
       mm = new MedimonModel();
       mv1 = new BattleFrame(server, mm);
       mv2 = new CommandFrame(server, mm);
       mc = new MedimonController(mm, mv2, server);
    }
    
  }
}
