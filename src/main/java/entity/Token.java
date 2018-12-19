package entity;

public class Token {
    private String type;
    private int row;
    private int column;
    private String content;

    public Token(int row, int col, String type, String content){
        this.row = row;
        this.column = col;
        this.type = type;
        this.content = content;

    }
    public String getType(){
        return type;
    }
    public String getContent(){
        return content;
    }
    public int getRow(){
        return row;
    }
    public int getColumn(){
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setType(String type) {
        this.type = type;
    }
}

