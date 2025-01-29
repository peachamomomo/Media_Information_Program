import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException; 
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.imageio.ImageIO;

class MedimonController implements ActionListener {
  protected TitleFrame title;
  protected BattleFrame battle;
  protected CommandFrame view;
  protected MedimonModel model;
  protected CommServer sv;
  protected CommClient cl;
  protected TextFrame tf;
  protected ChangeFrame changeFrame;
  protected ResultFrame result;
  private Clip clip1 = model.createClip(new File("startbutton.wav"));
  private Clip clip2 = model.createClip(new File("button.wav"));
  private Clip clip3 = model.createClip(new File("error.wav"));
  private Clip clip4 = model.createClip(new File("Death.wav"));

  //サーバからのメッセージを記録
  private String serverMsg;

  //自分がサーバかクライアントかを記録
  private final boolean isServer;

  //戦闘が始まったかどうか
  private boolean battleStart = false;

  //モンスター交代時の入力待ち
  private boolean changeMonsterWaiting = false;

  //deathChange: 自身のモンスター死亡時の交代選択待機
  private boolean deathChange = false;
  private JButton changeButton;
    
  private int monsterID, skillID, opponentMonsterID, opponentSkillID;

//str: serverのIPアドレス
  public MedimonController(MedimonModel m, TitleFrame t, boolean isServer, String str, int port) {
    title = t;
    model = m;
    this.isServer = isServer;
    if(isServer){
      sv = new CommServer(port);
    } else {
      cl = new CommClient(str, port);
    }
    title.getButton(1).addActionListener(this);
    title.getButton(2).addActionListener(this);
    title.getButton(3).addActionListener(this);
  }

  @Override
  public void actionPerformed(ActionEvent e){

    if(!battleStart){
      clip1.start();
      clip1.setFramePosition(0);
      monsterID = Integer.parseInt(e.getActionCommand());
      duelStart();
      openCommandWindow();
      return;
    }

    if(e.getSource() == changeButton){
      if((isServer && model.getRemaining(1) > 1) || 
        (!isServer && model.getRemaining(2) > 1)){
        clip2.start();
        clip2.setFramePosition(0);
        view.windowClose();
        openChangeWindow();
      } else {
        clip3.start();
        clip3.setFramePosition(0);
      }
      return;
    }

    if(deathChange){
      clip2.start();
      clip2.setFramePosition(0);
      monsterID = Integer.parseInt(e.getActionCommand());
      deathChange();
      try {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(new Runnable() {
          @Override
          public void run() { 
            openCommandWindow();
          }
        }, 2, TimeUnit.SECONDS);
      } catch (Exception en) {
        System.out.println(en);
      }
      return;
    } else {
      clip2.start();
      clip2.setFramePosition(0);
      if(changeMonsterWaiting){ //openChangeWindow();実行でtrueになる
        changeFrame.windowClose();
        System.out.println("ChangeWindow :close");
        monsterID = Integer.parseInt(e.getActionCommand());
        skillID = -1;
      } else {
        view.windowClose();
        System.out.println("commandWindow :close");
        
        skillID = Integer.parseInt(e.getActionCommand());
      }
    
      //サーバに送る
      sendAction(monsterID, skillID);
      System.out.println("sendAction: finish");
            
      //modelにデータ送信
      recvFromServer();
      System.out.println("recvFromServer: finish");
      setToModel();
      System.out.println("setToModel: finish");
    }

    //サーバ/クライアント両方の処理後、勝負継続か終了を判定
    System.out.println("commandWindow :conduct");
    try {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(new Runnable() {
          @Override
          public void run() { 
            duelJudge();
          }
        }, 5, TimeUnit.SECONDS);
      } catch (Exception en) {
        System.out.println(en);
      }
  }

    //サーバにデータを送るメソッド
  public void sendAction(int monsterIndex, int skillIndex){
    if(isServer){
      serverMsg = String.format("%d %d %d", monsterIndex, skillIndex, 1); //1 -> server
      System.out.println("send: " + serverMsg);
      sv.send(serverMsg);
    } else {
      serverMsg = String.format("%d %d %d", monsterIndex, skillIndex, 2); //2 ->client
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
      //clientInput = Integer.parseInt(msgArray[4]);
      System.out.println("recv: "+opponentMonsterID+" "+opponentSkillID
                          +" "+msgArray[2]);
    } else {
      String[] msgArray = (cl.recv()).split(" ");
      opponentMonsterID = Integer.parseInt(msgArray[0]);
      opponentSkillID = Integer.parseInt(msgArray[1]);
      //serverInput = Integer.parseInt(msgArray[4]);
      System.out.println("recv: "+opponentMonsterID+" "+opponentSkillID
                          +" "+msgArray[2]);
    }
  }

  public void firstSetToModel(){
    //setSkillのPlayer1にはserverの情報を、Player2にはclientの情報を入力するように実装しました
    if(isServer){
      System.out.println("setToModel: "+monsterID+" "+skillID+" "+opponentMonsterID+" "+opponentSkillID);
      model.firstSetSkill(monsterID, skillID, opponentMonsterID, opponentSkillID);
    } else {
      System.out.println("setToModel: "+opponentMonsterID+" "+opponentSkillID+" "+monsterID+" "+skillID);
      model.firstSetSkill(opponentMonsterID, opponentSkillID, monsterID, skillID);
    }
  }

