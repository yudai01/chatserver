import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClientHandler extends Thread{
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    List clients;
    String name;
    ////////////////////////////////////////////////////////////
    public ChatClientHandler(Socket socket,List clients){
        this.socket = socket;
	this.clients = clients;
	this.name = "undefiend"+(clients.size()+1);
	for(int i = 0;i<clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    String checkname = handler.getClientsName();
	    if(checkname.equals(name)){
		this.name= "undefiend"+(clients.size()+1+(i+1));
	    }
	}
    }
    
    ///////////////////////////////////////////////////////////
    public void run(){
	try{
	    System.out.println(this.getClientsName()+": conected. クライアント"+(clients.size())+"が接続");
	    open();
	    while(true){
		String message = receive();
		String[] commands = message.split(" ");
		if(commands[0].equalsIgnoreCase("post")){
		    post(commands[1]);
		}
		else if(commands[0].equalsIgnoreCase("help")){
		    help();
		}
		else if(commands[0].equalsIgnoreCase("whoami")){
		    whoami();
		}
		else if(commands[0].equalsIgnoreCase("name")){
		    name(commands[1]);
		}
		else if(commands[0].equalsIgnoreCase("bye")){
		    bye();
		}
		else if(commands[0].equalsIgnoreCase("tell")){
		    tell(commands[1],commands[2]);
		}
		else if(commands[0].equalsIgnoreCase("users")){
		    users();
		}
		else{
		    this.send("存在しないコマンド");
		}

	    }
	} catch(IOException e){
	    e.printStackTrace();
	} finally{
	    close();
	}
    }    
    ////////////////////////////////////////////////////
    /**
     * 名前を返すメソッド．
     */

    public String getClientsName(){
	return name;
    }
    ////////////////////////////////////////////////////

    /**
     * クライアントとのデータのやり取りを行うストリームを開くメソッド．
     */
    public void open() throws IOException{
        in = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        out = new BufferedWriter(
            new OutputStreamWriter(socket.getOutputStream())
        );
    }
    //////////////////////////////////////////////////////
    
    /**
     * クライアントからデータを受け取るメソッド．
     */
    public String receive() throws IOException{
        String line = in.readLine();
        System.out.println(this.getClientsName()+": "+line);
        return line;
    }
    ///////////////////////////////////////////////////////

    /**
     * クライアントにデータを送信するメソッド．
     */
    public void send(String message) throws IOException{
        out.write(message);
        out.write("\r\n");
        out.flush();
    }
    ////////////////////////////////////////////////////
    /**
     * 接続しているユーザー全員にメッセージを送信するメソッド．
     */
    public void post(String message)throws IOException{
	List names = new ArrayList();
	for(int i = 0;i<clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
		if(handler!=this){
		    names.add(handler.getClientsName());
		    handler.send("["+this.getClientsName()+"]"+message);
		}
	}
	Collections.sort(names);//リストを昇順にソート
	String returnMessage="";
	for(int i=0;i<names.size();i++){
	    returnMessage=returnMessage+names.get(i)+",";
	}
	if(returnMessage.equals("")){
	    returnMessage="送信できませんでした";
	}
	this.send(returnMessage);
    }
    //////////////////////////////////////////////////////
    /**
     * 使用可能なコマンドを表示するメソッド．
     */ 
   public void help()throws IOException{
	this.send("post-全員にメッセージを送信します");
	this.send("name-名前を変更します");
	this.send("whoami-自分の名前を表示します");
	this.send("tell-特定の人物にメッセージを送信");
	this.send("users-現在接続しているユーザーを表示します");
	this.send("bye-接続を切断します");
    }
    /////////////////////////////////////////////////////
    /**
     * ユーザー名を変更するメソッド．
     */
    public void name(String name)throws IOException{
	int cheak = 0;
	for(int i = 0;i<clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if((handler.getClientsName()).equals(name)){
		this.send("同一の名前が存在します");
		break;
	    }
	    else if(i==clients.size()-1){//最後までまわる、つまり同一の名前がなかった場合に名前を変更する
		this.name=name;
	    }
	}
	
    }
    ////////////////////////////////////////////////////
    /**
     * 自分のユーザー名を表示するメソッド．
     */
    public void whoami()throws IOException{
	this.send("["+this.getClientsName()+"]");
    }

    ////////////////////////////////////////////////////
    /**
     * 特定のユーザにメッセージを送信するメソッド．
     */
    public void tell(String sendname,String message)throws IOException{
	for(int i = 0;i<clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if((handler.name).equals(sendname)){
		handler.send("["+this.getClientsName()+"]"+message);
	    }
	}
    }

    ///////////////////////////////////////////////////
    /**
     * 接続されているユーザーを表示するメソッド．
     */
    public void users()throws IOException{
	List names = new ArrayList();
	for(int i = 0;i<clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    names.add(handler.getClientsName());
	}
	Collections.sort(names);
	String returnMessage="";
	for(int i=0;i<names.size();i++){
	    returnMessage=returnMessage+names.get(i)+",";
	}
	this.send(returnMessage);

    }
    ///////////////////////////////////////////////////
    /**
     * サーバーのリストから削除するメソッド
     */
    public void bye()throws IOException{
	int listnumber=0;
	for(int i = 0;i<clients.size();i++){
	    ChatClientHandler handler = (ChatClientHandler)clients.get(i);
	    if(handler!=this){
		handler.send(this.getClientsName()+"が退室しました");
	    }
	    else{
		listnumber=i;
	    }
	}
	ChatServer.remove(listnumber);
       	close();
    }
    
    /////////////////////////////////////////////////////
    /**
     * クライアントとの接続を閉じるメソッド．
     */
    public void close(){
        if(in != null){
            try{
                in.close();
            } catch(IOException e){ }
        }
        if(out != null){
            try{
                out.close();
            } catch(IOException e){ }
        }
        if(socket != null){
            try{
                socket.close();
            } catch(IOException e){ }
        }
    }
    //////////////////////////////////////////////////////
}

/*工夫した点
  変数宣言を考えられる限り減らしメモリの無駄遣いをなくすように工夫しました。またfor文での処理もこれ以上回す必要がないと判断した場合にはbreak で抜け出すようにし処理時間の短縮を図っています。*/
