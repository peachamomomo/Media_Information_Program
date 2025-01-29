import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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

class Skill {
    private Character x;
    private int n, type;
    private String name;
    public Character getStatusTarget() { return x; }
    public void setStatusTarget(Character x) { this.x = x; }
    public int getValue() { return n; }
    public void setValue(int n) { this.n = n; }
    public int getType() { return type; }
    public void setType(int type) { this.type = type; }
    public String getSkillname() { return name; }
    public void setSkillname(String name) { this.name = name; }
}

class Medimon {
    private int H, h, s, b;  //体力、速さ、防御
    private ArrayList<Skill> a;  //技
    private int type;  //技のタイプ
    private String n;  //名前

    public Medimon(int h, int s, int b, int type, Skill s1, Skill s2, Skill s3, Skill s4, String n){
        this.H = h; this.h = h; this.s = s; this.b = b; this.type = type; this.n = n;
        this.a = new ArrayList<Skill>();
        a.add(s1);
        a.add(s2);
        a.add(s3);
        a.add(s4);
    }

    //体力の減少・増加
    public void changeHp(int n, int type){
        if(n > 0) {
            h = h + n;
            if(h >= H) h = H;
        } else {
            if((type+1)%3 == this.type) {
                h = h + (int)(n*(150-b)*2/(float)100);
            } else if(type == this.type) {
                h = h + (int)(n*(150-b)/(float)100);
            } else {
                h = h + (int)(n*(150-b)*0.5/(float)100);
            }
            if(h < 0) h = 0;
        } 
    }

    //速さの減少・増加
    public void changeSpeed(int n){
        Float x = n / (float)100;
        s = (int)(s * x);
    }

    //防御の減少・増加
    public void changeBougyo(int n){
        Float x = n / (float)100;
        b = (int)(b * x);
    } 

    public int getMaxHp() { return H; }
    public int getHp() { return h; }
    public int getSpeed() { return s; }
    public int getBougyo() { return b; }
    public int getType() { return type; }
    public Skill getSkill(int index) { return a.get(index); }
    public String getName() { return n; }
}

class MedimonModel {
    private ArrayList<Medimon> allmedimon, all1medimon, all2medimon;       // 1がServer、２がClient
    private int now1medimon = 0, now2medimon = 0;
    private int h, s, b, type; 
    private Skill s1, s2, s3, s4;
    private String n;
    public Clip clip1, clip2, clip3, clip4, clip5, clip6;
    public MedimonModel() {
        this.allmedimon = new ArrayList<Medimon>();
        h = 100; s = 50; b = 50;  type = 0; n = "トーグ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setType(0); s1.setSkillname("パンチ");
        s2.setStatusTarget('b'); s2.setValue(120); s2.setType(0); s2.setSkillname("ぼうぎょ");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setType(0); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(20); s4.setType(0); s4.setSkillname("ねむる");
        Medimon m1 = new Medimon(h, s, b, type, s1, s2, s3, s4, n);
        allmedimon.add(m1);
        h = 10; s = 55; b = 50;  type = 1;  n = "ウチャ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-40); s1.setType(0); s1.setSkillname("かえん");
        s2.setStatusTarget('s'); s2.setValue(120); s2.setType(0); s2.setSkillname("炎の舞");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setType(0); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setType(0); s4.setSkillname("かいふく");
        m1 = new Medimon(h, s, b, type, s1, s2, s3, s4, n);
        allmedimon.add(m1);
        h = 10; s = 50; b = 50;  type = 0;  n = "ミーゴ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setType(0); s1.setSkillname("パンチ");
        s2.setStatusTarget('h'); s2.setValue(-40); s2.setType(0); s2.setSkillname("キック");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setType(0); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setType(0); s4.setSkillname("かいふく");
        m1 = new Medimon(h, s, b, type, s1, s2, s3, s4, n);
        allmedimon.add(m1);
        h = 10; s = 50; b = 50;  type = 0;  n = "ヨギー";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setType(0); s1.setSkillname("パンチ");
        s2.setStatusTarget('h'); s2.setValue(-40); s2.setType(0); s2.setSkillname("キック");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setType(0); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setType(0); s4.setSkillname("かいふく");
        m1 = new Medimon(h, s, b, type, s1, s2, s3, s4, n);
        allmedimon.add(m1);
        this.all1medimon = new ArrayList<Medimon>();
        for(int i=0; i<3; i++) {
            h = allmedimon.get(i).getHp(); s = allmedimon.get(i).getSpeed(); b = allmedimon.get(i).getBougyo();  
            type = allmedimon.get(i).getType(); n = allmedimon.get(i).getName();
            s1 = allmedimon.get(i).getSkill(0); s2 = allmedimon.get(i).getSkill(1);
            s3 = allmedimon.get(i).getSkill(2); s4 = allmedimon.get(i).getSkill(3);
            m1 = new Medimon(h, s, b, type, s1, s2, s3, s4, n);
            all1medimon.add(m1);
        }
        this.all2medimon = new ArrayList<Medimon>();
        for(int i=0; i<3; i++) {
            h = allmedimon.get(i).getHp(); s = allmedimon.get(i).getSpeed(); b = allmedimon.get(i).getBougyo();  
            type = allmedimon.get(i).getType(); n = allmedimon.get(i).getName();
            s1 = allmedimon.get(i).getSkill(0); s2 = allmedimon.get(i).getSkill(1);
            s3 = allmedimon.get(i).getSkill(2); s4 = allmedimon.get(i).getSkill(3);
            m1 = new Medimon(h, s, b, type, s1, s2, s3, s4, n);
            all2medimon.add(m1);
        }
    }