    //ボタン入力から受け取った自分のmonster/skillIDと、メッセージから受け取った相手のmonster/skillIDをmodelにsetするメソッド
  public void setToModel(){
    //setSkillのPlayer1にはserverの情報を、Player2にはclientの情報を入力するように実装しました
    if(isServer){
      System.out.println("setToModel: "+monsterID+" "+skillID+" "+opponentMonsterID+" "+opponentSkillID);
      model.setSkill(monsterID, skillID, opponentMonsterID, opponentSkillID);
      if(model.getMedimon(1, model.getNowmedimon(1)).getSpeed() >= model.getMedimon(2, model.getNowmedimon(2)).getSpeed()){
        //serverのモンスターのスピードが速い場合
        tf = new TextFrame(model.getMedimon(1, model.getNowmedimon(1)), skillID);
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  tf.windowClose();
                  tf = new TextFrame(model.getMedimon(2, model.getNowmedimon(2)), opponentSkillID);
              }
          }, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  tf.windowClose();       
              }
          }, 4, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
      } else{
        //clientのモンスターのスピードが速い場合
        tf = new TextFrame(model.getMedimon(2, model.getNowmedimon(2)), opponentSkillID);
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  tf.windowClose();
                  tf = new TextFrame(model.getMedimon(1, model.getNowmedimon(1)), skillID);
              }
          }, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  tf.windowClose();
              }
          }, 4, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    } else {
      System.out.println("setToModel: "+opponentMonsterID+" "+opponentSkillID+" "+monsterID+" "+skillID);
      model.setSkill(opponentMonsterID, opponentSkillID, monsterID, skillID);

      if(model.getMedimon(1, model.getNowmedimon(1)).getSpeed() >= model.getMedimon(2, model.getNowmedimon(2)).getSpeed()){
        //serverのモンスターのスピードが速い場合
        //tf = new TextFrame(model.getMedimon(1, model.getNowmedimon(1)), skillID);
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  //tf.windowClose();
                  //tf = new TextFrame(model.getMedimon(2, model.getNowmedimon(2)), opponentSkillID);
              }
          }, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  //tf.windowClose();
              }
          }, 4, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
      } else{
        //clientのモンスターのスピードが速い場合
        //tf = new TextFrame(model.getMedimon(2, model.getNowmedimon(2)), opponentSkillID);
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  //tf.windowClose();
                  //tf = new TextFrame(model.getMedimon(1, model.getNowmedimon(1)), skillID);
              }
          }, 2, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
        try {
          final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
          executorService.schedule(new Runnable() {
              @Override
              public void run() {
                  //tf.windowClose();
              }
          }, 4, TimeUnit.SECONDS);
        } catch (Exception e) {
          System.out.println(e);
        }
      }
    }
  }

  public void duelStart(){
    skillID = -1;
    title.windowClose();
    sendAction(monsterID, skillID);
    recvFromServer();
    firstSetToModel();
    battle = new BattleFrame(isServer, model);
    battleStart = true;
  }

  //試合状況を監視する(勝敗or継続中)
  public void duelJudge(){
    if(isServer){
      System.out.println("serverHP: " + model.getMedimon(1, model.getNowmedimon(1)).getHp() + " clientHP: "
                          + model.getMedimon(2, model.getNowmedimon(2)).getHp());
      switch (model.gameSet()) {
        case 1:
          battle.stopClip();
          battle.windowClose();
          result = new ResultFrame(1);
          return;
        case 2:
          battle.stopClip();
          battle.windowClose();
          result = new ResultFrame(2);
          return;
        default:
          if(model.getMedimon(1, model.getNowmedimon(1)).getHp() == 0){
            clip4.start();
            clip4.setFramePosition(0);
            deathChange = true;
            openChangeWindow();
            return;
          } else if(model.getMedimon(2, model.getNowmedimon(2)).getHp() == 0){
            clip4.start();
            clip4.setFramePosition(0);
            System.out.println("server: deathChangeWaiting");
            recvFromServer();
            model.deathChange(2, opponentMonsterID);
            try {
              final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
              executorService.schedule(new Runnable() {
                @Override
                public void run() { 
                  openCommandWindow();
                }
              }, 2, TimeUnit.SECONDS);
            } catch (Exception en) {
              System.out.println(en);
            }
            return;
          }
      }
    } else {
      System.out.println("clientHP: " + model.getMedimon(2, model.getNowmedimon(2)).getHp() + " serverHP: "
                          + model.getMedimon(1, model.getNowmedimon(1)).getHp());
      switch (model.gameSet()) {
        case 2:
          battle.stopClip();
          battle.windowClose();
          result = new ResultFrame(1);
          return;
        case 1:
          battle.stopClip();
          battle.windowClose();
          result = new ResultFrame(2);
          return;
        default:
          //自身のモンスターのHPが0ならデスチェンジ、相手のモンスターのHPが0なら交代先が入力されるまで待機
          if(model.getMedimon(2, model.getNowmedimon(2)).getHp() == 0){
            clip4.start();
            clip4.setFramePosition(0);
            deathChange = true;
            openChangeWindow();
            return;
          } else if(model.getMedimon(1, model.getNowmedimon(1)).getHp() == 0){
            clip4.start();
            clip4.setFramePosition(0);
            System.out.println("client: deathChangeWaiting");
            recvFromServer();
            model.deathChange(1, opponentMonsterID);
            try {
              final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
              executorService.schedule(new Runnable() {
                @Override
                public void run() { 
                  openCommandWindow();
                }
              }, 2, TimeUnit.SECONDS);
            } catch (Exception en) {
              System.out.println(en);
            }
            return;
          }
      }
    }
    openCommandWindow();
  }

  public void openCommandWindow(){ 
    view = new CommandFrame(isServer, model);
    changeButton = view.getButton(-1);
    view.getButton(1).addActionListener(this);
    view.getButton(2).addActionListener(this);
    view.getButton(3).addActionListener(this);
    view.getButton(4).addActionListener(this);
    changeButton.addActionListener(this);
    changeMonsterWaiting = false;

    System.out.println("commandWindow :open");
  }

  public void openChangeWindow(){
    view.windowClose();
    System.out.println("commandWindow :close");
    changeFrame = new ChangeFrame(isServer, model);
    changeFrame.getButton(0).addActionListener(this);
    changeFrame.getButton(1).addActionListener(this);
    changeFrame.getButton(2).addActionListener(this);
  
    changeMonsterWaiting = true;
    System.out.println("ChangeWindow :open");
  }

  public void deathChange(){
    changeFrame.windowClose();
    if(isServer){
      model.deathChange(1, monsterID);
      sendAction(monsterID, skillID);
      System.out.println("server: deathChange");
    } else {
      model.deathChange(2, monsterID);
      sendAction(monsterID, skillID);
      System.out.println("client: deathChange");
    }
    deathChange = false;
  }
}

