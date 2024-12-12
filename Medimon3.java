import java.awt.event.*;

class MedimonController implements ActionListener {
    protected Medimon1 view;
    protected Medimon medimon;
    CommServer sv = new CommServer(10010);
    CommClient cl = new CommClient();
    String serverMsg;
    //isServerAction = 1/0のときserver/client側の行動を示す
    int monsterHp, monsterID, skillID, isServerAction;
    public MedimonController(Medimon m, Medimon1 v) {
        view = v;
        medimon = m;
        //view.(Buttonオブジェクト名).addActionListener(this);
    }

    public void actionPerformed(ActionEvent e){
        //skillIDを設定しやすいように、setActionCommand()でskillIDを設定してください
        skillID = Integer.parseInt(e.getActionCommand());

    }

    public void sendAction(){
        serverMsg = String.format("%d %d &d %d", monsterHp, monsterID, skillID, isServerAction);
        sv.send(serverMsg);
        cl.send(serverMsg);
    }

    //サーバーからのメッセージを取得するときは、以下のrecvFromServer()をViewに追加する
    public void recvFromServer(){
        String[] serverMsgArray = sv.recv().split(" ");
        String[] clientMsgArray = cl.recv().split(" ");
        //View, model側にmonsterHp, monsterID, skillID, isServerActionの値をしまうint型変数と、
        //それをsetするメソッドを作ってください
    }
}