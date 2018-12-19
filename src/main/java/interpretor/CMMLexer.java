package interpretor;
import entity.Token;
import tools.ConstVar;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;

public class CMMLexer {
    private ArrayList<String> errorList = new ArrayList<String>();
    private ArrayList<Token> tokenList = new ArrayList<Token>();
    private ArrayList<Token> dispalyToken = new ArrayList<Token>();
    private boolean IsCommentState = false;

    private enum State {
        START, INT, REAL, LETTER, PLUS, MINUS, MUL, DIV, ASSIGN, LT, GT, QUOTATION
    }

    private static HashSet<String> charInLawSet = ConstVar.getCharInLawSet();
    private static HashSet<String> splitSignalSet = ConstVar.getSplitSignalSet();
    private static HashSet<String> blankSignalSet = ConstVar.getBlankSignalSet();

    public ArrayList<String> getErrorList() {
        return errorList;
    }

    public ArrayList<Token> getTokenList() {
        return tokenList;
    }

    public void setErrorList(ArrayList<String> errorList) {
        this.errorList = errorList;
    }

    /*识别字母*/
    private static boolean IsLetter(char c) {
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_') {
            return true;
        }
        return false;
    }


    private static boolean IsDigit(char c) {
        if (c >= '0' && c <= '9') {
            return true;
        }
        return false;
    }

    //判断是否是标准整数，排除001的情况
    private static boolean IsInteger(String numstr) {
        if (numstr.matches("^-?\\d+$") && !numstr.matches("^-?0{1,}\\d+$"))
            return true;
        else
            return false;
    }

    //判断是否是标准实数，排除00.001的情况
    private static boolean IsReal(String numstr) {
        if (numstr.matches("^(-?\\d+)(\\.\\d+)+$")
                && !numstr.matches("^(-?0{2,}+)(\\.\\d+)+$"))
            return true;
        else
            return false;
    }

    public static boolean IsIdentifier(String str){
        if (str.matches("^\\w+$") && IsLetter(str.charAt(0)))
            return true;
        else
            return false;
    }


    private static boolean IsReserved(String str) {
        if (str.equals(ConstVar.IF) || str.equals(ConstVar.ELSE) || str.equals(ConstVar.WHILE)
                || str.equals(ConstVar.READ) || str.equals(ConstVar.WRITE) || str.equals(ConstVar.INT)
                || str.equals(ConstVar.REAL) || str.equals(ConstVar.BOOL) || str.equals(ConstVar.STRING)
                || str.equals(ConstVar.TRUE) || str.equals(ConstVar.FALSE) || str.equals(ConstVar.FOR)) {
            return true;
        }
        return false;
    }

    private void AnalysisLine(String line, int rowNum) {
        //终结符号
        int token_start = 0;
        line +="\n";
        State state = State.START;
        int length = line.length();

        for (int i = 0; i < length; i++) {
            String currentChar = String.valueOf(line.charAt(i));
            if (!IsCommentState) {
                //合法输入
                if (charInLawSet.contains(currentChar) || IsDigit(line.charAt(i)) || IsLetter(line.charAt(i))) {
                    switch (state) {
                        case START:
                            //遇到不同的符号进入不同的状态
                            if (splitSignalSet.contains(currentChar)) {
                                state = State.START;
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_SEPARATOR, currentChar));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_SEPARATOR, currentChar));
                            } else if (currentChar.equals(ConstVar.PLUS)) {
                                state = State.PLUS;
                            } else if (currentChar.equals(ConstVar.MINUS)) {
                                state = State.MINUS;
                            } else if (currentChar.equals(ConstVar.MULTIPLY)) {
                                state = State.MUL;
                            } else if (currentChar.equals(ConstVar.DIVIDE)) {
                                state = State.DIV;
                            } else if (currentChar.equals(ConstVar.ASSIGN)) {
                                state = State.ASSIGN;
                            } else if (currentChar.equals(ConstVar.LT)) {
                                state = State.LT;
                            } else if (currentChar.equals(ConstVar.GT)) {
                                state = State.GT;
                            } else if (currentChar.equals(ConstVar.DOUBLE_QUOTATION)) {
                                state = State.QUOTATION;
                                token_start = i + 1;
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_SEPARATOR, ConstVar.DOUBLE_QUOTATION));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_SEPARATOR, ConstVar.DOUBLE_QUOTATION));
                            } else if (IsDigit(line.charAt(i))) {
                                token_start = i;
                                state = State.INT;
                            } else if (IsLetter(line.charAt(i))) {
                                token_start = i;
                                state = State.LETTER;
                            } else if (blankSignalSet.contains(currentChar)) {
                                state = State.START;
                            }
                            break;
                        case INT:
                            if (IsDigit(line.charAt(i))) {
                                state = State.INT;
                            } else if (currentChar.equals(ConstVar.DOT)) {
                                state = State.REAL;
                            } else if (IsLetter(line.charAt(i))) {
                                String error = "ERROR:在 " + rowNum + " 行, 第 " + i  + " 个字符: " + line.substring(token_start,i+1) + " 是非法数字表达";
                                errorList.add(error);
                                dispalyToken.add(new Token(rowNum,token_start,ConstVar.TYPE_ERROR,line.substring(token_start,i+1)));
                                state = State.START;
                            }
                            else {
                                String intStr = line.substring(token_start, i);
                                if (IsInteger(intStr)) {
                                    tokenList.add(new Token(rowNum, token_start, ConstVar.TYPE_INT, intStr));
                                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_INT, intStr));
                                } else {
                                    String error = "ERROR:在 " + rowNum + " 行, 第 " + token_start + " 个字符开始: " + intStr + " 是非法整数";
                                    errorList.add(error);
                                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_ERROR, line.substring(token_start, i)));
                                }
                                i--;
                                state = State.START;
                            }
                            break;
                        case REAL:
                            state = State.START;
                            if (currentChar.equals(ConstVar.DOT)) {
                                String error = "ERROR:在 " + rowNum + " 行, 第 " + i + " 个字符: " + currentChar + " 是非法实数表达，重复小数点";
                                errorList.add(error);
                                dispalyToken.add(new Token(rowNum,token_start,ConstVar.TYPE_ERROR,line.substring(token_start,i+1)));
                            } else if (IsDigit(line.charAt(i))) {
                                state = State.REAL;
                            } else if (IsLetter(line.charAt(i))) {
                                String error = "ERROR:在 " + rowNum + " 行, 第 " + i + " 个字符: " + currentChar + " 是非法实数表达";
                                errorList.add(error);
                                dispalyToken.add(new Token(rowNum,token_start,ConstVar.ENTER,line.substring(token_start,i+1)));
                            } else {

                                String rearStr = line.substring(token_start, i);
                                if (IsReal(rearStr)) {
                                    tokenList.add(new Token(rowNum, token_start, ConstVar.TYPE_REAL, line.substring(token_start, i)));
                                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_REAL, line.substring(token_start, i)));
                                } else {
                                    String error = "ERROR:在 " + rowNum + " 行, 第 " + token_start + " 个字符开始: " + rearStr + " 是非法实数";
                                    errorList.add(error);
                                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_ERROR, line.substring(token_start, i)));
                                }
                                i--;
                            }
                            break;
                        case LETTER:
                            if (IsLetter(line.charAt(i)) || IsDigit(line.charAt(i))) {
                                state = State.LETTER;
                            } else {

                                String id = line.substring(token_start, i);
                                if (IsReserved(id)) {
                                    tokenList.add(new Token(rowNum, token_start, ConstVar.TYPE_RESERVED, id));
                                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_RESERVED, id));
                                }
                                else if(IsIdentifier(id)) {
                                    tokenList.add(new Token(rowNum, token_start, ConstVar.TYPE_ID, id));
                                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_ID, id));
                                }
                                else{
                                    String error = "ERROR:在 " + rowNum + " 行, 第 " + token_start + " 个字符开始: " + id + " 是非法标识符";
                                    errorList.add(error);
                                    dispalyToken.add(new Token(rowNum,token_start,ConstVar.TYPE_ERROR,id));
                                }
                                i--;
                                state = State.START;
                            }
                            break;
                        case PLUS:
                            i--;
                            tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.PLUS));
                            dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.PLUS));
                            state = State.START;
                            break;
                        case MINUS:
                            String lastType = tokenList.get(tokenList.size()-1).getType();
                            String lastContent = tokenList.get(tokenList.size()-1).getContent();
                            i--;
                            if(lastContent.equals(ConstVar.BRACKET_RIGHT)||lastContent.equals(ConstVar.PAREN_RIGHT)
                                    ||lastType.equals(ConstVar.TYPE_INT)||lastType.equals(ConstVar.TYPE_REAL)||lastType.equals(ConstVar.TYPE_ID)){
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.MINUS));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.MINUS));
                                state = State.START;
                            }else {
                                //负数状态
                                token_start = i;
                                state = State.INT;
                            }

                            break;
                        case MUL:
                            if (currentChar.equals(ConstVar.DIVIDE)) {
                                String error = "ERROR: 在 " + rowNum + " 行, 第 " + i + " 个字符: " + " 注释符*/使用错误";
                                errorList.add(error);
                                dispalyToken.add(new Token(rowNum, i - 1, ConstVar.TYPE_ERROR, ConstVar.MUL_COMMENT_E));
                            } else {
                                i--;
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.MULTIPLY));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.MULTIPLY));
                            }
                            state = State.START;
                            break;
                        case DIV:
                            if (currentChar.equals(ConstVar.DIVIDE)) {
                                //to do single comment
                                dispalyToken.add(new Token(rowNum, i-1, ConstVar.TYPE_SINGAL_COMMENT_SIGNAL, ConstVar.LINE_COMMENT));
                                dispalyToken.add(new Token(rowNum, i+1, ConstVar.TYPE_COMMENT, line.substring(i + 1,length-1)));
                                i = length - 1;
                            } else if (currentChar.equals(ConstVar.MULTIPLY)) {
                                IsCommentState = true;
                                //to do mul comment
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_MUL_COMMENT_SIGNAL, ConstVar.MUL_COMMENT_S));
                                token_start = i + 1;
                            } else {
                                i--;
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.DIVIDE));
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.DIVIDE));
                            }
                            state = State.START;
                            break;
                        case ASSIGN:
                            if (currentChar.equals(ConstVar.ASSIGN)) {
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.EQUAL));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.EQUAL));
                            } else {
                                i--;
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.ASSIGN));
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.ASSIGN));
                            }
                            state = State.START;
                            break;
                        case LT:
                            if (currentChar.equals(ConstVar.ASSIGN)) {
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.LT_EQUAl));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.LT_EQUAl));
                            } else if (currentChar.equals(ConstVar.GT)) {
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.NEQUAL));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.NEQUAL));
                            } else {
                                i--;
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.LT));
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.LT));
                            }
                            state = State.START;
                            break;
                        case GT:
                            if (currentChar.equals(ConstVar.ASSIGN)) {
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.GT_EQUAL));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.GT_EQUAL));
                            } else {
                                i--;
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.GT));
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_OPERATION, ConstVar.GT));
                            }
                            state = State.START;
                            break;
                        case QUOTATION:
                            if (currentChar.equals(ConstVar.DOUBLE_QUOTATION)) {
                                tokenList.add(new Token(rowNum, token_start, ConstVar.TYPE_STRING, line.substring(token_start, i)));
                                tokenList.add(new Token(rowNum, i, ConstVar.TYPE_SEPARATOR, ConstVar.DOUBLE_QUOTATION));

                                dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_STRING, line.substring(token_start, i)));
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_SEPARATOR, ConstVar.DOUBLE_QUOTATION));
                                state = State.START;
                            } else if (i == length - 1) {
                                String error_str = line.substring(token_start);
                                String error = "ERROR:在 " + rowNum + " 行, 第 " + token_start + " 列: 字符串 " + error_str + " 缺少引号";
                                errorList.add(error);
                                dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_ERROR, error_str));
                            }
                            break;
                    }
                }
                //非法输入
                else {
                    String error = "ERROR:在 " + rowNum + " 行, 第 " + i + " 个字符: " + currentChar + " 是非法输入";
                    errorList.add(error);
                    dispalyToken.add(new Token(rowNum, i, ConstVar.TYPE_ERROR, currentChar));
                }
            }
            //多行注释情况
            else {
                if (currentChar.equals(ConstVar.MULTIPLY)) {
                    state = State.MUL;
                } else if (currentChar.equals(ConstVar.DIVIDE) && state == State.MUL) {
                    IsCommentState = false;
                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_COMMENT, line.substring(token_start, i-1)));
                    dispalyToken.add(new Token(rowNum, i - 1, ConstVar.TYPE_MUL_COMMENT_SIGNAL, ConstVar.MUL_COMMENT_E));
                    state = State.START;
                } else if (i == length - 2) {
                    dispalyToken.add(new Token(rowNum, token_start, ConstVar.TYPE_COMMENT, line.substring(token_start, length - 1)));
                    state = State.START;
                }
            }
        }
    }

    public void Print_Lexer_Result(){
        for (Token token : dispalyToken) {
            System.out.println("row: " + token.getRow() + " col: " + token.getColumn() + " type: " + token.getType() + " content: " + token.getContent());
        }
        if (errorList.size()>0){
            System.out.println("词法分析报错： ");
            for (String string: errorList){
                System.out.println(string);
            }
        }
    }

    public void Lexer_Analysis(String path) {
        errorList.clear();
        tokenList.clear();
        dispalyToken.clear();
        IsCommentState = false;
        String line = "";
        int lineNum = 1;
        try {
            File file = new File(path);
            if(!file.isFile()||!file.exists()){
                System.out.println("文件不存在");
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"GBK"));
            while((line = reader.readLine()) != null){
                if (IsCommentState && !line.contains(ConstVar.MUL_COMMENT_E)) {
                    dispalyToken.add(new Token(lineNum, 0, ConstVar.TYPE_COMMENT, line));
                } else {
                    AnalysisLine(line, lineNum);
                }
                lineNum++;
            }
            if (IsCommentState){
                String error = "ERROR: /*多行注释符号未闭合";
                errorList.add(error);
            }
        } catch (IOException e) {
            System.err.println("读取文本失败");
        }
    }


}