class FontLoader {
  public static void registerFont(String fontPath) {
      try {
          Font font = Font.createFont(Font.TRUETYPE_FONT, new File(fontPath));
          GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
          ge.registerFont(font);
      } catch (Exception e) {
          e.printStackTrace();
      }
  }
}

class TitleFrame extends JFrame{
  boolean server;
  MedimonModel mm;
  int me, you;
  Medimon nowmymedimon, nowyourmedimon;
  JLabel l1, l2, l3, l4, l5, l6, l7, l8;
  JButton b1, b2, b3;
  JPanel p1, p2, p3, p4, p5, p6, p7, p8, p9;
  Clip clip;
  public TitleFrame(boolean server, MedimonModel mm){
    FontLoader.registerFont("misaki_gothic.ttf");

    clip = mm.createClip(new File("start_bgm.wav"));
    clip.loop(Clip.LOOP_CONTINUOUSLY);

    this.server = server;
    if(server == true){
      this.me = 1; this.you = 2;
      setTitle("Server Mode");
    }else{
      this.me = 2; this.you = 1;
      setTitle("Client Mode");
    }
    this.mm = mm;

    setNowMedimon(this.mm);

    this.setLayout(new GridLayout(2, 1));

    p1=new JPanel(); p2=new JPanel();
    p2.setLayout(new BorderLayout());
    this.add(p1); this.add(p2);

    p3=new JPanel();
    p3.setLayout(new GridLayout(1, 3));
    p2.add(p3, BorderLayout.CENTER);

    p4=new JPanel(); p5=new JPanel(); p6=new JPanel();
    p4.setLayout(new BorderLayout()); p5.setLayout(new BorderLayout()); p6.setLayout(new BorderLayout());
    p3.add(p4); p3.add(p5); p3.add(p6);

    p7=new JPanel(); p8=new JPanel(); p9=new JPanel();
    p7.setLayout(new GridLayout(2, 1)); p8.setLayout(new GridLayout(2, 1)); p9.setLayout(new GridLayout(2, 1));
    p4.add(p7, BorderLayout.CENTER); p5.add(p8, BorderLayout.CENTER); p6.add(p9, BorderLayout.CENTER);

    l1=new JLabel();
    p1.add(l1);
    l1.setText("<html><img src='file:TitleLogo.png' width=600 height=400></html>");
    l1.setHorizontalAlignment(JLabel.CENTER);
    l1.setVerticalAlignment(JLabel.CENTER);
    l2=new JLabel();
    p2.add(l2, BorderLayout.NORTH);
    l2.setHorizontalAlignment(JLabel.CENTER);
    l2.setVerticalAlignment(JLabel.CENTER);
    l2.setText("<html><center><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>さいしょにたたかわせるモンスターをえらんでください<br></span><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>（モンスターにカーソルをあわせると、つかえるわざがみれます）<br><br></span></center></html>"); //自分側のモンスターの名前
    
    l3=new JLabel(); l4=new JLabel(); l5=new JLabel();
    p7.add(l3); p8.add(l4); p9.add(l5);
    l3.setText("<html><img src='file:" + setMonsterImage('c', mm.getMedimon(me, 0).getName()) + "' width=150 height=150></html>");
    l3.setHorizontalAlignment(JLabel.CENTER);
    l3.setToolTipText("<html><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" 
    + mm.getMedimon(me, 0).getSkill(0).getSkillname() + ": " + SkillEffect(me, mm, 0, 0) + "<br>"
    + mm.getMedimon(me, 0).getSkill(1).getSkillname() + ": " + SkillEffect(me, mm, 0, 1) + "<br>"
    + mm.getMedimon(me, 0).getSkill(2).getSkillname() + ": " + SkillEffect(me, mm, 0, 2) + "<br>"
    + mm.getMedimon(me, 0).getSkill(3).getSkillname() + ": " + SkillEffect(me, mm, 0, 3)
    + "</span></html>");
    l4.setText("<html><img src='file:" + setMonsterImage('c', mm.getMedimon(me, 1).getName()) + "' width=150 height=150></html>");
    l4.setHorizontalAlignment(JLabel.CENTER);
    l4.setToolTipText("<html><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" 
    + mm.getMedimon(me, 1).getSkill(0).getSkillname() + ": " + SkillEffect(me, mm, 1, 0) + "<br>"
    + mm.getMedimon(me, 1).getSkill(1).getSkillname() + ": " + SkillEffect(me, mm, 1, 1) + "<br>"
    + mm.getMedimon(me, 1).getSkill(2).getSkillname() + ": " + SkillEffect(me, mm, 1, 2) + "<br>"
    + mm.getMedimon(me, 1).getSkill(3).getSkillname() + ": " + SkillEffect(me, mm, 1, 3)
    + "</span></html>");
    l5.setText("<html><img src='file:" + setMonsterImage('c', mm.getMedimon(me, 2).getName()) + "' width=150 height=150></html>");
    l5.setHorizontalAlignment(JLabel.CENTER);
    l5.setToolTipText("<html><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" 
    + mm.getMedimon(me, 2).getSkill(0).getSkillname() + ": " + SkillEffect(me, mm, 2, 0) + "<br>"
    + mm.getMedimon(me, 2).getSkill(1).getSkillname() + ": " + SkillEffect(me, mm, 2, 1) + "<br>"
    + mm.getMedimon(me, 2).getSkill(2).getSkillname() + ": " + SkillEffect(me, mm, 2, 2) + "<br>"
    + mm.getMedimon(me, 2).getSkill(3).getSkillname() + ": " + SkillEffect(me, mm, 2, 3)
    + "</span></html>");

    l6=new JLabel(); l7=new JLabel(); l8=new JLabel();
    l6.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" 
    + mm.getMedimon(me, 0).getName() + "</span><span style='font-family:MisakiGothic; font-size:25pt; color:black;'>"
    + "<br>HP: " + mm.getMedimon(me, 0).getMaxHp() 
    + "<br>はやさ: " + mm.getMedimon(me, 0).getSpeed() 
    + "<br>ぼうぎょ: " + mm.getMedimon(me, 0).getBougyo() + "</span></html>");
    l7.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" 
    + mm.getMedimon(me, 1).getName() + "</span><span style='font-family:MisakiGothic; font-size:25pt; color:black;'>" 
    + "<br>HP: " + mm.getMedimon(me, 1).getMaxHp() 
    + "<br>はやさ: " + mm.getMedimon(me, 1).getSpeed() 
    + "<br>ぼうぎょ: " + mm.getMedimon(me, 1).getBougyo() + "</span></html>");
    l8.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" 
    + mm.getMedimon(me, 2).getName() + "</span><span style='font-family:MisakiGothic; font-size:25pt; color:black;'>"
    + "<br>HP: " + mm.getMedimon(me, 2).getMaxHp() 
    + "<br>はやさ: " + mm.getMedimon(me, 2).getSpeed() 
    + "<br>ぼうぎょ: " + mm.getMedimon(me, 2).getBougyo() + "</span></html>");
    p7.add(l6); p8.add(l7); p9.add(l8);

    b1=new JButton();
    b1.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>えらぶ</span></html>");
    b2=new JButton();
    b2.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>えらぶ</span></html>");
    b3=new JButton();
    b3.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>えらぶ</span></html>");
    b1.setActionCommand("0");
    b2.setActionCommand("1");
    b3.setActionCommand("2");
    p4.add(b1, BorderLayout.SOUTH); p5.add(b2, BorderLayout.SOUTH); p6.add(b3, BorderLayout.SOUTH);

    this.setSize(1000, 900); 
    this.setLocation(460, 140);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }

  public void setNowMedimon(MedimonModel mm){
    this.nowmymedimon = mm.getMedimon(me, mm.getNowmedimon(me));
    this.nowyourmedimon = mm.getMedimon(you, mm.getNowmedimon(you));
  }

  public String setMonsterImage(char s, String name){
    if(s == 'b'){
      if(name == "トーグ"){return "Thogg_b.png";}
      if(name == "ウチャ"){return "Ucha_b.png";}
      if(name == "ミーゴ"){return "Mi-go_b.png";}
    }else if(s == 'f'){
      if(name == "トーグ"){return "Thogg_f.png";}
      if(name == "ウチャ"){return "Ucha_f.png";}
      if(name == "ミーゴ"){return "Mi-go_f.png";}
    }else if(s == 'c'){
      if(name == "トーグ"){return "Thogg_change.png";}
      if(name == "ウチャ"){return "Ucha_change.png";}
      if(name == "ミーゴ"){return "Mi-go_change.png";}
    }
    return "\0";
  }

  public String SkillEffect(int i, MedimonModel mm, int index, int n){
    if(mm.getMedimon(i, index).getSkill(n).getStatusTarget() == 'h'){
      if(mm.getMedimon(i, index).getSkill(n).getValue() < 0){
        return Math.abs(mm.getMedimon(i, index).getSkill(n).getValue()) + "ダメージ";
      }else{
        return mm.getMedimon(i, index).getSkill(n).getValue() + "かいふく";
      }
    }else if(mm.getMedimon(i, index).getSkill(n).getStatusTarget() == 'b'){
      return "ぼうぎょ" + (mm.getMedimon(i, index).getSkill(n).getValue() - 100) + "％アップ";
    }else if(mm.getMedimon(i, index).getSkill(n).getStatusTarget() == 's'){
      return "はやさ" + (mm.getMedimon(i, index).getSkill(n).getValue() - 100) + "％アップ";
    }
    return "\0";
  }

  public JButton getButton(int i){
    if     (i==1){return b1;}
    else if(i==2){return b2;}
    else         {return b3;}
  }

  public void windowClose(){
    clip.close();
    System.out.println("OK");
    this.dispose();
  }
}

