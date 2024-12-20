import java.awt.*;

import javax.swing.*;


import java.awt.event.*;

class MedimonController implements ActionListener {
    protected CommandFrame view;
    protected MedimonModel model;
    protected CommServer sv;
    protected CommClient cl;

    //サーバからのメッセージを記録
    private String serverMsg;

    //自分がサーバかクライアントかを記録
    private boolean isServer;
    
    //boolean serverConducting == 0のとき、サーバが処理中(入力は受け付けない)
    //boolean clientConducting == 0のとき、クライアントが処理中(入力は受け付けない)
    //get server/clientConducting();とメッセージで現在の処理状態を取得
    private int serverConducting = 0, clientConducting = 0;

    //コマンド入力待ち
    //server/clientInput == 1のとき、入力された
    private int serverInput = 0, clientInput = 0;

    //モンスター交代時の入力待ち
    private boolean changeMonsterWaiting = false;
    
    //existCommandWindow == trueのとき、Viewのコマンドウィンドウを表示する
    private boolean existCommandWindow = true;
    
    private int monsterID, skillID, opponentMonsterID, opponentSkillID;
    public MedimonController(MedimonModel m, CommandFrame v, boolean isServer) {
        view = v;
        model = m;
        this.isServer = isServer;
        if(isServer){
            sv = new CommServer(10010);
        } else {
            cl = new CommClient("localhost", 10010);
        }
        view.getButton(0).addActionListener(this);
        view.getButton(1).addActionListener(this);
        view.getButton(2).addActionListener(this);
        view.getButton(3).addActionListener(this);
        view.getButton(4).addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        //if(Integer.parseInt(e.getActionCommand()) == (モンスター交代のskill番号)) changeMonsterWaiting = true;
        
        //skillIDを設定しやすいように、JButton名.setActionCommand(skillID);を設定してください
        view.windowClose();
        skillID = Integer.parseInt(e.getActionCommand());
        //monsterID = view.getMonsterID(); //仮称: getMonsterID();でviewからMonsterのindex番号を受け取る

        //サーバに送る
        if(isServer){
            serverInput = 1;
            sendAction(monsterID, skillID);
        } else {
            clientInput = 1;
            sendAction(monsterID, skillID);
        }
        
        //サーバ/クライアント両方のmodelにデータ送信
        while(isServer){
            recvFromServer();
            if(clientInput == 1){
                setToModel();
                break;
            }
        }
        while(!isServer){
            recvFromServer();
            if(serverInput == 1){
                setToModel();
                break;
            }
        }

        //サーバ/クライアント両方の処理が終わり次第コマンドウィンドウ表示を命令
        while(isServer){
            if(model.getServerConducting() == 0 && clientConducting == 0){
                existCommandWindow = true;
                break;
            }
        }
        while(!isServer){
            if(model.getServerConducting() == 0 && serverConducting == 0){
                existCommandWindow = true;
                break;
            }
        }

        /*if(isServer){
            if(model.getServerConducting() == 0){
                recvFromServer();
                if(clientConducting == 0){
                    setToModel();
                }
            }
        } else {
            if(model.getServerConducting() == 0){
                recvFromServer();
                if(serverConducting == 0){
                    setToModel();
                }
            }
        }*/
    }

    //サーバにデータを送るメソッド
    public void sendAction(int monsterIndex, int skillIndex){
        if(isServer){
            serverMsg = String.format("%d %d %d %d %d", monsterIndex, skillIndex, 1, serverConducting, serverInput); //1 -> server
            System.out.println(serverMsg);
            sv.send(serverMsg);
        } else {
            serverMsg = String.format("%d %d %d %d %d", monsterIndex, skillIndex, 2, clientConducting, clientInput); //2 ->client
            System.out.println(serverMsg);
            cl.send(serverMsg);
        }
    }

    //サーバからデータを受け取るメソッド
    public void recvFromServer(){
        if(isServer){
            String[] msgArray = sv.recv().split(" ");
            System.out.println(sv.recv());
            opponentMonsterID = Integer.parseInt(msgArray[0]);
            opponentSkillID = Integer.parseInt(msgArray[1]);
            clientConducting = Integer.parseInt(msgArray[3]);
            clientInput = Integer.parseInt(msgArray[4]);
        } else {
            String[] msgArray = cl.recv().split(" ");
            System.out.println(cl.recv());
            opponentMonsterID = Integer.parseInt(msgArray[0]);
            opponentSkillID = Integer.parseInt(msgArray[1]);
            serverConducting = Integer.parseInt(msgArray[3]);
            serverInput = Integer.parseInt(msgArray[4]);
        }
        //View, model側にmonsterID, skillIDの値をしまうint型変数と、
        //それをsetするメソッドを作ってください
    }

    //ボタン入力から受け取った自分のmonster/skillIDと、メッセージから受け取った相手のmonster/skillIDをmodelにsetするメソッド
    public void setToModel(){
        //setSkillのPlayer1にはserverの情報を、Player2にはclientの情報を入力するように実装しました
        if(isServer){
            model.setSkill(monsterID, skillID, opponentMonsterID, opponentSkillID);
        } else {
            model.setSkill(opponentMonsterID, opponentSkillID, monsterID, skillID);
        }
    }

    //コマンドウィンドウの非/表示を命令するboolean値
    public boolean getExistCommandWindow(){
        return existCommandWindow;
    }
}

class BattleFrame extends JFrame {
  public BattleFrame(boolean server, MedimonModel mm){
  int me, you;
  if(server == true){
    me = 1; you = 2;
  }else{
    me = 2; you = 1;
  }
  Medimon nowmymedimon = mm.getMedimon(me, 0), nowyourmedimon = mm.getMedimon(you, 0);

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

    
    this.setVisible(true);
  }

  public void windowClose(){
    System.out.println("OK");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  }


  public JButton getButton(int i){
    if(i==1){return b1;}
    else if(i==2){return b2;}
    else if(i==3){return b3;}
    else if(i==4){return b4;}
    else{return b5;}
  }
}

public class MedimonGame {
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
