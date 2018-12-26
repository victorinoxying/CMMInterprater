package interpretor;
import entity.Token;
import entity.TreeNode;
import tools.ConstVar;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

/**
 *
 */
public class CMMParser {
    //从词法分析取得的token流
    private ArrayList<Token> tokenList;
    //错误列表
    private ArrayList<String> errorList;
    //语法树根节点
    private TreeNode root;
    //当前分析的token
    private Token currentToken = null;
    //分析到第几个token的标尺
    private int index = 0;

    private static HashSet<String> compareSignalSet = ConstVar.getCompareSignalSet();
    private static HashSet<String> declareSignalSet = ConstVar.getDeclareSignalSet();
    private static HashSet<String> valueTypeSignalSet = ConstVar.getValueTypeSignalSet();
    public CMMParser(ArrayList<Token> LexerResult){
        tokenList = LexerResult;
        errorList = new ArrayList<String>();
        root = new TreeNode(ConstVar.TYPE_ROOT);
        if(tokenList.size()>0){
            currentToken = tokenList.get(0);
        }
    }

    public ArrayList<String> getErrorList() {
        return errorList;
    }

    public ArrayList<Token> getTokenList() {
        return tokenList;
    }

    public TreeNode getRoot() {
        return root;
    }

    public TreeNode Parser(){
        while(index<tokenList.size()){
            root.add(statement());
        }
        return root;
    }

    public void PrintGrammerTree(){
        Enumeration<TreeNode> enumeration = root.preorderEnumeration();
        while(enumeration.hasMoreElements()){
            TreeNode node = enumeration.nextElement();
            for (int i=0;i<node.getLevel();i++){
                System.out.print(" ");
            }
            System.out.print(node.getType()+':'+ node.getContent());
            System.out.print('\n');
        }
        if(errorList.size()>0){
            System.out.println("语法分析报错：");
            for(String error :errorList){
                System.out.println(error);
            }
        }
    }

    private void addError(String errorInfo){
        Token errorToken = tokenList.get(index-1);
        if(errorToken == null){
            errorToken = tokenList.get(tokenList.size()-1);
        }
        String positionInfo = "ERROR: 在第 " + errorToken.getRow()+ " 行， 第 "+errorToken.getColumn()+" 列: ";
        String error = positionInfo + errorInfo;
        errorList.add(error);
    }

    private void nextToken(){
        index++;
        if (index> tokenList.size()-1){
            currentToken = null;
            return;
        }
        currentToken = tokenList.get(index);
    }

    /**
     * statement: if_stm | while_stm | read_stm | write_stm | assign_stm | declare_stm | for_stm;
     * @return
     */
    private TreeNode statement(){
        TreeNode resultNode = null;
        //assign
        if(currentToken != null && currentToken.getType().equals(ConstVar.TYPE_ID)){
            resultNode = assign_statement(false);

        }
        //declare
        else if (currentToken != null && declareSignalSet.contains(currentToken.getContent())){
            resultNode = declare_statement(false);
        }
        //for
        else if (currentToken != null && currentToken.getContent().equals(ConstVar.FOR)){
            resultNode = for_statement();
        }
        //If
        else if (currentToken != null && currentToken.getContent().equals(ConstVar.IF)) {
            resultNode = if_statement();
        }
        //while
        else if(currentToken!=null&& currentToken.getContent().equals(ConstVar.WHILE)){
            resultNode = while_statement();
        }
        //read
        else if(currentToken!= null&& currentToken.getContent().equals(ConstVar.READ)){
            resultNode = read_statemnet();
        }
        //write
        else if(currentToken!=null&& currentToken.getContent().equals(ConstVar.WRITE)){
            resultNode = write_statement();
        }
        //error
        else {
            String error = " 语句以错误的token开始";
            addError(error);
            resultNode = new TreeNode(error,ConstVar.TYPE_ERROR);
            nextToken();
        }
        return resultNode;
    }