    public Medimon getMedimon(int Player, int Index) {
        if(Player == 1) {                            // Server
            return all1medimon.get(Index);
        } else {                            // Client
            return all2medimon.get(Index);
        }
    }

    public void firstSetSkill(int Player1MonsterID, int Player1SkillID, int Player2MonsterID, int Player2SkillID) {
        clip6 = createClip(new File("Koutai.wav"));
        p1Change(Player1MonsterID);
        p2Change(Player2MonsterID);
    }

    public void setSkill(int Player1MonsterID, int Player1SkillID, int Player2MonsterID, int Player2SkillID) { //二人の行動を受け取る
        clip1 = createClip(new File("Attack.wav"));
        clip2 = createClip(new File("Heal.wav"));
        clip3 = createClip(new File("Debuf.wav"));
        clip4 = createClip(new File("Speed.wav"));
        clip5 = createClip(new File("Bougyo.wav"));
        clip6 = createClip(new File("Koutai.wav"));
        if(Player1SkillID < 0) {
            try {
                final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        p1Change(Player1MonsterID);                       
                    }
                }, 2, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println(e);
            }
            if(Player2SkillID < 0) {
                try {
                    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                    executorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            p2Change(Player2MonsterID);                       
                        }
                    }, 4, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.out.println(e);
                }
            } else {
                try {
                    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                    executorService.schedule(new Runnable() {
                        @Override
                        public void run() { 
                            p2Skill(Player1SkillID, Player2SkillID);
                        }
                    }, 4, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } else if(Player2SkillID < 0) {
            try {
                final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        p2Change(Player2MonsterID);                       
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
                        p1Skill(Player1SkillID, Player2SkillID);                       
                    }
                }, 4, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println(e);
            }
        } else {   
            Medimon p1 = all1medimon.get(now1medimon);
            Medimon p2 = all2medimon.get(now2medimon);
            try {
                final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if(p1.getSpeed() >= p2.getSpeed()) {  
                            p1Skill(Player1SkillID, Player2SkillID);
                        } else {
                            p2Skill(Player1SkillID, Player2SkillID);
                        }
                    }
                }, 2, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println(e);
            }
            if(p1.getHp()!=0 && p2.getHp()!=0) {
                try {
                    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                    executorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            if(p1.getSpeed() >= p2.getSpeed()) {  
                                p2Skill(Player1SkillID, Player2SkillID);
                            }  else {
                                p1Skill(Player1SkillID, Player2SkillID);
                            } 
                        }
                    }, 4, TimeUnit.SECONDS);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            try {
                final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                executorService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        CloseClip();
                    }
                }, 6, TimeUnit.SECONDS);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    public void p1Skill (int Player1SkillID, int Player2SkillID) {
        Medimon p1 = all1medimon.get(now1medimon);
        Medimon p2 = all2medimon.get(now2medimon);

        if(p1.getHp()!=0 && p2.getHp()!=0) {
            Skill sp1 = p1.getSkill(Player1SkillID);
            if(sp1.getStatusTarget() == 'h') {
                if(sp1.getValue() < 0) {
                    p2.changeHp(sp1.getValue(), sp1.getType());
		            clip1.start();
                    clip1.setFramePosition(0);
                } else {
                    p1.changeHp(sp1.getValue(), sp1.getType());
		            clip2.start();
                    clip2.setFramePosition(0);
                }
            } else if(sp1.getStatusTarget() == 's')  {
                if(sp1.getValue() < 100) {
                    p2.changeSpeed(sp1.getValue());
		            clip3.start();
                    clip3.setFramePosition(0);
                } else {
                    p1.changeSpeed(sp1.getValue());
		            clip4.start();
                    clip4.setFramePosition(0);
                }
            } else {
                if(sp1.getValue() < 100) {
                    p2.changeBougyo(sp1.getValue());
		            clip3.start();
                    clip3.setFramePosition(0);
                } else {
                    p1.changeBougyo(sp1.getValue());
		            clip5.start();
                    clip5.setFramePosition(0);
                }
            }
        } 
    }

    public void p2Skill (int Player1SkillID, int Player2SkillID) {
        Medimon p1 = all1medimon.get(now1medimon);
        Medimon p2 = all2medimon.get(now2medimon);
        if(p1.getHp()!=0 && p2.getHp()!=0) {
            Skill sp2 = p2.getSkill(Player2SkillID);
            if(sp2.getStatusTarget() == 'h') {
                if(sp2.getValue() < 0) {
                    p1.changeHp(sp2.getValue(), sp2.getType());
		            clip1.start();
                    clip1.setFramePosition(0);
                } else {
                    p2.changeHp(sp2.getValue(), sp2.getType());
		            clip2.start();
                    clip2.setFramePosition(0);
                }
            } else if(sp2.getStatusTarget() == 's')  {
                if(sp2.getValue() < 100) {
                    p1.changeSpeed(sp2.getValue());
		            clip3.start();
                    clip3.setFramePosition(0);
                } else {
                    p2.changeSpeed(sp2.getValue());
		            clip4.start();
                    clip4.setFramePosition(0);
                }
            } else {
                if(sp2.getValue() < 100) {
                    p1.changeBougyo(sp2.getValue());
		            clip3.start();
                    clip3.setFramePosition(0);
                } else {
                    p2.changeBougyo(sp2.getValue());
		            clip5.start();
                    clip5.setFramePosition(0);
                }
            }
        }        
    }

    public void p1Change (int Player1MonsterID) {
        now1medimon = Player1MonsterID;
		clip6.start();
        clip6.setFramePosition(0);
    }

    public void p2Change (int Player2MonsterID) {
        now2medimon = Player2MonsterID;
		clip6.start();
        clip6.setFramePosition(0);
    }

    public void deathChange(int Player, int monsterID) {
        clip6 = createClip(new File("Koutai.wav"));
        try {
            final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(new Runnable() {
                @Override
                public void run() {
                    if(Player == 1) p1Change(monsterID);
                    else p2Change(monsterID);                       
                }
            }, 1, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println(e);
        }
        
    }

    public Skill getSkill(int Player, int Index) {
        if(Player == 1) {                            // Server
            return all1medimon.get(now1medimon).getSkill(Index);
        } else {                            // Client
            return all2medimon.get(now2medimon).getSkill(Index);
        }
    }

    public int getNowmedimon(int Player) { 
        if(Player == 1) return now1medimon;
        else return now2medimon; 
    }

    public int getRemaining(int Player) {
        int n = 0;
        if(Player == 1) {
            for(int i=0; i<3; i++) {
                if(all1medimon.get(i).getHp() != 0) n = n + 1;
            }
        } else {
            for(int i=0; i<3; i++) {
                if(all2medimon.get(i).getHp() != 0) n = n + 1;
            }
        }
        return n;
    }

    public int gameSet() {
        boolean p1 = all1medimon.get(0).getHp() == 0 && all1medimon.get(1).getHp() == 0 && all1medimon.get(2).getHp() == 0;
        boolean p2 = all2medimon.get(0).getHp() == 0 && all2medimon.get(1).getHp() == 0 && all2medimon.get(2).getHp() == 0;
        if(p1) return 2;
        else if(p2) return 1;
        else return 0;
    }

    public static Clip createClip(File path) {
		//指定されたURLのオーディオ入力ストリームを取得
		try (AudioInputStream ais = AudioSystem.getAudioInputStream(path)){
			//ファイルの形式取得
			AudioFormat af = ais.getFormat();
			//単一のオーディオ形式を含む指定した情報からデータラインの情報オブジェクトを構築
			DataLine.Info dataLine = new DataLine.Info(Clip.class,af);
			//指定された Line.Info オブジェクトの記述に一致するラインを取得
			Clip c = (Clip)AudioSystem.getLine(dataLine);
			//再生準備完了
			c.open(ais);
			return c;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return null;
	}

    public void CloseClip() {
        clip1.close(); clip2.close(); clip3.close(); clip4.close(); clip5.close(); clip6.close(); 
    }
}