class BackgroundPanel extends JPanel {
  private Image backgroundImage;

  public BackgroundPanel(String filePath) {
      try {
          backgroundImage = ImageIO.read(new File(filePath));
      } catch (IOException e) {
          e.printStackTrace();
      }
  }

  @Override
  protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (backgroundImage != null) {
          g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
      }
  }
}

class BattleFrame extends JFrame implements ActionListener{
  private Timer timer;
  boolean server;
  int judge;
  MedimonModel mm;
  int me, you;
  Medimon nowmymedimon, nowyourmedimon;
  JLabel l1, l2, l3, l4, l5, l6, l7;
  JProgressBar bar1, bar2;
  JPanel p1, p2, p6, p7;
  Clip clip;
  public BattleFrame(boolean server, MedimonModel mm){
  FontLoader.registerFont("misaki_gothic.ttf");
  judge = 0;

  clip = mm.createClip(new File("bgm.wav"));
  clip.loop(Clip.LOOP_CONTINUOUSLY);

  this.server = server;
  if(server == true){
    this.me = 1; this.you = 2;
  }else{
    this.me = 2; this.you = 1;
  }
  this.mm = mm;

  BackgroundPanel backgroundPanel = new BackgroundPanel("Background.png");
  backgroundPanel.setLayout(new BorderLayout());

  p1=new JPanel(); p2=new JPanel(); p6=new JPanel(); p7=new JPanel();

	l1=new JLabel(); l2=new JLabel(); l3=new JLabel(); l4=new JLabel(); l5=new JLabel(); l6=new JLabel(); l7=new JLabel(); 
  l1.setHorizontalAlignment(JLabel.CENTER);
  l2.setHorizontalAlignment(JLabel.LEFT);
  l3.setHorizontalAlignment(JLabel.CENTER);
  l4.setHorizontalAlignment(JLabel.RIGHT);
  l5.setHorizontalAlignment(JLabel.CENTER);
  l6.setHorizontalAlignment(JLabel.LEFT);
  l7.setHorizontalAlignment(JLabel.RIGHT);
  l1.setText("<html><img src='file:Center.png' width=50 height=200></html>");

  setNowMedimon(this.mm);
	l2.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + this.nowmymedimon.getName() + "</span></html>"); //自分側のモンスターの名前
  l3.setText("<html><img src='file:" + setMonsterImage('b', this.nowmymedimon.getName()) + "' width=400 height=400></html>"); //自分側のモンスターの画像
	l4.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + this.nowyourmedimon.getName() + "</span></html>"); //相手側のモンスターの名前
  l5.setText("<html><img src='file:" + setMonsterImage('f', this.nowyourmedimon.getName()) + "' width=400 height=400></html>"); //相手側のモンスターの画像
	l6.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + this.nowmymedimon.getHp() + "/" + nowmymedimon.getMaxHp() + "</span></html>"); //自分側のモンスターのHP
  l7.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + this.nowyourmedimon.getHp() + "/" + nowyourmedimon.getMaxHp() + "</span></html>"); //相手側のモンスターのHP

	bar1 = new JProgressBar();
	bar2 = new JProgressBar();
  bar1.setPreferredSize(new Dimension(10, 4));
  bar1.setForeground(Color.GREEN);
  bar2.setForeground(Color.GREEN);
  
  bar1.setMinimum(0); bar1.setMaximum(this.nowmymedimon.getMaxHp());
	bar1.setValue(this.nowmymedimon.getHp());
  bar2.setMinimum(0); bar2.setMaximum(this.nowyourmedimon.getMaxHp());
	bar2.setValue(this.nowyourmedimon.getHp());
  
  if(me==1){
    this.setTitle("Server Mode");
  }else{
    this.setTitle("Client Mode");
  }

  p1.setLayout(new BorderLayout());
  p2.setLayout(new BorderLayout());
	p6.setLayout(new GridLayout(4,1));
	p7.setLayout(new GridLayout(4,1));
  p1.setOpaque(false); p2.setOpaque(false); p6.setOpaque(false); p7.setOpaque(false);

	p1.add(p6, BorderLayout.NORTH);
	p2.add(p7, BorderLayout.NORTH);

  this.add(backgroundPanel);

  backgroundPanel.add(p1, BorderLayout.WEST);
  backgroundPanel.add(p2, BorderLayout.EAST);
  backgroundPanel.add(l1, BorderLayout.CENTER);

	p1.add(l3,BorderLayout.CENTER);
	p2.add(l5,BorderLayout.CENTER);
  p6.add(new JLabel());
	p6.add(l2);
	p6.add(bar1);
	p6.add(l6);
  p7.add(new JLabel());
	p7.add(l4);
	p7.add(bar2);
	p7.add(l7);
  
  this.setSize(1000, 600); 
  this.setLocation(460, 140);
  this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
  this.setVisible(true);

  timer = new Timer(1, this); // 1ミリ秒毎にactionPerformedを呼び出し
  timer.start(); // stopメソッドを呼ぶまでactionPerformedが呼ばれ続ける
  }

  public void setNowMedimon(MedimonModel mm){
    this.nowmymedimon = mm.getMedimon(me, mm.getNowmedimon(me));
    this.nowyourmedimon = mm.getMedimon(you, mm.getNowmedimon(you));
  }

  public String setMonsterImage(char s, String name){
    if(s == 'b'){
      if(name == "トーグ"){return "Thogg_b.png";}
      if(name == "ウチャ"){return "Ucha_b.png";}
      if(name == "ミーゴ"){return "Mi-go_b.png";}
    }else if(s == 'f'){
      if(name == "トーグ"){return "Thogg_f.png";}
      if(name == "ウチャ"){return "Ucha_f.png";}
      if(name == "ミーゴ"){return "Mi-go_f.png";}
    }else if(s == 'c'){
      if(name == "トーグ"){return "Thogg_change.png";}
      if(name == "ウチャ"){return "Ucha_change.png";}
      if(name == "ミーゴ"){return "Mi-go_change.png";}
    }
    return "\0";
  }

  public void actionPerformed(ActionEvent e){
    setNowMedimon(this.mm);
	  l2.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + this.nowmymedimon.getName() + "</span></html>"); //自分側のモンスターの名前
    l3.setText("<html><img src='file:" + setMonsterImage('b', this.nowmymedimon.getName()) + "' width=400 height=400></html>"); //自分側のモンスターの画像
	  l4.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + this.nowyourmedimon.getName() + "</span></html>"); //相手側のモンスターの名前
    l5.setText("<html><img src='file:" + setMonsterImage('f', this.nowyourmedimon.getName()) + "' width=400 height=400></html>"); //相手側のモンスターの画像
	  l6.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + this.nowmymedimon.getHp() + "/" + nowmymedimon.getMaxHp() + "</span></html>"); //自分側のモンスターのHP
    bar1.setMinimum(0); bar1.setMaximum(this.nowmymedimon.getMaxHp());
    bar1.setValue(this.nowmymedimon.getHp());
	  l7.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + this.nowyourmedimon.getHp() + "/" + nowyourmedimon.getMaxHp() + "</span></html>"); //相手側のモンスターのHP
    bar2.setMinimum(0); bar2.setMaximum(this.nowyourmedimon.getMaxHp());
    bar2.setValue(this.nowyourmedimon.getHp());
  }

  public void stopClip(){
    clip.stop();
  }

  public void windowClose(){
    System.out.println("OK");
    this.dispose();
  }
}

