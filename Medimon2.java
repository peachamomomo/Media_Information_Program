import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

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
    private Skill s1 = new Skill(), s2 = new Skill(), s3 = new Skill(), s4 = new Skill();
    private String n;
    private int serverconducting = 0;          //行動処理中であるか判別する値
    public MedimonModel() {
        this.all1medimon = new ArrayList<Medimon>();
        h = 100; s = 50; b = 50;  n = "Monster";
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setSkillname("パンチ");
        s2.setStatusTarget('h'); s2.setValue(-40); s2.setSkillname("キック");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setSkillname("かいふく");
        Medimon m1 = new Medimon(h, s, b, s1, s2, s3, s4, n);
        all1medimon.add(m1);
        this.all2medimon = new ArrayList<Medimon>();    
        h = 100; s = 50; b = 50;  n = "Monster";
        s1.setStatusTarget('h'); s1.setValue(-20); s1.setSkillname("パンチ");
        s2.setStatusTarget('h'); s2.setValue(-40); s2.setSkillname("キック");
        s3.setStatusTarget('h'); s3.setValue(-60); s3.setSkillname("タックル");
        s4.setStatusTarget('h'); s4.setValue(40); s4.setSkillname("かいふく");
        Medimon m2 = new Medimon(h, s, b, s1, s2, s3, s4, n);
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
        Medimon p1 = all1medimon.get(now1medimon);
        Medimon p2 = all2medimon.get(now2medimon);
        Skill sp1 = p1.getSkill(Player1SkillID);
        Skill sp2 = p2.getSkill(Player2SkillID);
        if(Player1MonsterID < 0 || Player2MonsterID < 0) {

        } else {
        if(p1.getSpeed() >= p2.getSpeed()) {
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
        } else {
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
        for (int i = 0; i < 5; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(p1.getSpeed() >= p2.getSpeed()) {  
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
        }  else {
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
        serverconducting = 0;
    }

    public Skill getSkill(int Player, int Index) {
        if(Player == 1) {                            // Server
            return all1medimon.get(now1medimon).getSkill(Index);
        } else {                            // Client
            return all2medimon.get(now2medimon).getSkill(Index);
        }
    }

    public int getServerConducting() { return serverconducting; }

    public int getNow1medimon() { return now1medimon; }

    public int getNow2medimon() { return now2medimon; }
}
