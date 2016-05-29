package geek.jai.angelbot.modal;

/**
 * Created by JAID on 28-05-2016.
 * Type Value :
 * 0 : Sent
 * 1 : Received
 * 2 : Pic
 */
public class Chat {
    private String chatText;
    private int type;
    private String fileURI;

    public Chat() {

    }

    public Chat(String chatText, int type) {
        this.chatText = chatText;
        this.type = type;
    }

    public String getChatText() {
        return chatText;
    }

    public void setChatText(String chatText) {
        this.chatText = chatText;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getFileURI() {
        return fileURI;
    }

    public void setFileURI(String fileURI) {
        this.fileURI = fileURI;
    }
}
