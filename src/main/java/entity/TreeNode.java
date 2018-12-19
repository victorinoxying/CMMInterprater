package entity;

import javax.swing.tree.DefaultMutableTreeNode;

public class TreeNode extends DefaultMutableTreeNode {
    private String type;
    private String content;
    private int rowNum;

    public TreeNode() {
        super();
        type = "";
        content = "";
        rowNum =0;
    }

    public TreeNode(String content){
        super(content);
        this.content = content;
    }

    public TreeNode(String content, String type){
        super(content);
        this.type = type;
        this.content = content;
    }


    public TreeNode(String content, String type, int rowNum){
        super(content);
        this.rowNum = rowNum;
        this.type = type;
        this.content = content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRowNum() {
        return rowNum;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public void add(TreeNode childNode) {
        super.add(childNode);
    }

    public TreeNode getChildAt(int index) {
        return (TreeNode) super.getChildAt(index);
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }
}