class CommandFrame extends JFrame{
  JButton b1, b2, b3, b4, change;
  public CommandFrame(boolean server, MedimonModel mm){
    FontLoader.registerFont("misaki_gothic.ttf");
    JPanel p1=new JPanel(),p2=new JPanel();
    int me;
    if(server == true){me = 1;}
    else{me = 2;}
    b1=new JButton("<html><center><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getSkill(me,0).getSkillname() + "</span><br><br><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" + SkillEffect(me, mm, 0) + "</span></center></html>"); 
    b2=new JButton("<html><center><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getSkill(me,1).getSkillname() + "</span><br><br><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" + SkillEffect(me, mm, 1) + "</span></center></html>");
    b3=new JButton("<html><center><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getSkill(me,2).getSkillname() + "</span><br><br><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" + SkillEffect(me, mm, 2) + "</span></center></html>");
    b4=new JButton("<html><center><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getSkill(me,3).getSkillname() + "</span><br><br><span style='font-family:MisakiGothic; font-size:20pt; color:black;'>" + SkillEffect(me, mm, 3) + "</span></center></html>");
    change=new JButton("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>こうたい</span></html>");
    b1.setActionCommand("0");
    b2.setActionCommand("1");
    b3.setActionCommand("2");
    b4.setActionCommand("3");

    if(me==1){
      this.setTitle("Server Mode");
    }else{
      this.setTitle("Client Mode");
    }

    this.setLayout(new GridLayout(1,3));

    this.add(p1);
    this.add(p2);
    this.add(change);

    p1.setLayout(new GridLayout(2,1));
    p2.setLayout(new GridLayout(2,1));

    p1.add(b1); p1.add(b2); p2.add(b3); p2.add(b4);
   
    this.setSize(1000,300);
    this.setLocation(460, 740);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.setVisible(true);
  }

  public void windowClose(){
    System.out.println("OK");
    this.dispose();
  }

  public String SkillEffect(int i, MedimonModel mm, int n){
    if(mm.getSkill(i, n).getStatusTarget() == 'h'){
      if(mm.getSkill(i, n).getValue() < 0){
        return Math.abs(mm.getSkill(i, n).getValue()) + "ダメージ";
      }else{
        return mm.getSkill(i, n).getValue() + "かいふく";
      }
    }else if(mm.getSkill(i, n).getStatusTarget() == 'b'){
      return "ぼうぎょ" + (mm.getSkill(i, n).getValue() - 100) + "％アップ";
    }else if(mm.getSkill(i, n).getStatusTarget() == 's'){
      return "はやさ" + (mm.getSkill(i, n).getValue() - 100) + "％アップ";
    }
    return "\0";
  }

  public JButton getButton(int i){
    if(i==1){return b1;}
    else if(i==2){return b2;}
    else if(i==3){return b3;}
    else if(i==4){return b4;}
    else{return change;}
  }
}

class ChangeFrame extends JFrame{
  JButton change1, change2, change3;
  JLabel l1, l2, l3, l4, l5, l6, l7, l8, l9;
  public ChangeFrame(boolean server, MedimonModel mm){
    FontLoader.registerFont("misaki_gothic.ttf");
    JPanel p1=new JPanel(),p2=new JPanel(), p3=new JPanel(), p4=new JPanel(), p5=new JPanel(), p6=new JPanel();
    int me;
    if(server == true){me = 1;}
    else{me = 2;}
    // JPanelに貼り付けるためのJButtonを5つ生成
    change1=new JButton("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>こうたい</span></html>"); 
    change2=new JButton("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>こうたい</span></html>"); 
    change3=new JButton("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>こうたい</span></html>");
    change1.setActionCommand("0");
    change2.setActionCommand("1");
    change3.setActionCommand("2");

    l1=new JLabel(); l2=new JLabel(); l3=new JLabel(); l4=new JLabel(); l5=new JLabel(); l6=new JLabel(); l7=new JLabel(); l8=new JLabel(); l9=new JLabel();
    l1.setText("<html><img src='file:" + setMonsterImage('c', mm.getMedimon(me, 0).getName()) + "' width=150 height=150></html>");
    l2.setText("<html><img src='file:" + setMonsterImage('c', mm.getMedimon(me, 1).getName()) + "' width=150 height=150></html>");
    l3.setText("<html><img src='file:" + setMonsterImage('c', mm.getMedimon(me, 2).getName()) + "' width=150 height=150></html>");
    l4.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getMedimon(me, 0).getName() +  "</span></html>");
    l5.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + mm.getMedimon(me, 0).getHp() + "/" + mm.getMedimon(me, 0).getMaxHp() + "</span></html>");
    l6.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getMedimon(me, 1).getName() +  "</span></html>");
    l7.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + mm.getMedimon(me, 1).getHp() + "/" + mm.getMedimon(me, 1).getMaxHp() + "</span></html>");
    l8.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + mm.getMedimon(me, 2).getName() +  "</span></html>");
    l9.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>HP " + mm.getMedimon(me, 2).getHp() + "/" + mm.getMedimon(me, 2).getMaxHp() + "</span></html>");
    
