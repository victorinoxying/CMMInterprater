package interpretor;

import entity.SymbolElement;
import entity.SymbolTable;
import entity.TreeNode;
import tools.ConstVar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class CMMSemanticAnalysis {
    private SymbolTable table = new SymbolTable();
    /* 语法分析得到的抽象语法树 */
    private TreeNode root;
    /* 语义分析错误信息 */
    private ArrayList<String> errorList = new ArrayList<String>();
    /* 语义分析标识符作用域 */
    private int level = 0;

    private HashSet<String> calculateSignalSet = ConstVar.getCalculateSignalSet();
    private HashSet<String> compareSignalSet = ConstVar.getCompareSignalSet();

    public CMMSemanticAnalysis(TreeNode root) {
        this.root = root;
    }

    public void analysis(){
        if(root!=null){
            statement(root);
        }
    }

    public void PrintError(){
        if (errorList.size() > 0) {
            System.out.println("语义分析错误：");
            for (String error:errorList) {
                System.out.println(error);
            }

        }
    }

    public void PrintSymbolTable(){
        if(table.size()>0){
            System.out.println("语义分析得到的符号表：");
            for(SymbolElement element:table.getSymbolTable()){
                String symbol = "name: "+element.getName()+" type: "+element.getType()+" value: "+element.getValue();
                System.out.println(symbol);
            }

        }
    }

    private void addError(String error, int line) {
        String errorInfo = "ERROR: " + "第 " + line + " 行：" + error;
        errorList.add(errorInfo);
    }

    private void addTypeError(String lType, String rType, int line){
        String errorInfo = "不能将"+rType+"类型赋值给"+lType +"型变量";
        addError(errorInfo,line);
    }


    private void statement(TreeNode root){
        if(root.getContent().equals(ConstVar.EMPTY_STM)||root.getChildCount()==0){
            return;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode currentNode = root.getChildAt(i);
            String content = currentNode.getContent();
            if (content.equals(ConstVar.DEClARE_STATEMENT)) {
                declare_stm(currentNode);
            } else if (content.equals(ConstVar.ASSIGN_STATEMENT)) {
                assign_stm(currentNode);
            } else if (content.equals(ConstVar.FOR_STATEMENT)) {
                // 进入for循环语句，改变作用域
                level++;
                for_stm(currentNode);
                // 退出for循环语句，改变作用域并更新符号表
                level--;
                table.update(level);
            }  else if (content.equals(ConstVar.IF_STATEMENT)) {
                // 进入if语句，改变作用域
                level++;
                if_stm(currentNode);
                // 退出if语句，改变作用域并更新符号表
                level--;
                table.update(level);
            } else if (content.equals(ConstVar.WHILE_STATEMENT)) {
                // 进入while语句，改变作用域
                level++;
                while_stm(currentNode);
                // 退出while语句，改变作用域并更新符号表
                level--;
                table.update(level);
            } else if (content.equals(ConstVar.READ_STATEMENT)) {
                read_stm(currentNode);
            } else if (content.equals(ConstVar.WRITE_STATEMENT)) {
                write_stm(currentNode);
            }
            else {
                return;
            }
        }
    }

    private SymbolElement doID(TreeNode node, boolean isAssign){
        String id = node.getContent();
        if(table.getAllLevel(id,level) == null){
            String error  = "变量 "+ id+" 在使用时未声明";
            addError(error, node.getRowNum());
            return null;
        }
        else {
            //数组
            if(node.getChildCount()>0&&node.getChildAt(0).getContent().equals(ConstVar.ARRAY_STATEMENT)){
                String index = array(node.getChildAt(0),id);
                if(index!= null){
                    id+="["+index+"]";
                }
                else {
                    return null;
                }
            }
            SymbolElement element = table.getAllLevel(id,level);
            if(isAssign){
                if(element!=null){
                    return element;
                }
                else {
                    String error  = "变量 "+ id+" 在使用时未声明";
                    addError(error, node.getRowNum());
                    return null;
                }
            }
            else {
                if(element!=null && !element.getValue().equals("")){
                    return element;
                }else {
                    String error = "变量"+id+"未初始化";
                    addError(error,node.getRowNum());
                    return null;
                }
            }
        }

    }

    private String array(TreeNode node, String id){
        int size = table.getAllLevel(id,level).getArrayElementsNum();
        if(size==0){
            String error = "类型错误,变量 "+id+" 不是数组类型";
            addError(error, node.getRowNum());
            return null;
        }
        TreeNode valueNode = node.getChildAt(0);
        String content = valueNode.getContent();
        String type = valueNode.getType();
        if(type.equals(ConstVar.TYPE_INT)){
            return doArrayIndex(size,content,valueNode.getRowNum());
        }
        else if(type.equals(ConstVar.TYPE_ID)){
            SymbolElement checkedId = doID(valueNode,false);
            if(checkedId!=null){
                if(checkedId.getType().equals(ConstVar.TYPE_INT)){
                    return doArrayIndex(size,checkedId.getValue(),valueNode.getRowNum());
                }
                else {
                    String error = "类型不匹配,数组下标必须为整型"+content+" 是"+type+"类型变量";
                    addError(error,valueNode.getRowNum());
                    return null;
                }
            }
            //不通过id检查
            else {
                return null;
            }
        }
        else if(calculateSignalSet.contains(content)){
            TreeNode result = expression(valueNode);
            if(result!=null){
                if(result.getType().equals(ConstVar.TYPE_INT)){
                    return doArrayIndex(size,result.getContent(),valueNode.getRowNum());
                }else {
                    String error = "类型不匹配,数组下标必须为整型,下标表达式计算值为"+ConstVar.TYPE_REAL+"类型";
                    addError(error,valueNode.getRowNum());
                    return null;
                }
            }else {
                return null;
            }
        }
        else {
            String error = "类型不匹配,数组下标必须为整型, "+content+" 是"+type+"类型变量";
            addError(error,valueNode.getRowNum());
            return null;
        }
    }

    private String doArrayIndex(int size,String index,int lineNum){
        int i = Integer.parseInt(index);
        if(i>=0&&i<size){
            return index;
        }
        else if(i<0){
            String error = "数组下标不能为负数";
            addError(error,lineNum);
        }
        else if(i>=size){
            String error = "数组下标溢出";
            addError(error,lineNum);
        }
        return null;
    }

    private TreeNode expression(TreeNode node){
        if(node.getChildCount()<2){
            return null;
        }
        TreeNode resultNode = new TreeNode();
        //运算符号
        String content = node.getContent();
        // 存放两个运算对象的值
        TreeNode lNode = doExpression_cell(node.getChildAt(0));
        TreeNode rNode = doExpression_cell(node.getChildAt(1));
        if(lNode==null||rNode==null){
            return null;
        }
        else {
            if(lNode.getType().equals(ConstVar.TYPE_REAL)||rNode.getType().equals(ConstVar.TYPE_REAL)){
                resultNode.setType(ConstVar.TYPE_REAL);
                if(content.equals(ConstVar.PLUS)){
                    double result = Double.parseDouble(lNode.getContent())+Double.parseDouble(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }else if(content.equals(ConstVar.MINUS)){
                    double result = Double.parseDouble(lNode.getContent())-Double.parseDouble(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }else if(content.equals(ConstVar.MULTIPLY)){
                    double result = Double.parseDouble(lNode.getContent())*Double.parseDouble(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }else if(content.equals(ConstVar.DIVIDE)){
                    double result = Double.parseDouble(lNode.getContent())/Double.parseDouble(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }
            }
            else {
                resultNode.setType(ConstVar.TYPE_INT);
                if(content.equals(ConstVar.PLUS)){
                    int result = Integer.parseInt(lNode.getContent())+Integer.parseInt(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }else if(content.equals(ConstVar.MINUS)){
                    int result = Integer.parseInt(lNode.getContent())-Integer.parseInt(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }else if(content.equals(ConstVar.MULTIPLY)){
                    int result = Integer.parseInt(lNode.getContent())*Integer.parseInt(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }else if(content.equals(ConstVar.DIVIDE)){
                    int result = Integer.parseInt(lNode.getContent())/Integer.parseInt(rNode.getContent());
                    resultNode.setContent(String.valueOf(result));
                }
            }
        }
        return resultNode;
    }

    private TreeNode doExpression_cell(TreeNode node){
        TreeNode resultNode = new TreeNode();
        String type = node.getType();
        String value = node.getContent();
        if(type.equals(ConstVar.TYPE_INT)||type.equals(ConstVar.TYPE_REAL)){
            //直接赋值
            resultNode.setContent(value);
            resultNode.setType(type);
            return resultNode;
        }
        else if(type.equals(ConstVar.TYPE_ID)){
            SymbolElement checkedID =  doID(node, false);
            if(checkedID!=null){
                String eleType = checkedID.getType();
                String eleValue = checkedID.getValue();
                if(eleType.equals(ConstVar.TYPE_INT)||eleType.equals(ConstVar.TYPE_REAL)){
                    //取值
                    resultNode.setContent(eleValue);
                    resultNode.setType(eleType);
                    return resultNode;
                }
                else {
                    String error = "变量 "+value +" 不是整型或实数类型，无法做表达式运算";
                    addError(error,node.getRowNum());
                    return null;
                }
            }
            else{
                return null;
            }
        }
        else if(calculateSignalSet.contains(value)){
            resultNode = expression(node);
            return resultNode;
        }
        else {
            String error = "变量 "+value +" 不是整型或实数类型，无法做表达式运算";
            addError(error,node.getRowNum());
            return null;
        }
    }

    private boolean condition(TreeNode node){
        String content = node.getContent();
        String type = node.getType();
        if(content.equals(ConstVar.TRUE)){
            return true;
        }
        else if(content.equals(ConstVar.FALSE)){
            return false;
        }
        else if(type.equals(ConstVar.TYPE_ID)){
            SymbolElement checkedID = doID(node,false);
            if(checkedID!=null){
                if(checkedID.getType().equals(ConstVar.TYPE_BOOL)){
                    if(checkedID.getValue().equals(ConstVar.TRUE)){
                        return true;
                    }
                    else if(checkedID.getValue().equals(ConstVar.FALSE)){
                        return false;
                    }
                }
                else {
                    String error = "类型错误，不能将 "+checkedID.getType()+" 类型变量"+checkedID.getName()+"作为判断条件";
                    addError(error,node.getRowNum());
                    return false;
                }
            }
        }
        else if(compareSignalSet.contains(content)){
            TreeNode lExpression = condition_cell(node.getChildAt(0));
            TreeNode rExpression = condition_cell(node.getChildAt(1));
            if(lExpression!=null&&rExpression!=null){
                double lValue = Double.parseDouble(lExpression.getContent());
                double rValue = Double.parseDouble(rExpression.getContent());
                if((content.equals(ConstVar.LT)&& lValue<rValue)||(content.equals(ConstVar.LT_EQUAl)&&lValue<=rValue)
                        ||(content.equals(ConstVar.GT)&&lValue>rValue)||(content.equals(ConstVar.GT_EQUAL)&&lValue>=rValue)
                        ||(content.equals(ConstVar.NEQUAL)&&lValue!=rValue)||(content.equals(ConstVar.EQUAL)&&lValue==rValue)){
                    return true;
                }
            }
        }
        return false;
    }

    private TreeNode condition_cell(TreeNode node){
        String type = node.getType();
        String content = node.getContent();
        if(type.equals(ConstVar.TYPE_INT)||type.equals(ConstVar.TYPE_REAL)){
            return node;
        }
        else if(type.equals(ConstVar.TYPE_BOOL)||type.equals(ConstVar.TYPE_STRING)){
            String error =  "类型错误，"+type+"类型不能用于比较运算";
            addError(error,node.getRowNum());
            return null;
        }
        else if(type.equals(ConstVar.TYPE_ID)){
            SymbolElement checkID = doID(node,false);
            if(checkID!=null){
                String idType = checkID.getType();
                String idValue = checkID.getValue();
                if(idType.equals(ConstVar.TYPE_INT)||idType.equals(ConstVar.TYPE_REAL)){
                    TreeNode result = new TreeNode();
                    result.setContent(idValue);
                    result.setType(idType);
                    return result;
                }
                else {
                    String error = "类型错误，"+checkID.getType()+"类型不能用于比较运算";
                    addError(error,node.getRowNum());
                }
            }
        }
        else if(calculateSignalSet.contains(content)){
            TreeNode result = expression(node);
            if(result!=null){
                return result;
            }
        }
        return null;
    }

    private void declare_stm(TreeNode node) {
        //reserved
        TreeNode typeNode = node.getChildAt(0);
        String typeContent = typeNode.getContent();
        String lType;
        if(typeContent.equals(ConstVar.INT)){
            lType = ConstVar.TYPE_INT;
        }else if(typeContent.equals(ConstVar.REAL)){
            lType = ConstVar.TYPE_REAL;
        }else if(typeContent.equals(ConstVar.BOOL)){
            lType = ConstVar.TYPE_BOOL;
        }else if(typeContent.equals(ConstVar.STRING)){
            lType = ConstVar.TYPE_STRING;
        }else {
            return;
        }
        //id
        TreeNode idNode = node.getChildAt(1);
        String idName = idNode.getContent();
        // 判断变量是否已经被声明
        if (table.getCurrentLevel(idName, level) == null) {
            // 普通变量
            if (idNode.getChildCount() == 0) {
                SymbolElement element = new SymbolElement(idNode.getContent(), lType, idNode.getRowNum(), level);
                // 判断变量是否在声明时被初始化
                if (node.getChildCount() > 2&& node.getChildAt(2).getContent().equals(ConstVar.ASSIGN)) {
                    TreeNode valueNode = node.getChildAt(2).getChildAt(0);
                    String rValue = valueNode.getContent();
                    String rType = valueNode.getType();
                    //左值为int
                    if (lType.equals(ConstVar.TYPE_INT)) {
                        if (rType.equals(ConstVar.TYPE_INT)) {
                            element.setValue(rValue);
                        }
                        else if (rType.equals(ConstVar.TYPE_REAL) || rType.equals(ConstVar.TYPE_STRING) || rType.equals(ConstVar.TYPE_BOOL)) {
                            addTypeError(lType, rType, valueNode.getRowNum());
                            return;
                        }
                        else if (rType.equals(ConstVar.TYPE_ID)) {
                            SymbolElement checkedID = doID(valueNode,false);
                            if(checkedID!=null){
                                String rIDType =checkedID.getType();
                                String rIDValue = checkedID.getValue();
                                if(rIDType.equals(ConstVar.TYPE_INT)){
                                    element.setValue(rIDValue);
                                }else {
                                    addTypeError(lType,rIDType,valueNode.getRowNum());
                                    return;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else if(calculateSignalSet.contains(rValue)){
                            TreeNode result = expression(valueNode);
                            if(result!=null){
                                if(result.getType().equals(ConstVar.TYPE_INT)){
                                    element.setValue(result.getContent());
                                }else {
                                    addTypeError(lType,result.getType(),valueNode.getRowNum());
                                    return;
                                }
                            }else {
                                return;
                            }
                        }
                    }
                    //左值为real
                    else if(lType.equals(ConstVar.TYPE_REAL)){
                        if(rType.equals(ConstVar.TYPE_REAL)||rType.equals(ConstVar.TYPE_INT)){
                            element.setValue(String.valueOf(Double.parseDouble(rValue)));
                        }
                        else if(rType.equals(ConstVar.TYPE_BOOL)||rType.equals(ConstVar.TYPE_STRING)){
                            addTypeError(lType,rType,valueNode.getRowNum());
                            return;
                        }
                        else if(rType.equals(ConstVar.TYPE_ID)){
                            SymbolElement checkedID = doID(valueNode,false);
                            if(checkedID!= null){
                                String rIDValue = checkedID.getValue();
                                String rIDType = checkedID.getType();
                                if(rIDType.equals(ConstVar.TYPE_INT)||rIDType.equals(ConstVar.TYPE_REAL)){
                                    element.setValue(String.valueOf(Double.parseDouble(rIDValue)));
                                }
                                else {
                                    addTypeError(lType,rIDType,valueNode.getRowNum());
                                    return;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else if(calculateSignalSet.contains(rValue)){
                            TreeNode result = expression(valueNode);
                            if(result!= null){
                                element.setValue(result.getContent());
                            }else {
                                return;
                            }
                        }
                    }
                    //左值为bool
                    else if(lType.equals(ConstVar.TYPE_BOOL)){
                        if(rType.equals(ConstVar.TYPE_BOOL)){
                            element.setValue(rValue);
                        }
                        else if(rType.equals(ConstVar.TYPE_INT)){
                            int intValue = Integer.parseInt(rValue);
                            if(intValue>0){
                                element.setValue(ConstVar.TRUE);
                            }else {
                                element.setValue(ConstVar.FALSE);
                            }
                        }
                        else if(rType.equals(ConstVar.TYPE_REAL)||rType.equals(ConstVar.TYPE_STRING)){
                            addTypeError(lType,rType,valueNode.getRowNum());
                            return;
                        }
                        else if(rType.equals(ConstVar.TYPE_ID)){
                            SymbolElement checkedID = doID(valueNode,false);
                            if(checkedID!= null){
                                String rIDValue = checkedID.getValue();
                                String rIDType = checkedID.getType();
                                if(rIDType.equals(ConstVar.TYPE_BOOL)){
                                    element.setValue(rIDValue);
                                }
                                else if(rIDType.equals(ConstVar.TYPE_INT)){
                                    int intValue = Integer.parseInt(rIDValue);
                                    if(intValue>0){
                                        element.setValue(ConstVar.TRUE);
                                    }else {
                                        element.setValue(ConstVar.FALSE);
                                    }
                                }
                                else{
                                    addTypeError(lType,rIDType,valueNode.getRowNum());
                                    return;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else if(compareSignalSet.contains(rValue)){
                            boolean result = condition(valueNode);
                            if(result){
                                element.setValue(ConstVar.TRUE);
                            }else {
                                element.setValue(ConstVar.FALSE);
                            }
                        }
                    }
                    //左值为string
                    else if(lType.equals(ConstVar.TYPE_STRING)){
                        if(rType.equals(ConstVar.TYPE_STRING)){
                            element.setValue(rValue);
                        }
                        else if(rType.equals(ConstVar.TYPE_BOOL)||rType.equals(ConstVar.TYPE_REAL)
                                ||rType.equals(ConstVar.TYPE_INT)){
                            addTypeError(lType,rType,valueNode.getRowNum());
                            return;
                        }
                        else if(rType.equals(ConstVar.TYPE_ID)){
                            SymbolElement checkedID = doID(valueNode,false);
                            if(checkedID!=null){
                                if(checkedID.getType().equals(ConstVar.TYPE_STRING)){
                                    element.setValue(checkedID.getValue());
                                }
                                else {
                                    addTypeError(lType,checkedID.getType(),valueNode.getRowNum());
                                    return;
                                }
                            }
                            else {
                                return;
                            }
                        }
                        else if(calculateSignalSet.contains(rValue)){
                            String error ="不能将算数表达式赋给"+ConstVar.TYPE_STRING+"类型变量";
                            addError(error,valueNode.getRowNum());
                            return;
                        }
                    }
                }
                //赋值判断结束，将表元素存入table
                table.add(element);
            }
            //声明数组处理
            else if(idNode.getChildCount() ==1&&idNode.getChildAt(0).getType().equals(ConstVar.TYPE_ARRAY)) {
                String arrayType = "";
                if(lType.equals(ConstVar.TYPE_REAL)){
                    arrayType = ConstVar.TYPE_REAL_ARRAY;
                }else if(lType.equals(ConstVar.TYPE_INT)){
                    arrayType = ConstVar.TYPE_INT_ARRAY;
                }else if(lType.equals(ConstVar.TYPE_BOOL)){
                    arrayType = ConstVar.TYPE_BOOL_ARRAY;
                }else if(lType.equals(ConstVar.TYPE_STRING)){
                    arrayType = ConstVar.TYPE_STRING_ARRAY;
                }
                SymbolElement element = new SymbolElement(idName,arrayType,idNode.getRowNum(),level);
                TreeNode arrayStmNode = idNode.getChildAt(0);
                int arraySize =0;
                TreeNode sizeNode = arrayStmNode.getChildAt(0);
                String sizeType = sizeNode.getType();
                String sizeValue = sizeNode.getContent();
                //声明处理
                if(sizeType.equals(ConstVar.TYPE_INT)){
                    int size = Integer.parseInt(sizeValue);
                    if(size<=0){
                        String error = "声明错误，声明数组长度必须大于0";
                        addError(error,sizeNode.getRowNum());
                        return;
                    }
                }
                else if(sizeType.equals(ConstVar.TYPE_STRING)||sizeType.equals(ConstVar.TYPE_REAL)||sizeType.equals(ConstVar.TYPE_BOOL)){
                    String error = "类型错误，声明数组长度只能使用整型";
                    addError(error,sizeNode.getRowNum());
                    return;
                }
                else if(sizeType.equals(ConstVar.TYPE_ID)){
                    SymbolElement checkedID = doID(sizeNode,false);
                    if(checkedID!=null){
                        if(checkedID.getType().equals(ConstVar.TYPE_INT)){
                            int size = Integer.parseInt(checkedID.getValue());
                            if(size<=0){
                                String error = "声明错误，声明数组长度必须大于0";
                                addError(error,sizeNode.getRowNum());
                                return;
                            }
                            else {
                                sizeValue = checkedID.getValue();
                            }
                        }
                        else {
                            String error = "类型错误，声明数组长度只能使用整型，变量 "+checkedID.getName()+" 是"+checkedID.getType()+"类型";
                            addError(error,sizeNode.getRowNum());
                            return;
                        }
                    }else {
                        return;
                    }
                }
                else if(calculateSignalSet.contains(sizeValue)){
                    TreeNode result = expression(sizeNode);
                    if(result!=null){
                        if(result.getType().equals(ConstVar.TYPE_INT)){
                            int size = Integer.parseInt(result.getContent());
                            if(size<=0){
                                String error = "声明错误，声明数组长度必须大于0";
                                addError(error,sizeNode.getRowNum());
                                return;
                            }
                            else {
                                sizeValue = result.getContent();
                            }
                        }
                        else {
                            String error = "类型错误，声明数组长度只能使用整型，算数表达式值为"+ConstVar.TYPE_REAL+"类型";
                            addError(error,sizeNode.getRowNum());
                            return;
                        }
                    }
                    else {
                        return;
                    }
                }
                arraySize =Integer.parseInt(sizeValue);
                element.setArrayElementsNum(arraySize);
                table.add(element);
                //给数组赋值
                if(node.getChildCount()>2&&node.getChildAt(2).getContent().equals(ConstVar.ASSIGN)){
                    TreeNode valuesNode = node.getChildAt(2).getChildAt(0);
                    String arrayValue ="{";
                    for(int i=0;i<valuesNode.getChildCount();i++){
                        TreeNode valueNode = valuesNode.getChildAt(i);
                        String type = valueNode.getType();
                        String value = valueNode.getContent();
                        if(type.equals(lType)){
                            String elementName = idName+"["+i+"]";
                            SymbolElement arrayElement = new SymbolElement(elementName,lType,valueNode.getRowNum(),level);
                            arrayElement.setValue(value);
                            table.add(arrayElement);
                            arrayValue+=value+",";
                        }else {
                            String error = "类型不匹配，不能将"+type+"类型元素添加到"+lType+"类型的数组中";
                            addError(error,valueNode.getRowNum());
                        }
                    }
                    arrayValue = arrayValue.substring(0,arrayValue.length()-1)+"}";
                    SymbolElement array = table.getAllLevel(idName,level);
                    array.setValue(arrayValue);

                }
            }
        }
        //变量已存在
        else {
            String error = "变量" + idName + "已被声明,请重命名该变量";
            addError(error, idNode.getRowNum());
        }


    }

    private void assign_stm(TreeNode node){
        TreeNode assignNode =node.getChildAt(0);
        //左值
        TreeNode lNode = assignNode.getChildAt(0);
        //右值
        TreeNode rNode = assignNode.getChildAt(1);
        String lName = lNode.getContent();
        String lType = "";
        SymbolElement checkedId = doID(lNode,true);
        if(checkedId!=null){
            lName = checkedId.getName();
            lType = checkedId.getType();
        }
        else {
            return;
        }
        String rType = rNode.getType();
        String rContent = rNode.getContent();
        String rValue = "";
        String rValueType = "";
        if(rType.equals(ConstVar.TYPE_INT)||rType.equals(ConstVar.TYPE_STRING)
                ||rType.equals(ConstVar.TYPE_BOOL)||rType.equals(ConstVar.TYPE_REAL)){
            rValue = rContent;
            rValueType = rType;
        }
        else if(rType.equals(ConstVar.TYPE_ID)){
            SymbolElement rCheckedID = doID(rNode,false);
            if(rCheckedID!=null){
                rValue = rCheckedID.getValue();
                rValueType = rCheckedID.getType();
            }
            else {
                return;
            }
        }
        else if(calculateSignalSet.contains(rContent)){
            TreeNode result = expression(rNode);
            if(result!=null){
                rValue = result.getContent();
                rValueType =result.getType();
            }else {
                return;
            }
        }
        else if(compareSignalSet.contains(rContent)){
            rValueType = ConstVar.TYPE_BOOL;
            if( condition(rNode)){
                rValue =ConstVar.TRUE;
            }else {
                rValue = ConstVar.FALSE;
            }
        }
        //对比左右值类型并赋值
        SymbolElement element = table.getAllLevel(lName,level);
        if(lType.equals(ConstVar.TYPE_INT)){
            if(rValueType.equals(ConstVar.TYPE_INT)){
                element.setValue(rValue);
            }
            else {
                addTypeError(lType,rValueType,rNode.getRowNum());
            }
        }
        else if(lType.equals(ConstVar.TYPE_REAL)){
            if(rValueType.equals(ConstVar.TYPE_INT)||rValueType.equals(ConstVar.TYPE_REAL)){
                element.setValue(String.valueOf(Double.parseDouble(rValue)));
            }
            else {
                addTypeError(lType,rValueType,rNode.getRowNum());
            }
        }
        else if(lType.equals(ConstVar.TYPE_BOOL)){
            if(rValueType.equals(ConstVar.TYPE_BOOL)){
                element.setValue(rValue);
            }else if(rValueType.equals(ConstVar.TYPE_INT)){
                int rIntValue = Integer.parseInt(rValue);
                if(rIntValue>0){
                    element.setValue(ConstVar.TRUE);
                }else {
                    element.setValue(ConstVar.FALSE);
                }
            }else {
                addTypeError(lType,rValueType,rNode.getRowNum());
            }
        }
        else if(lType.equals(ConstVar.TYPE_STRING)){
            if(rValueType.equals(ConstVar.TYPE_STRING)){
                element.setValue(rValue);
            }
            else {
                addTypeError(lType,rValueType,rNode.getRowNum());
            }
        }
    }

    private void for_stm(TreeNode node){
        // declareNode
        TreeNode initNode = node.getChildAt(0);
        // Condition
        TreeNode conditionNode = node.getChildAt(1);
        // Change
        TreeNode assignNode = node.getChildAt(2);
        // Statements
        TreeNode statementNode = node.getChildAt(3);
        // for循环语句初始化
        if(initNode.getContent().equals(ConstVar.DEClARE_STATEMENT)){
            declare_stm(initNode);
        }else if(initNode.getContent().equals(ConstVar.ASSIGN_STATEMENT)){
            assign_stm(initNode);
        }
        else {
            return;
        }
        // 条件为真
        while (condition(conditionNode.getChildAt(0))) {
            level++;
            statement(statementNode);
            level--;
            table.update(level);
            // for循环执行一次后改变循环条件中的变量
            assign_stm(assignNode);
        }
    }

    private void if_stm(TreeNode node){
        int count = node.getChildCount();
        // 根结点Condition
        TreeNode conditionNode = node.getChildAt(0);
        // 根结点Statements
        TreeNode statementNode = node.getChildAt(1);
        // 条件为真
        if (condition(conditionNode.getChildAt(0))) {
            statement(statementNode);
        } else if (count == 3) { // 条件为假且有else语句
            TreeNode elseNode = node.getChildAt(2);
            TreeNode elseStatementNode = elseNode.getChildAt(0);
            level++;
            statement(elseStatementNode);
            level--;
            table.update(level);
        } else { // 条件为假同时没有else语句
            return;
        }
    }

    private void while_stm(TreeNode node){
        // 根结点Condition
        TreeNode conditionNode = node.getChildAt(0);
        // 根结点Statements
        TreeNode statementNode = node.getChildAt(1);
        while (condition(conditionNode.getChildAt(0))) {
            level++;
            statement(statementNode);
            level--;
            table.update(level);
        }
    }

    private void read_stm(TreeNode node){
        TreeNode idNode = node.getChildAt(0);
        String idName;
        String idType;
        SymbolElement checkedID = doID(idNode,true);
        if(checkedID!=null){
            idName = checkedID.getName();
            idType = checkedID.getType();
            if(idType.equals(ConstVar.TYPE_BOOL_ARRAY)||idType.equals(ConstVar.TYPE_INT_ARRAY)
                    ||idType.equals(ConstVar.TYPE_STRING_ARRAY)||idType.equals(ConstVar.TYPE_REAL_ARRAY)){
                String error = "读入语句中，不能赋值给数组类型";
                addError(error,idNode.getRowNum());
                return;
            }
            SymbolElement element = table.getAllLevel(idName,level);
            try{
                Scanner scanner = new Scanner(System.in);
                String value = scanner.nextLine();
                if(idType.equals(ConstVar.TYPE_BOOL)){
                    value = String.valueOf(Boolean.parseBoolean(value));
                    element.setValue(value);
                }else if (idType.equals(ConstVar.TYPE_INT)){
                    value = String.valueOf(Integer.parseInt(value));
                    element.setValue(value);
                }else if(idType.equals(ConstVar.TYPE_STRING)){
                    element.setValue(value);
                }else if(idType.equals(ConstVar.TYPE_REAL)){
                    value = String.valueOf(Double.parseDouble(value));
                    element.setValue(value);
                }
            }catch (Exception e){
                System.out.println("input： 无效输入");
            }
        }
    }

    private void write_stm(TreeNode node){
        TreeNode valueNode = node.getChildAt(0);
        String type = valueNode.getType();
        String content = valueNode.getContent();
        if(type.equals(ConstVar.TYPE_INT)||type.equals(ConstVar.TYPE_REAL)
                ||type.equals(ConstVar.TYPE_STRING)||type.equals(ConstVar.TYPE_BOOL)){
            System.out.println("output: "+ content);
        }
        else if(type.equals(ConstVar.TYPE_ID)){
            SymbolElement checkedID = doID(valueNode,false);
            if(checkedID!=null){
                System.out.println("output: "+ checkedID.getValue());
            }
        }
        else if(calculateSignalSet.contains(content)){
            TreeNode result = expression(valueNode);
            if(result!=null){
                System.out.println("output: "+ result.getContent());
            }
        }
    }


}
