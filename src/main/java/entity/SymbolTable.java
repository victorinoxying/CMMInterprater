package entity;

import java.util.Vector;

public class SymbolTable {
    /* 存放SymbolTableElement */
    private Vector<SymbolElement> symbolTable = new Vector<SymbolElement>();

    /* 根据索引查找SymbolTableElement对象*/
    public SymbolElement get(int index) {
        if (index>symbolTable.size()-1||index<0){
            return null;
        }
        return symbolTable.get(index);
    }

    /**
     * 根据SymbolTableElement对象的名字对所有作用域查找
     */
    public SymbolElement getAllLevel(String name, int level) {
        while (level > -1) {
            for (SymbolElement element : symbolTable) {
                if (element.getName().equals(name) && element.getLevel() == level) {
                    return element;
                }
            }
            level--;
        }
        return null;
    }

    /*根据SymbolTableElement对象的名字对当前作用域查找*/
    public SymbolElement getCurrentLevel(String name, int level) {
        for (SymbolElement element : symbolTable) {
            if (element.getName().equals(name) && element.getLevel() == level) {
                return element;
            }
        }
        return null;
    }

    /* 向symbolTable中添加SymbolTableElement对象,放在末尾*/
    public boolean add(SymbolElement element) {
        return symbolTable.add(element);
    }

    /* 在symbolTable中指定的索引处添加SymbolTableElement对象*/
    public void add(int index, SymbolElement element) {
        symbolTable.add(index, element);
    }

    /* 从symbolTable中移除指定索引处的元素*/
    public void remove(int index) {
        symbolTable.remove(index);
    }

    /*从symbolTable中移除指定名字和作用域的元素*/
    public void remove(String name, int level) {
        for (int i = 0; i < size(); i++) {
            if (get(i).getName().equals(name) && get(i).getLevel() == level) {
                remove(i);
                return;
            }
        }
    }

    /*清空symbolTable中的元素,将其大小设为0*/
    public void clear() {
        symbolTable.clear();
    }

    /*当level减小时更新符号表,去除无用的元素*/
    public void update(int level) {
        for (int i = 0; i < size(); i++) {
            if (get(i).getLevel() > level) {
                remove(i);
            }
        }
    }

    /*判断是否包含指定的元素*/
    public boolean contains(SymbolElement element) {
        return symbolTable.contains(element);
    }

    /*判断是否为空*/
    public boolean isEmpty() {
        return symbolTable.isEmpty();
    }

    /*计算元素个数 */
    public int size() {
        return symbolTable.size();
    }

    public Vector<SymbolElement> getSymbolTable() {
        return symbolTable;
    }
}