    l4.setHorizontalAlignment(JLabel.LEFT);
    l5.setHorizontalAlignment(JLabel.LEFT);
    l6.setHorizontalAlignment(JLabel.LEFT);
    l7.setHorizontalAlignment(JLabel.LEFT);
    l8.setHorizontalAlignment(JLabel.LEFT);
    l9.setHorizontalAlignment(JLabel.LEFT);

    if(me==1){
      this.setTitle("Server Mode");
    }else{
      this.setTitle("Client Mode");
    }

    this.setLayout(new GridLayout(3,1));

    p1.setLayout(new GridLayout(1,3));
    p2.setLayout(new GridLayout(1,3));
    p3.setLayout(new GridLayout(1,3));

    p4.setLayout(new GridLayout(2,1));
    p5.setLayout(new GridLayout(2,1));
    p6.setLayout(new GridLayout(2,1));
    p4.add(l4); p4.add(l5); p5.add(l6); p5.add(l7); p6.add(l8); p6.add(l9); 

    p1.add(l1); p1.add(p4); p1.add(change1);
    p2.add(l2); p2.add(p5); p2.add(change2);
    p3.add(l3); p3.add(p6); p3.add(change3);

    if(mm.getNowmedimon(me) != 0 && mm.getMedimon(me, 0).getHp() != 0){this.add(p1);}
    if(mm.getNowmedimon(me) != 1 && mm.getMedimon(me, 1).getHp() != 0){this.add(p2);}
    if(mm.getNowmedimon(me) != 2 && mm.getMedimon(me, 2).getHp() != 0){this.add(p3);}

