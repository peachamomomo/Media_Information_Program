import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

class MedimonServerTest implements ActionListener {
    protected Medimon1 view;
    protected Medimon medimon;
    CommServer sv;
    CommClient cl;
    String serverMsg;
    //isServerAction = 1/0のときserver/client側の行動を示す
    int monsterHp, monsterID, skillID, isServerAction;
    public MedimonServerTest(Medimon m, Medimon1 v, CommServer server, CommClient client) {
        view = v;
        medimon = m;
        sv = server;
        cl = client;
        monsterHp = 10;
        monsterID = 0;
        skillID = 0;
        isServerAction = 0;
        //view.(Buttonオブジェクト名).addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e){
        //skillIDを設定しやすいように、setActionCommand()でskillIDを設定してください
        skillID = Integer.parseInt(e.getActionCommand());
    }

    public void sendToView(){
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

    public static void main(String[] args) {
        CommServer server = new CommServer(10010);
        CommClient client = new CommClient();
        BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
        String msg = "start";
        MedimonServerTest testServer = new MedimonServerTest(new Medimon(0, 0, 0), new Medimon1(), server, client);

        try {
            while((msg = server.recv()) != null){
                msg = buf.readLine();

            }    
        } catch (Exception e) {}
        
    }
}