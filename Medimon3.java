import java.awt.event.*;

class MedimonController implements ActionListener {
    protected Medimon1 view;
    protected MedimonModel model;
    CommServer sv;
    CommClient cl;
    String serverMsg;
    //自分がサーバかクライアントかを記録
    boolean isServer;
    
    //boolean serverConducting == 0のとき、サーバが処理中(入力は受け付けない)
    //boolean clientConducting == 0のとき、クライアントが処理中(入力は受け付けない)
    //get server/clientConducting();とメッセージで現在の処理状態を取得
    int serverConducting = 0, clientConducting = 0;
    
    //existCommandWindow == trueのとき、Viewのコマンドウィンドウを表示する
    boolean existCommandWindow = true;
    
    int monsterID, skillID;
    public MedimonController(MedimonModel m, Medimon1 v, boolean isServer) {
        view = v;
        model = m;
        this.isServer = isServer;
        if(isServer){
            sv = new CommServer(10010);
        } else {
            cl = new CommClient("localhost", 10010);
        }
        //view.(Buttonオブジェクト名).addActionListener(this);
    }

    public void actionPerformed(ActionEvent e){
        //skillIDを設定しやすいように、JButton名.setActionCommand(skillID);を設定してください
        existCommandWindow = false;
        skillID = Integer.parseInt(e.getActionCommand());
        
        if(isServer){
            if(!model.getServerConducting == 0){
                recvFromServer();
                if(clientConducting == 0){
                    setToModel();
                }
            }
        } else {
            if(!model.getClientConducting == 0){
                recvFromServer();
                if(serverConducting == 0){
                    setToModel();
                }
            }
        }
        
        skillID = Integer.parseInt(e.getActionCommand());
    }

    //sendAction()
    public void sendAction(int monsterIndex, int skillIndex){
        if(isServer){
            serverMsg = String.format("%d %d %d %d", monsterIndex, skillIndex, 1, serverConducting); //1 -> server
            sv.send(serverMsg);
        } else {
            serverMsg = String.format("%d %d %d %d", monsterIndex, skillIndex, 2, clientConducting); //2 ->client
            cl.send(serverMsg);
        }
    }

    public void recvFromServer(){
        if(isServer){
            String[] clientMsgArray = sv.recv().split(" ");
            clientConducting = Integer.parseInt(clientMsgArray[3]);
        } else {
            String[] serverMsgArray = cl.recv().split(" ");
            serverConducting = Integer.parseInt(serverMsgArray[3]);
            //model.setMonster(Integer.parseInt(serverMsgArray[0]));
            //model.setSkill(Integer.parseInt(serverMsgArray[1]));
        }
        //View, model側にmonsterID, skillIDの値をしまうint型変数と、
        //それをsetするメソッドを作ってください
    }

    //ボタン入力から受け取った自分のmonster/skillIDと、メッセージから受け取った相手のmonster/skillIDをmodelにsetする関数
    public void setToModel(){
        if(isServer){
            //model.setMonster();
            //model.setSkill();
            
            //model.setOpponentMonster(Integer.parseInt(clientMsgArray[0]));
            //model.setOpponentSkill(Integer.parseInt(clientMsgArray[1]));
        } else {
            //model.setMonster();
            //model.setSkill();
            
            //model.setOpponentMonster(Integer.parseInt(clientMsgArray[0]));
            //model.setOpponentSkill(Integer.parseInt(clientMsgArray[1]));
        }
    }

    public boolean getExistCommandWindow(){
        return existCommandWindow;
    }
}