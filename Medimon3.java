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