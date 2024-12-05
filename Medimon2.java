import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class Medimon {
    private int h, s, b;  //体力、速さ、防御
    private ArrayList<Integer> a;  //技

    public Medimon(int h, int s, int b){
        this.h = h; this.s = s; this.b = b;
        this.a = new ArrayList<Integer>();
    }

    

    //体力の減少
    public void downHp(int n){
        if(h >= n) h = h - n;
        else h = 0;
    }

    //速さの減少
    public void downSpeed(int n){
        if(s >= n) s = s - n;
        else s = 0;
    }

    //防御の減少
    public void downBougyo(int n){
        if(b >= n) b = b - n;
        else b = 0;
    }
}