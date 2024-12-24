import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

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
    //private int serverInput = 0, clientInput = 0;

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
        
        view.windowClose();
        System.out.println("commandWindow :close");
        skillID = Integer.parseInt(e.getActionCommand());
        //monsterID = view.getMonsterID(); //仮称: getMonsterID();でviewからMonsterのindex番号を受け取る

        //サーバに送る
        if(isServer){
            sendAction(monsterID, skillID);
        } else {
            sendAction(monsterID, skillID);
        }
        System.out.println("sendAction: finish");
        
        //サーバ/クライアント両方のmodelにデータ送信
        if(isServer){
            recvFromServer();
            System.out.println("recvFromServer: finish");
            setToModel();
            System.out.println("setToModel: finish");
        } else {
            recvFromServer();
            System.out.println("recvFromServer: finish");
            setToModel();
            System.out.println("setToModel: finish");
        }

        //サーバ/クライアント両方の処理が終わり次第コマンドウィンドウ表示を命令
        while(isServer){
          System.out.println("commandWindow :conduct");
          sendAction(monsterID, skillID);
          recvFromServer();
            if(model.getServerConducting() == 0 && clientConducting == 0){
                //view.windowOpen();
                System.out.println("commandWindow :open");
                break;
            }
        }
        while(!isServer){
          System.out.println("commandWindow :conduct");
          sendAction(monsterID, skillID);
          recvFromServer();
            if(model.getServerConducting() == 0 && serverConducting == 0){
                //view.windowOpen();
                System.out.println("commandWindow :open");
                break;
            }
        }
    }

    //サーバにデータを送るメソッド
    public void sendAction(int monsterIndex, int skillIndex){
        if(isServer){
            serverMsg = String.format("%d %d %d %d", monsterIndex, skillIndex, 1, model.getServerConducting()); //1 -> server
            System.out.println("send: " + serverMsg);
            sv.send(serverMsg);
        } else {
            serverMsg = String.format("%d %d %d %d", monsterIndex, skillIndex, 2, model.getServerConducting()); //2 ->client
            System.out.println("send: " + serverMsg);
            cl.send(serverMsg);
        }
    }

    //サーバからデータを受け取るメソッド
    public void recvFromServer(){
        if(isServer){
            String[] msgArray = (sv.recv()).split(" ");
            opponentMonsterID = Integer.parseInt(msgArray[0]);
            opponentSkillID = Integer.parseInt(msgArray[1]);
            clientConducting = Integer.parseInt(msgArray[3]);
            //clientInput = Integer.parseInt(msgArray[4]);
            System.out.println("recv: "+opponentMonsterID+" "+opponentSkillID
                                +" "+msgArray[2]+" "+clientConducting);
          } else {
            String[] msgArray = (cl.recv()).split(" ");
            opponentMonsterID = Integer.parseInt(msgArray[0]);
            opponentSkillID = Integer.parseInt(msgArray[1]);
            serverConducting = Integer.parseInt(msgArray[3]);
            //serverInput = Integer.parseInt(msgArray[4]);
            System.out.println("recv: "+opponentMonsterID+" "+opponentSkillID
                                +" "+msgArray[2]+" "+clientConducting);
          }
        //View, model側にmonsterID, skillIDの値をしまうint型変数と、
        //それをsetするメソッドを作ってください
    }

    //ボタン入力から受け取った自分のmonster/skillIDと、メッセージから受け取った相手のmonster/skillIDをmodelにsetするメソッド
    public void setToModel(){
        //setSkillのPlayer1にはserverの情報を、Player2にはclientの情報を入力するように実装しました
        if(isServer){
            System.out.println("setToModel: "+monsterID+" "+skillID+" "+opponentMonsterID+" "+opponentSkillID);
            model.setSkill(monsterID, skillID, opponentMonsterID, opponentSkillID);
        } else {
            System.out.println("setToModel: "+opponentMonsterID+" "+opponentSkillID+" "+monsterID+" "+skillID);
            model.setSkill(opponentMonsterID, opponentSkillID, monsterID, skillID);
        }
    }

    //コマンドウィンドウの非/表示を命令するboolean値
    public boolean getExistCommandWindow(){
        return existCommandWindow;
    }
}

