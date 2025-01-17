import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


class Skill {
    private Character x;
    private int n;
    private String name;
    public Character getStatusTarget() { return x; }
    public void setStatusTarget(Character x) { this.x = x; }
    public int getValue() { return n; }
    public void setValue(int n) { this.n = n; }
    public String getSkillname() { return name; }
    public void setSkillname(String name) { this.name = name; }
}

class Medimon {
    private int H, h, s, b;  //体力、速さ、防御
    private ArrayList<Skill> a;  //技
    private int i=0;  //技の数
    private String n;  //名前

    public Medimon(int h, int s, int b, Skill s1, Skill s2, Skill s3, Skill s4, String n){
        this.H = h; this.h = h; this.s = s; this.b = b; this.n = n;
        this.a = new ArrayList<Skill>();
        a.add(s1);
        a.add(s2);
        a.add(s3);
        a.add(s4);
        i = 4;
    }

    //体力の減少・増加
    public void changeHp(int n){
        if(n > 0) {
            h = h + n;
            if(h >= H) h = H;
        } else {
            h = h + (int)(n*(150-b)/(float)100);
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
    public Skill getSkill(int index) { return a.get(index); }
    public String getName() { return n; }
}

class MedimonModel {
    private ArrayList<Medimon> all1medimon, all2medimon;       // 1がServer、２がClient
    private int now1medimon = 0, now2medimon = 0;
    private int h, s, b; 
    private Skill s1, s2, s3, s4;
    private String n;
    private int serverconducting = 0;          //行動処理中であるか判別する値
    public MedimonModel() {
        this.all1medimon = new ArrayList<Medimon>();
        h = 150; s = 50; b = 50;  n = "トーグ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setSkillname("パンチ");
        s2.setStatusTarget('b'); s2.setValue(120); s2.setSkillname("ぼうぎょ");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(20); s4.setSkillname("ねむる");
        Medimon m1 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all1medimon.add(m1);
        h = 100; s = 55; b = 50;  n = "ウチャ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-40); s1.setSkillname("かえん");
        s2.setStatusTarget('s'); s2.setValue(120); s2.setSkillname("炎の舞");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setSkillname("かいふく");
        m1 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all1medimon.add(m1);
        h = 100; s = 50; b = 50;  n = "Monster3";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setSkillname("パンチ");
        s2.setStatusTarget('h'); s2.setValue(-40); s2.setSkillname("キック");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setSkillname("かいふく");
        m1 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all1medimon.add(m1);
        this.all2medimon = new ArrayList<Medimon>();    
        h = 150; s = 50; b = 50;  n = "トーグ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setSkillname("パンチ");
        s2.setStatusTarget('b'); s2.setValue(110); s2.setSkillname("ぼうぎょ");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(20); s4.setSkillname("ねむる");
        Medimon m2 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all2medimon.add(m2);
        h = 100; s = 55; b = 50;  n = "ウチャ";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-40); s1.setSkillname("かえん");
        s2.setStatusTarget('s'); s2.setValue(120); s2.setSkillname("炎の舞");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setSkillname("かいふく");
        m2 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all2medimon.add(m2);
        h = 100; s = 50; b = 50;  n = "Monster3";
        s1 = new Skill(); s2 = new Skill(); s3 = new Skill(); s4 = new Skill();
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setSkillname("パンチ");
        s2.setStatusTarget('h'); s2.setValue(-40); s2.setSkillname("キック");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setSkillname("かいふく");
        m2 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all2medimon.add(m2);
    }

    public Medimon getMedimon(int Player, int Index) {
        if(Player == 1) {                            // Server
            return all1medimon.get(Index);
        } else {                            // Client
            return all2medimon.get(Index);
        }
    }

    public void setSkill(int Player1MonsterID, int Player1SkillID, int Player2MonsterID, int Player2SkillID) { //二人の行動を受け取る
        serverconducting = 1;
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
            serverconducting = 0;
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
            serverconducting = 0;
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
            serverconducting = 0;
        }
    }

    public void p1Skill (int Player1SkillID, int Player2SkillID) {
        Medimon p1 = all1medimon.get(now1medimon);
        Medimon p2 = all2medimon.get(now2medimon);
        if(p1.getHp()!=0 && p2.getHp()!=0) {
            Skill sp1 = p1.getSkill(Player1SkillID);
            if(sp1.getStatusTarget() == 'h') {
                if(sp1.getValue() < 0) p2.changeHp(sp1.getValue());
                else p1.changeHp(sp1.getValue());
            } else if(sp1.getStatusTarget() == 's')  {
                if(sp1.getValue() < 100) p2.changeSpeed(sp1.getValue());
                else p1.changeSpeed(sp1.getValue());
            } else {
                if(sp1.getValue() < 100) p2.changeBougyo(sp1.getValue());
                else p1.changeBougyo(sp1.getValue());
            }
        } 
    }

    public void p2Skill (int Player1SkillID, int Player2SkillID) {
        Medimon p1 = all1medimon.get(now1medimon);
        Medimon p2 = all2medimon.get(now2medimon);
        if(p1.getHp()!=0 && p2.getHp()!=0) {
            Skill sp2 = p2.getSkill(Player2SkillID);
            if(sp2.getStatusTarget() == 'h') {
                if(sp2.getValue() < 0) p1.changeHp(sp2.getValue());
                else p2.changeHp(sp2.getValue());
            } else if(sp2.getStatusTarget() == 's')  {
                if(sp2.getValue() < 100) p1.changeSpeed(sp2.getValue());
                else p2.changeSpeed(sp2.getValue());
            } else {
                if(sp2.getValue() < 100) p1.changeBougyo(sp2.getValue());
                else p2.changeBougyo(sp2.getValue());
            }
        }        
    }

    public void p1Change (int Player1MonsterID) {
        now1medimon = Player1MonsterID;
    }

    public void p2Change (int Player2MonsterID) {
        now2medimon = Player2MonsterID;
    }

    public void deathChange(int Player, int monsterID) {
        if(Player == 1) now1medimon = monsterID;
        else now2medimon = monsterID;
    }

    public Skill getSkill(int Player, int Index) {
        if(Player == 1) {                            // Server
            return all1medimon.get(now1medimon).getSkill(Index);
        } else {                            // Client
            return all2medimon.get(now2medimon).getSkill(Index);
        }
    }

    public int getNowmedimon(int Player) { 
        if(Player ==1) return now1medimon;
        else return now2medimon; 
    }

    public int gameSet() {
        boolean p1 = all1medimon.get(0).getHp() == 0 && all1medimon.get(1).getHp() == 0 && all1medimon.get(2).getHp() == 0;
        boolean p2 = all2medimon.get(0).getHp() == 0 && all2medimon.get(1).getHp() == 0 && all2medimon.get(2).getHp() == 0;
        if(p1) return 2;
        else if(p2) return 1;
        else return 0;
    }
}