    /**
     * declare_stm->(INT | REAL | BOOL | STRING)(ID|ID array)(ASSIGN condition) SEMICOLON;
     * @return treeNode
     */
    private TreeNode declare_statement(boolean isFor){
        TreeNode resultNode = new TreeNode(ConstVar.DEClARE_STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        //匹配 reserved
        TreeNode declareNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_RESERVED, currentToken.getRow());
        resultNode.add(declareNode);
        nextToken();
        // 匹配 id
        if (currentToken.getType().equals(ConstVar.TYPE_ID)) {
            TreeNode idNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_ID, currentToken.getRow());
            resultNode.add(idNode);
            nextToken();
            // 处理array的情况
            if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACKET_LEFT)) {
                idNode.add(array(true));
            }
            else if (currentToken != null
                    && !currentToken.getContent().equals(ConstVar.ASSIGN)
                    && !currentToken.getContent().equals(ConstVar.SEMICOLON)
                    && !currentToken.getContent().equals(ConstVar.COMMA)) {
                String error = " 声明语句出错,标识符后出现不正确的token";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR,currentToken.getRow()));
                nextToken();
            }
        }
        else {
            String error = " 声明语句中标识符出错";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            nextToken();
        }
        // 匹配赋值符号= condition
        if (currentToken != null && currentToken.getContent().equals(ConstVar.ASSIGN)) {
            TreeNode assignNode = new TreeNode(ConstVar.ASSIGN,ConstVar.TYPE_SEPARATOR,currentToken.getRow());
            resultNode.add(assignNode);
            nextToken();
            assignNode.add(condition());
        }
        //不在for循环中匹配分号
        if(!isFor){
            // 匹配分号;
            if (currentToken != null && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
                nextToken();
            } else {
                String error = " 声明语句缺少分号 ';'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            }
        }
        return resultNode;
    }


    /**
     * assign_stm->(ID | ID array) ASSIGN expression SEMICOLON;
     * @param isFor
     * @return
     */
    private TreeNode assign_statement(boolean isFor){
        TreeNode resultNode = new TreeNode(ConstVar.ASSIGN_STATEMENT, ConstVar.TYPE_STATEMENT,currentToken.getRow());
        TreeNode assignNode = new TreeNode(ConstVar.ASSIGN,ConstVar.TYPE_SEPARATOR,currentToken.getRow());
        TreeNode idNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_ID,currentToken.getRow());
        nextToken();
        //数组处理
        if(currentToken!= null&& currentToken.getContent().equals(ConstVar.BRACKET_LEFT)){
            idNode.add(array(false));
        }
        // 添加id节点
        assignNode.add(idNode);
        if(currentToken!= null&& currentToken.getContent().equals(ConstVar.ASSIGN)){
            nextToken();
        }
        else {
            String error = " 赋值语句缺少赋值符号 '='";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        //expression匹配
        assignNode.add(condition());
        resultNode.add(assignNode);
        //不在for循环结构体内，匹配;
        if(!isFor){
            if(currentToken!= null&& currentToken.getContent().equals(ConstVar.SEMICOLON)){
                nextToken();
            }
            else {
                String error = " 赋值语句缺少分号 ';'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
                return resultNode;
            }
        }
        return resultNode;
    }

    /*
     *if_stm: IF LPAREN condition RPAREN LBRACE statement RBRACE (ELSE LBRACE statement RBRACE);
     * @return
     */
    private TreeNode if_statement(){
        // 是否有大括号
        boolean ifHasBrace = true;
        boolean elseHasBrace =true;
        TreeNode resultNode = new TreeNode(ConstVar.IF_STATEMENT,ConstVar.STATEMENT,currentToken.getRow());
        nextToken();
        //左括号
        if(currentToken!= null && currentToken.getContent().equals(ConstVar.PAREN_LEFT)){
            nextToken();
        }
        else {
            String error = " if语句缺少左括号 '('";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
        }
        //condition
        if(currentToken == null){
            String error = " if语句缺少条件语句和右括号 ')'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        TreeNode conditionNode = new TreeNode(ConstVar.CONDITION_STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        conditionNode.add(condition());
        resultNode.add(conditionNode);

        if(currentToken!=null && currentToken.getContent().equals(ConstVar.PAREN_RIGHT)){
            nextToken();
        }
        else {
            String error = " if语句缺少右括号 ')'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
        }
        // 匹配左大括号{
        if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_LEFT)) {
            nextToken();
        } else {
            ifHasBrace = false;
        }
        // statement
        TreeNode statementNode = new TreeNode(ConstVar.STATEMENT, ConstVar.TYPE_STATEMENT);
        resultNode.add(statementNode);
        if (ifHasBrace) {
            while (currentToken != null) {
                if (!currentToken.getContent().equals(ConstVar.BRACE_RIGHT)){
                    statementNode.add(statement());
                }
                else if (statementNode.getChildCount() == 0) {
                    resultNode.remove(resultNode.getChildCount() - 1);
                    statementNode.setContent(ConstVar.EMPTY_STM);
                    resultNode.add(statementNode);
                    break;
                } else {
                    break;
                }
            }
            // 匹配右大括号}
            if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_RIGHT)) {
                nextToken();
            } else {
                String error = " if语句缺少右大括号 '}'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            }
        }
        //无大括号
        else {
            if (currentToken != null)
                statementNode.add(statement());
        }
        //else
        if (currentToken != null && currentToken.getContent().equals(ConstVar.ELSE)) {
            nextToken();
            TreeNode elseStatementNode = new TreeNode(ConstVar.ELSE_STATEMENT,ConstVar.TYPE_STATEMENT);
            // 匹配左大括号{
            if (currentToken!= null &&currentToken.getContent().equals(ConstVar.BRACE_LEFT)) {
                nextToken();
            }
            else {
                elseHasBrace = false;
            }
            if (elseHasBrace) {
                // statement
                while (currentToken != null) {
                    if (!currentToken.getContent().equals(ConstVar.BRACE_RIGHT))
                        elseStatementNode.add(statement());
                    else if (elseStatementNode.getChildCount() == 0) {
                        TreeNode node = new TreeNode(ConstVar.EMPTY_STM,ConstVar.TYPE_STATEMENT);
                        elseStatementNode.add(node);
                        break;
                    } else {
                        break;
                    }
                }
                // 匹配右大括号}
                if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_RIGHT)) {
                    nextToken();
                }
                else {
                    String error = " else语句缺少右大括号 '}'";
                    addError(error);
                    elseStatementNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
                }
            }
            else {
                if (currentToken != null)
                    elseStatementNode.add(statement());
            }
            resultNode.add(elseStatementNode);
        }
        return resultNode;
    }

    /**
     * while_stm-> WHILE LPAREN condition RPAREN LBRACE statement RBRACE;
     * @return treeNode
     */
    private TreeNode while_statement(){
        // 是否有大括号,默认为true
        boolean hasBrace = true;
        // while函数返回结点的根结点
        TreeNode resultNode = new TreeNode(ConstVar.WHILE_STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        nextToken();
        // 匹配左括号(
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_LEFT)) {
            nextToken();
        } else { // 报错
            String error = " while循环缺少左括号'('";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
        }
        if(currentToken ==null){
            String error = " while循环缺少左括号'('和条件语句";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        // condition
        TreeNode conditionNode = new TreeNode(ConstVar.CONDITION_STATEMENT,ConstVar.TYPE_STATEMENT);
        resultNode.add(conditionNode);
        conditionNode.add(condition());
        // 匹配右括号)
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_RIGHT)) {
            nextToken();
        } else {
            String error = " while循环缺少右括号 ')'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
        }
        // 匹配左大括号{
        if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_LEFT)) {
            nextToken();
        } else {
            hasBrace = false;
        }
        if (currentToken ==  null){
            String error = " while循环未正确闭合";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        // statement
        TreeNode statementNode = new TreeNode(ConstVar.STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        resultNode.add(statementNode);
        if(hasBrace) {
            while (currentToken != null) {
                if (!currentToken.getContent().equals(ConstVar.BRACE_RIGHT))
                    statementNode.add(statement());
                else if (statementNode.getChildCount() == 0) {
                    resultNode.remove(resultNode.getChildCount() - 1);
                    statementNode.setContent(ConstVar.EMPTY_STM);
                    resultNode.add(statementNode);
                    break;
                } else {
                    break;
                }
            }
            // 匹配右大括号}
            if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_RIGHT)) {
                nextToken();
            } else {
                String error = " while循环缺少右大括号 '}'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            }
        } else {
            if(currentToken != null)
                statementNode.add(statement());
        }
        return resultNode;
    }

    /**
     * read_stm->READ LPAREN ID RPAREN SEMICOLON;
     * @return
     */
    private TreeNode read_statemnet(){
        // 保存要返回的结点
        TreeNode resultNode = new TreeNode(ConstVar.READ_STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        TreeNode idNode =null;
        nextToken();
        // 匹配左括号(
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_LEFT)) {
            nextToken();
        } else {
            String error = " read语句缺少左括号 '('";
            addError(error);
            return new TreeNode(error,ConstVar.TYPE_ERROR);
        }
        // 匹配标识符
        if (currentToken != null && currentToken.getType().equals(ConstVar.TYPE_ID)) {
            idNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_ID,currentToken.getRow());
            nextToken();
            // 判断是否是为数组赋值
            if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACKET_LEFT)) {
                idNode.add(array(false));
            }
        } else {
            String error = " ead语句左括号后不是标识符";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        resultNode.add(idNode);
        // 匹配右括号)
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_RIGHT)) {
            nextToken();
        } else {
            String error = " read语句缺少右括号 ')'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        // 匹配分号;
        if (currentToken != null && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
            nextToken();
        } else {
            String error = " read语句缺少分号 ';'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        return resultNode;
    }

    /**
     * write_stm-> WRITE LPAREN expression RPAREN SEMICOLON;
     * @return
     */
    private TreeNode write_statement(){
        // 保存要返回的结点
        TreeNode resultNode = new TreeNode(ConstVar.WRITE_STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        TreeNode expressionNode = null;
        nextToken();
        // 匹配左括号(
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_LEFT)) {
            nextToken();
        } else {
            String error = " write语句缺少左括号 '('";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        if(currentToken == null){
            String error = " write语句未正常闭合";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        // 调用expression函数匹配表达式
        expressionNode = expression();
        resultNode.add(expressionNode);
        // 匹配右括号)
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_RIGHT)) {
            nextToken();
        } else {
            String error = "write语句缺少右括号 ’)'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        // 匹配分号;
        if (currentToken != null && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
            nextToken();
        } else {
            String error = " write语句缺少分号';'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        return resultNode;
    }

    /**
     * condition->expression (comparison_op expression)*;
     * @return
     */
    private TreeNode condition(){
        TreeNode conditionNode = expression();
        //逻辑运算expression
        if(currentToken!= null&& compareSignalSet.contains(currentToken.getContent())){
            TreeNode compareSignalNode = comparison_op();
            compareSignalNode.add(conditionNode);
            compareSignalNode.add(expression());
            return compareSignalNode;
        }
        //bool值
        return conditionNode;
    }

    private TreeNode comparison_op(){
        TreeNode resultNode = null;
        if(currentToken!= null&& currentToken.getContent().equals(ConstVar.LT)){
            resultNode = new TreeNode(ConstVar.LT,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null&& currentToken.getContent().equals(ConstVar.GT)){
            resultNode = new TreeNode(ConstVar.GT,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null&& currentToken.getContent().equals(ConstVar.LT_EQUAl)){
            resultNode = new TreeNode(ConstVar.LT_EQUAl,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null&& currentToken.getContent().equals(ConstVar.GT_EQUAL)){
            resultNode = new TreeNode(ConstVar.GT_EQUAL,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null&& currentToken.getContent().equals(ConstVar.EQUAL)){
            resultNode = new TreeNode(ConstVar.EQUAL,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null&& currentToken.getContent().equals(ConstVar.NEQUAL)){
            resultNode = new TreeNode(ConstVar.NEQUAL,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else {
            String error = " 比较运算符错误";
            addError(error);
            resultNode = new TreeNode(error,ConstVar.TYPE_ERROR);
        }
        return resultNode;
    }

    /**
     * isDeclare:是否在声明语句中，如果是声明语句[expression]中表达式可为空
     * array -> LBRACKET (expression| null) RBRACKET;
     * @return
     */
    private TreeNode array( boolean isDeclare){
        TreeNode resultNode = new TreeNode(ConstVar.ARRAY_STATEMENT,ConstVar.TYPE_ARRAY);
        //[在调用的时候已经匹配过了
        nextToken();
        if(currentToken.getContent().equals(ConstVar.BRACKET_RIGHT)){
            String error;
            if(isDeclare){
                error = "数组下标为空，是非法的数组声明";
            }
            else {
                error = "数组下标为空，无法找到正确元素";
            }
            addError(error);
            resultNode = new TreeNode(error,ConstVar.TYPE_ERROR);
            nextToken();
            return resultNode;
        }
        //匹配表达式
        resultNode.add(expression());
        if(currentToken!= null&& currentToken.getContent().equals(ConstVar.BRACKET_RIGHT)){
            nextToken();
        }
        else {
            String error = " 缺少右中括号 ']'";
            addError(error);
            resultNode = new TreeNode(error,ConstVar.TYPE_ERROR);
        }
        return resultNode;
    }

    /**
     * expression: term (add_op term)*;
     * @return treenode
     */
    private TreeNode expression(){
        TreeNode resultNode = term();
        while(currentToken!= null && (currentToken.getContent().equals(ConstVar.PLUS)||currentToken.getContent().equals(ConstVar.MINUS))){
            TreeNode addNode = add_op();
            addNode.add(resultNode);
            resultNode = addNode;
            resultNode.add(term());
        }
        return resultNode;
    }

    /**
     * add_op->PLUS | MINUS;
     * @return
     */
    private TreeNode add_op(){
        TreeNode resultNode = null;
        if(currentToken!= null && currentToken.getContent().equals(ConstVar.PLUS)){
            resultNode = new TreeNode(ConstVar.PLUS,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null && currentToken.getContent().equals(ConstVar.MINUS)){
            resultNode = new TreeNode(ConstVar.MINUS,ConstVar.TYPE_OPERATION,currentToken.getRow());
            nextToken();
        }
        else{
            String error = " 加减符号出错";
            addError(error);
            resultNode = new TreeNode( "加减符号出错",ConstVar.TYPE_ERROR);
        }
        return resultNode;
    }


    /**
     * term ->factor (mul_op factor)*;
     * @return
     */
    private TreeNode term(){
        TreeNode resultNode = factor();
        while(currentToken!= null && (currentToken.getContent().equals(ConstVar.MULTIPLY)||currentToken.getContent().equals(ConstVar.DIVIDE))) {
            TreeNode mulNode = mul_op();
            mulNode.add(resultNode);
            resultNode = mulNode;
            resultNode.add(factor());
        }
        return resultNode;

    }

    /**
     * mul_op ->TIMES | DIVIDE;
     * @return treenode
     */
    private TreeNode mul_op(){
        // 保存要返回的结点
        TreeNode resultNode = null;
        if (currentToken!= null && currentToken.getContent().equals(ConstVar.MULTIPLY)) {
            resultNode = new TreeNode(ConstVar.MULTIPLY,ConstVar.TYPE_OPERATION, currentToken.getRow());
            nextToken();
        }
        else if (currentToken!= null && currentToken.getContent().equals(ConstVar.DIVIDE)) {
            resultNode = new TreeNode( ConstVar.DIVIDE,ConstVar.TYPE_OPERATION, currentToken.getRow());
            nextToken();
        }
        else {
            String error = " 乘除符号出错";
            addError(error);
            resultNode = new TreeNode( "乘除符号出错",ConstVar.TYPE_ERROR);
        }
        return resultNode;
    }

    /**
     * factor -> TRUE | FALSE | REAL_LITERAL | INTEGER_LITERAL | ID | LPAREN expression RPAREN | DQ string DQ | ID array| LBRACE( INTEGER_LITERAL COMMA)RBRACE;
     * @return TreeNode
     */
    private TreeNode factor(){
        TreeNode resultNode = null;
        if(currentToken!= null && currentToken.getType().equals(ConstVar.TYPE_INT)){
            resultNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_INT,currentToken.getRow());
            nextToken();
        }
        else if(currentToken!= null && currentToken.getType().equals(ConstVar.TYPE_REAL)){
            resultNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_REAL,currentToken.getRow());
            nextToken();
        }
        else if (currentToken!= null && currentToken.getContent().equals(ConstVar.TRUE)||currentToken.getContent().equals(ConstVar.FALSE)){
            resultNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_BOOL,currentToken.getRow());
            nextToken();
        }
        //标识符
        else if(currentToken!= null && currentToken.getType().equals(ConstVar.TYPE_ID)){
            resultNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_ID,currentToken.getRow());
            nextToken();
            //标识符情况下，要考虑数组id array
            if(currentToken!= null && currentToken.getContent().equals(ConstVar.BRACKET_LEFT)){
                resultNode.add(array(false));
            }
        }
        //小括号
        else if(currentToken!= null && currentToken.getContent().equals(ConstVar.PAREN_LEFT)){
            nextToken();
            resultNode = expression();
            //匹配右括号
            if(currentToken!= null && currentToken.getContent().equals(ConstVar.PAREN_RIGHT)){
                nextToken();
            }
            //缺少右括号报错
            else {
                String error = "表达式缺少右括号 ')'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
                nextToken();
            }
        }
        //字符串
        else if(currentToken!= null && currentToken.getContent().equals(ConstVar.DOUBLE_QUOTATION)){
            resultNode = doString();
        }
        //数组
        else if (currentToken!=null &&currentToken.getContent().equals(ConstVar.BRACE_LEFT)){
            nextToken();
            resultNode = new TreeNode(ConstVar.ARRAY_STATEMENT,ConstVar.TYPE_ARRAY,currentToken.getRow());
            if (currentToken!=null &&(valueTypeSignalSet.contains(currentToken.getType())||currentToken.getContent().equals(ConstVar.FALSE)
                    ||currentToken.getContent().equals(ConstVar.TRUE)|| currentToken.getContent().equals(ConstVar.DOUBLE_QUOTATION))){
                //字符串数组第一个值
                if(currentToken.getContent().equals(ConstVar.DOUBLE_QUOTATION)){
                    resultNode.add(doString());
                }
                //其余类型数组第一个值
                else {
                    String type;
                    if(currentToken.getContent().equals(ConstVar.TRUE)||currentToken.getContent().equals(ConstVar.FALSE)){
                        type = ConstVar.TYPE_BOOL;
                    }
                    else {
                        type = currentToken.getType();
                    }
                    TreeNode elementNode = new TreeNode(currentToken.getContent(),type,currentToken.getRow());
                    resultNode.add(elementNode);
                    nextToken();

                }
            }
            else {
                if(currentToken==null){
                    String error = "数组赋值表达式未正常闭合";
                    addError(error);
                    resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
                    return resultNode;
                }
                String error = "数组赋值表达式只能使用常量";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
                nextToken();
            }
            while (currentToken!=null &&currentToken.getContent().equals(ConstVar.COMMA)){
                nextToken();
                if (currentToken!=null &&(valueTypeSignalSet.contains(currentToken.getType())||currentToken.getContent().equals(ConstVar.FALSE)
                        ||currentToken.getContent().equals(ConstVar.TRUE)|| currentToken.getContent().equals(ConstVar.DOUBLE_QUOTATION))){
                    if(currentToken.getContent().equals(ConstVar.DOUBLE_QUOTATION)){
                        resultNode.add(doString());
                    }
                    else {
                        String type;
                        if(currentToken.getContent().equals(ConstVar.TRUE)||currentToken.getContent().equals(ConstVar.FALSE)){
                            type = ConstVar.TYPE_BOOL;
                        }
                        else {
                            type = currentToken.getType();
                        }
                        TreeNode elementNode = new TreeNode(currentToken.getContent(),type,currentToken.getRow());
                        resultNode.add(elementNode);
                        nextToken();
                    }
                }
                else {
                    String error = "数组赋值表达式只能使用常量";
                    addError(error);
                    resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
                    while(currentToken != null && !currentToken.getContent().equals(ConstVar.BRACE_RIGHT)){
                        nextToken();
                    }
                    break;
                }
            }
            if(currentToken!= null&& currentToken.getContent().equals(ConstVar.BRACE_RIGHT)){
                nextToken();
            }
            else {
                String error = "数组赋值表达式缺少 '}'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            }
        }
        else {
            if(currentToken!= null && currentToken.getContent().equals(ConstVar.SEMICOLON)){
                nextToken();
            }
            String error = "表达式有误";
            addError(error);
            resultNode = new TreeNode(error,ConstVar.TYPE_ERROR);
        }
        return resultNode;
    }

    /** 处理token流中的字符串
     * @return
     */
    private TreeNode doString(){
        TreeNode resultNode = new TreeNode(ConstVar.NULL,ConstVar.TYPE_STRING,currentToken.getRow());
        nextToken();
        if(currentToken!= null&& currentToken.getContent().equals(ConstVar.DOUBLE_QUOTATION)){
            nextToken();
        }
        else{
            resultNode = new TreeNode(currentToken.getContent(),ConstVar.TYPE_STRING,currentToken.getRow());
            nextToken();
            //匹配右引号
            nextToken();
        }
        return resultNode;
    }

    /**
     * for_stm -> FOR LPAREN (declare_stm | assign_stm) SEMICOLON condition SEMICOLON assign_stm RPAREN LBRACE statement RBRACE;
     * @return treenode
     */
    private TreeNode for_statement(){
        boolean IsBrace = true;
        TreeNode resultNode = new TreeNode(ConstVar.FOR_STATEMENT,ConstVar.TYPE_STATEMENT,currentToken.getRow());
        nextToken();
        // 匹配左括号(
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_LEFT)) {
            nextToken();
        } else {
            String error = "for循环语句缺少左括号 '('";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
        }
        //assign statement
        if(currentToken != null && currentToken.getType().equals(ConstVar.TYPE_ID)){
            resultNode.add(assign_statement(true));
        }
        //or declare statement
        else if (currentToken != null && declareSignalSet.contains(currentToken.getContent())){
            resultNode.add(declare_statement(true));
        }
        else {
            String error = "for循环语句中initialization语句不是声明或赋值语句";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            while(currentToken!= null &&!currentToken.getContent().equals(ConstVar.SEMICOLON) && !currentToken.getContent().equals(ConstVar.PAREN_RIGHT)){
                nextToken();
            }
        }
        // 匹配分号;
        if (currentToken != null && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
            nextToken();
        }
        else {
            String error = "for循环语句缺少分号 ';'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        // condition
        if(currentToken!=null && !currentToken.getContent().equals(ConstVar.SEMICOLON) ){
            TreeNode conditionNode = new TreeNode(ConstVar.CONDITION_STATEMENT, ConstVar.TYPE_STATEMENT);
            conditionNode.add(condition());
            resultNode.add(conditionNode);
        }
        else {
            String error = "for循环语句缺少终结条件";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }

        // 匹配分号;
        if (currentToken != null && currentToken.getContent().equals(ConstVar.SEMICOLON)) {
            nextToken();
        } else {
            String error = "for循环语句缺少分号 ';'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            return resultNode;
        }
        //change
        if (currentToken!=null && !currentToken.getContent().equals(ConstVar.SEMICOLON)){
            resultNode.add(assign_statement(true));
        }
        // 匹配右括号)
        if (currentToken != null && currentToken.getContent().equals(ConstVar.PAREN_RIGHT)) {
            nextToken();
        } else {
            String error = " for循环语句缺少右括号 ')'";
            addError(error);
            resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
        }
        // 匹配左大括号{
        if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_LEFT)) {
            nextToken();
        } else {
            IsBrace = false;
        }
        // statement
        TreeNode statementNode = new TreeNode(ConstVar.STATEMENT, ConstVar.TYPE_STATEMENT);
        resultNode.add(statementNode);
        if(IsBrace) {
            while (currentToken != null) {
                if (!currentToken.getContent().equals(ConstVar.BRACE_RIGHT))
                    statementNode.add(statement());
                else if (statementNode.getChildCount() == 0) {
                    resultNode.remove(resultNode.getChildCount() - 1);
                    statementNode.setContent(ConstVar.EMPTY_STM);
                    resultNode.add(statementNode);
                    break;
                } else {
                    break;
                }
            }
            // 匹配右大括号}
            if (currentToken != null && currentToken.getContent().equals(ConstVar.BRACE_RIGHT)) {
                nextToken();
            } else {
                String error = "for循环语句缺少右大括号 '}'";
                addError(error);
                resultNode.add(new TreeNode(error,ConstVar.TYPE_ERROR));
            }
        }
        else {
            statementNode.add(statement());
        }
        return resultNode;
    }
}
