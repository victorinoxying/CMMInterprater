package entity;

public class SymbolElement {
    /* 元素名字 */
    private String name;
    /* 元素类型 */
    private String type;
    /* 元素所在行号 */
    private int rowNum;
    /* 元素作用域 */
    private int level;
    /* 元素的值 */
    private String value;
    /* 表明元素是否为数组,0表示不是,否则表示数组的大小 */
    private int arrayElementsNum;

    public SymbolElement(String name,String type,int rowNum,int level) {
        this.name = name;
        this.type = type;
        this.rowNum = rowNum;
        this.level = level;
        this.value = "";
        this.arrayElementsNum = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getArrayElementsNum() {
        return arrayElementsNum;
    }

    public void setArrayElementsNum(int arrayElementsNum) {
        this.arrayElementsNum = arrayElementsNum;
    }

    public String toString() {
        return name + "_" + type + "_" + level + "_" + arrayElementsNum;
    }

    public boolean equals(Object object) {
        SymbolElement element = (SymbolElement) object;
        return this.toString().equals(element.toString());
    }

}
