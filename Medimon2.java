import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class Medimon {
    private int H, h, s, b;  //体力、速さ、防御
    private ArrayList<Skill> a;  //技
    private int i=0;  //技の数

    public Medimon(int h, int s, int b){
        this.H = h; this.h = h; this.s = s; this.b = b;
        this.a = new ArrayList<Skill>();
    }

    public void addSkill(Character x, int n) {
        Skill d = new Skill();
        d.x = x; d.n = n;
        a.add(d);
        i++;
    }

    //体力の減少・増加
    public void changeHp(int n){
        h = h + n;
        if(h >= H) h = H;
        if(h < 0) h = 0;
    }

    //速さの減少・増加
    public void changeSpeed(Float n){
        s = (int)(s * n);
    }

    //防御の減少・増加
    public void changeBougyo(Float n){
        b = (int)(b * n);
    }

    class Skill {
        Character x;
        int n;
    }
}