    this.setSize(1000, 600); 
    this.setLocation(460, 140);
    this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    this.setVisible(true);
  }

  public void windowClose(){
    System.out.println("OK");
    this.dispose();
  }

  public JButton getButton(int i){
    if(i==0){return change1;}
    else if(i==1){return change2;}
    else if(i==2){return change3;}
    return null;
  }

  public String setMonsterImage(char s, String name){
    if(s == 'b'){
      if(name == "トーグ"){return "Thogg_b.png";}
      if(name == "ウチャ"){return "Ucha_b.png";}
      if(name == "ミーゴ"){return "Mi-go_b.png";}
    }else if(s == 'f'){
      if(name == "トーグ"){return "Thogg_f.png";}
      if(name == "ウチャ"){return "Ucha_f.png";}
      if(name == "ミーゴ"){return "Mi-go_f.png";}
    }else if(s == 'c'){
      if(name == "トーグ"){return "Thogg_change.png";}
      if(name == "ウチャ"){return "Ucha_change.png";}
      if(name == "ミーゴ"){return "Mi-go_change.png";}
    }
    return "\0";
  }
}

class TextFrame extends JFrame{
  JPanel p1;
  JLabel l1, l2;
  public TextFrame(Medimon medimon, int skillID){
    FontLoader.registerFont("misaki_gothic.ttf");
    this.setTitle("Text");

    p1=new JPanel();
    p1.setLayout(new GridLayout(4,1));
    this.add(p1);

    l1=new JLabel(); l2=new JLabel();
    if(skillID != -1){
      l1.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + medimon.getName() + "　の　" + medimon.getSkill(skillID).getSkillname() + "　！</span></html>");
      l2.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>" + SkillEffect(medimon.getSkill(skillID)) + "　！</span></html>");
    }else{
      l1.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;>" + medimon.getName() + "　もどれ！</span></html>");
    }
    p1.add(l1); p1.add(l2);
    p1.add(new JLabel()); p1.add(new JLabel());
    