class BattleFrame extends JFrame implements ActionListener{
  private Timer timer;
  boolean server;
  int me, you;
  Medimon nowmymedimon, nowyourmedimon;
  JLabel l1, l2, l3, l4, l5, l6, l7;
  JProgressBar bar1, bar2;
  public BattleFrame(boolean server, MedimonModel mm){
  this.server = server;
  if(server == true){
    this.me = 1; this.you = 2;
  }else{
    this.me = 2; this.you = 1;
  }
  this.nowmymedimon = mm.getMedimon(me, 0); this.nowyourmedimon = mm.getMedimon(you, 0);

  JPanel p1=new JPanel(),p2=new JPanel(),p6=new JPanel(),p7=new JPanel();

	l1=new JLabel(); l2=new JLabel(); l3=new JLabel(); l4=new JLabel(); l5=new JLabel(); l6=new JLabel(); l7=new JLabel(); 
  l1.setHorizontalAlignment(JLabel.CENTER);
  l2.setHorizontalAlignment(JLabel.LEFT);
  l3.setHorizontalAlignment(JLabel.CENTER);
  l4.setHorizontalAlignment(JLabel.RIGHT);
  l5.setHorizontalAlignment(JLabel.CENTER);
  l6.setHorizontalAlignment(JLabel.LEFT);
  l7.setHorizontalAlignment(JLabel.RIGHT);
  l1.setText("<html><img src='file:Background.png' width=100 height=200></html>");
  l2.setText("<html><span style='font-size:20pt; color:black;'>" + this.nowmymedimon.getName() +  "</span></html>"); //自分側のモンスターの名前
  l3.setText("<html><img src='file:Thogg_b.png' width=500 height=500></html>"); //自分側のモンスターの画像
	l4.setText("<html><span style='font-size:20pt; color:black;'>" + this.nowyourmedimon.getName() + "</span></html>"); //相手側のモンスターの名前
  l5.setText("<html><img src='file:Thogg_f.png' width=500 height=500></html>"); //相手側のモンスターの画像
	l6.setText("<html><span style='font-size:20pt; color:black;'>HP " + this.nowmymedimon.getHp() + "/" + this.nowmymedimon.getMaxHp() + "</span></html>"); //自分側のモンスターのHP
	l7.setText("<html><span style='font-size:20pt; color:black;'>HP " + this.nowyourmedimon.getHp() + "/" + this.nowyourmedimon.getMaxHp() + "</span></html>"); //相手側のモンスターのHP
  
	bar1 = new JProgressBar();
  bar1.setMinimum(0); bar1.setMaximum(this.nowmymedimon.getMaxHp());
	bar1.setValue(this.nowmymedimon.getHp());
  bar1.setForeground(Color.GREEN);
	bar2 = new JProgressBar();
  bar2.setMinimum(0); bar2.setMaximum(this.nowyourmedimon.getMaxHp());
	bar2.setValue(this.nowyourmedimon.getHp());
  bar2.setForeground(Color.GREEN);
  
  if(me==1){
    this.setTitle("Server Mode");
  }else{
    this.setTitle("Client Mode");
  }
  

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

  timer = new Timer(1, this); // 1ミリ秒毎にactionPerformedを呼び出し
  timer.start(); // stopメソッドを呼ぶまでactionPerformedが呼ばれ続ける
  }
  public void actionPerformed(ActionEvent e){
	  l2.setText("<html><span style='font-size:20pt; color:black;'>" + this.nowmymedimon.getName() +  "</span></html>"); //自分側のモンスターの名前
    l3.setText("<html><img src='file:Thogg_b.png' width=500 height=500></html>"); //自分側のモンスターの画像
	  l4.setText("<html><span style='font-size:20pt; color:black;'>" + this.nowyourmedimon.getName() + "</span></html>"); //相手側のモンスターの名前
    l5.setText("<html><img src='file:Thogg_f.png' width=500 height=500></html>"); //相手側のモンスターの画像
	  l6.setText("<html><span style='font-size:20pt; color:black;'>HP " + this.nowmymedimon.getHp() + "/" + nowmymedimon.getMaxHp() + "</span></html>"); //自分側のモンスターのHP
    bar1.setValue(this.nowmymedimon.getHp());
	  l7.setText("<html><span style='font-size:20pt; color:black;'>HP " + this.nowyourmedimon.getHp() + "/" + nowyourmedimon.getMaxHp() + "</span></html>"); //相手側のモンスターのHP
    bar2.setValue(this.nowyourmedimon.getHp());
  }
}



class CommandFrame extends JFrame{
  JButton b1, b2, b3, b4, b5;
  public CommandFrame(boolean server, MedimonModel mm){
    JPanel p1=new JPanel(),p2=new JPanel();
    int me;
    if(server == true){me = 1;}
    else{me = 2;}
    // JPanelに貼り付けるためのJButtonを5つ生成
    b1=new JButton(mm.getSkill(me,0).getSkillname()); b2=new JButton(mm.getSkill(me,1).getSkillname());
    b3=new JButton(mm.getSkill(me,2).getSkillname()); b4=new JButton(mm.getSkill(me,3).getSkillname());
    b5=new JButton("こうたい");
    b1.setActionCommand("0");
    b2.setActionCommand("1");
    b3.setActionCommand("2");
    b4.setActionCommand("3");
    b5.setActionCommand("4");

    if(me==1){
      this.setTitle("Server Mode");
    }else{
      this.setTitle("Client Mode");
    }

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