    this.setSize(1000,300);
    this.setLocation(360, 740);
    this.setUndecorated(true);
    this.setVisible(true);
  }

  public String SkillEffect(Skill skill){
    if(skill.getStatusTarget() == 'h'){
      if(skill.getValue() < 0){
        return Math.abs(skill.getValue()) + "ダメージ";
      }else{
        return skill.getValue() + "かいふく";
      }
    }else if(skill.getStatusTarget() == 'b'){
      return "ぼうぎょ" + (skill.getValue() - 100) + "％アップ";
    }else{
      return "はやさ" + (skill.getValue() - 100) + "％アップ";
    }
  }

  public void windowClose(){
    System.out.println("OK");
    this.dispose();
  }
}

class ResultFrame extends JFrame{
  JPanel p1, p2, p3;
  JLabel l1, l2, l3;
  public ResultFrame(int i){
    FontLoader.registerFont("misaki_gothic.ttf");
    this.setTitle("Result");

    p1=new JPanel();
    p1.setLayout(new BorderLayout());
    this.add(p1);
    p2=new JPanel(); 
    p2.setLayout(new GridLayout(2, 1));
    p3=new JPanel(); 
    p3.setLayout(new GridLayout(2, 1));
    p1.add(p2, BorderLayout.NORTH); p1.add(p3, BorderLayout.SOUTH);

    l1=new JLabel(); 
    l1.setHorizontalAlignment(JLabel.CENTER); l1.setVerticalAlignment(JLabel.CENTER);
    if(i == 1){
      l1.setText("<html><span style='font-family:MisakiGothic; font-size:100pt; color:black;'>YOU WIN!</span></html>");
      System.out.println("YOU WIN!");
    } else {
      l1.setText("<html><span style='font-family:MisakiGothic; font-size:100pt; color:black;'>YOU LOSE...</span></html>");
      System.out.println("YOU LOSE...");
    }
    p2.add(new JLabel()); p2.add(l1);

    l2=new JLabel();
    l2.setText("<html><img src='file:TitleLogo.png' width=600 height=400></html>");
    l2.setHorizontalAlignment(JLabel.CENTER); l2.setVerticalAlignment(JLabel.NORTH);
    p1.add(l2, BorderLayout.CENTER);

    l3=new JLabel();
    l3.setText("<html><span style='font-family:MisakiGothic; font-size:30pt; color:black;'>Ⅹに ＃メディモン でポストしてね</span></html>");
    l3.setHorizontalAlignment(JLabel.CENTER); l3.setVerticalAlignment(JLabel.NORTH);
    p3.add(l3); p3.add(new JLabel());

    this.setSize(1000, 900); 
    this.setLocation(460, 140);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setVisible(true);
  }

  public void windowClose(){
    System.out.println("OK");
    this.dispose();
  }
}

//localhost: server側のIPアドレスをコマンドライン第一引数に入力して起動
public class MedimonGame {
  public static void main(String[] args) {
    String str;
    boolean server=false;
    int port;
    if (args.length<2){
      System.out.println("Usage : java MedimonGame {server/{IP adress}/{localhost}} {port no.} \n");     
      System.exit(1);
    }
    MedimonModel mm;
    TitleFrame mv0;
    //BattleFrame mv1;
    MedimonController mc;
    //CommandFrame mv2;
    if (args[0].equals("server")){
      server=true;
      port = Integer.parseInt(args[1]);
      System.out.println("Server mode");
      str="server";
      mm = new MedimonModel();
      mv0 = new TitleFrame(server, mm);
      //mv1 = new BattleFrame(server, mm);
      //mv2 = new CommandFrame(server, mm);
      mc = new MedimonController(mm, mv0, server, str, port);
    }else{
      server=false;
      port = Integer.parseInt(args[1]);
      System.out.println("Client mode");
      str=args[0];
      mm = new MedimonModel();
      mv0 = new TitleFrame(server, mm);
      //mv1 = new BattleFrame(server, mm);
      //mv2 = new CommandFrame(server, mm);
      mc = new MedimonController(mm, mv0, server, str, port);
    }
    
  }
}
