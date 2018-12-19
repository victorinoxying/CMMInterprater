package tools;
import java.util.HashMap;
import java.util.HashSet;


public class ConstVar {
    public static final int MAXSIZE = 1024*1024*1024;
    /* 运算符 */
    public static final String PLUS = "+";
    public static final String MINUS = "-";
    public static final String DIVIDE = "/";
    public static final String MULTIPLY = "*";
    public static final String LT = "<";
    public static final String GT = ">";
    public static final String EQUAL = "==";
    public static final String NEQUAL = "<>";
    public static final String LT_EQUAl = "<=";
    public static final String GT_EQUAL = ">=";
    public static final String ASSIGN = "=";

    /* 保留字 */
    public static final String READ = "read";
    public static final String WRITE = "write";
    public static final String WHILE = "while";
    public static final String IF = "if";
    public static final String FOR = "for";
    public static final String ELSE = "else";
    public static final String INT = "int";
    public static final String REAL = "real";
    public static final String BOOL = "bool";
    public static final String STRING = "string";
    public static final String TRUE = "true";
    public static final String FALSE = "false";

    /* 分隔符*/
    public static final String DOUBLE_QUOTATION = "\"";
    public static final String BRACE_RIGHT = "}";
    public static final String BRACE_LEFT = "{";
    public static final String PAREN_RIGHT = ")";
    public static final String PAREN_LEFT = "(";
    public static final String BRACKET_RIGHT = "]";
    public static final String BRACKET_LEFT = "[";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String SEMICOLON = ";";
    public static final String TAB = "\t";
    public static final String BLANK = " ";
    public static final String ENTER = "\r";
    public static final String NEWLINE = "\n";

    /* 注释符*/
    public static final String LINE_COMMENT = "//";
    public static final String MUL_COMMENT_S = "/*";
    public static final String MUL_COMMENT_E = "*/";

    /* 语句类型*/
    public static final String STATEMENT = "statement_stm";
    public static final String CONDITION_STATEMENT = "condition_stm";
    public static final String DEClARE_STATEMENT = "declare_stm";
    public static final String WHILE_STATEMENT = "while_stm";
    public static final String ASSIGN_STATEMENT = "assign_stm";
    public static final String FOR_STATEMENT = "for_stm";
    public static final String IF_STATEMENT = "if_stm";
    public static final String ELSE_STATEMENT = "else_stm";
    public static final String READ_STATEMENT = "read_stm";
    public static final String WRITE_STATEMENT = "write_stm";
    public static final String ARRAY_STATEMENT = "array_stm";



    /* 标识符类型 */
    public static final String TYPE_OPERATION = "OPERATION";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_SEPARATOR = "SEPARATOR";
    public static final String TYPE_RESERVED ="RESERVED" ;
    public static final String TYPE_ID = "IDENTIFIER";
    public static final String TYPE_COMMENT = "COMMENT";
    public static final String TYPE_SINGAL_COMMENT_SIGNAL = "SINGAL_COMMENT_SIGNAL";
    public static final String TYPE_MUL_COMMENT_SIGNAL = "MUL_COMMENT_SIGNAL";
    public static final String TYPE_INT = "INTEGER";
    public static final String TYPE_REAL = "REAL";
    public static final String TYPE_STRING = "STRING";
    public static final String TYPE_ROOT = "ROOT";
    public static final String TYPE_BOOL = "BOOL";
    public static final String TYPE_STATEMENT = "STATEMENT";
    public static final String TYPE_ARRAY = "ARRAY";
    public static final String TYPE_INT_ARRAY = "INT*" ;
    public static final String TYPE_REAL_ARRAY = "REAL*" ;
    public static final String TYPE_STRING_ARRAY = "STRING*" ;
    public static final String TYPE_BOOL_ARRAY = "BOOL*" ;

    /* 空集*/
    public static final String EMPTY_STM = "empty";
    public static final String NULL = "";

    public static HashSet<String> getCharInLawSet() {
        HashSet<String> charInLawSet = new HashSet<String>();
        charInLawSet.add(SEMICOLON);charInLawSet.add(COMMA);
        charInLawSet.add(BRACE_LEFT);charInLawSet.add(BRACE_RIGHT);
        charInLawSet.add(BRACKET_LEFT);charInLawSet.add(BRACKET_RIGHT);
        charInLawSet.add(PAREN_LEFT);charInLawSet.add(PAREN_RIGHT);
        charInLawSet.add(PLUS);charInLawSet.add(MINUS);
        charInLawSet.add(MULTIPLY);charInLawSet.add(DIVIDE);
        charInLawSet.add(ASSIGN);
        charInLawSet.add(LT);charInLawSet.add(GT);
        charInLawSet.add(DOUBLE_QUOTATION);
        charInLawSet.add(ENTER);
        charInLawSet.add(TAB);
        charInLawSet.add(BLANK);
        charInLawSet.add(NEWLINE);
        charInLawSet.add(DOT);
        return charInLawSet;
    }
    public static HashSet<String>getSplitSignalSet(){
        HashSet<String> splitSignakSet = new HashSet<String>();
        splitSignakSet.add(SEMICOLON);splitSignakSet.add(COMMA);
        splitSignakSet.add(BRACE_LEFT);splitSignakSet.add(BRACE_RIGHT);
        splitSignakSet.add(BRACKET_LEFT);splitSignakSet.add(BRACKET_RIGHT);
        splitSignakSet.add(PAREN_LEFT);splitSignakSet.add(PAREN_RIGHT);
        return splitSignakSet;
    }

    public static HashSet<String>getBlankSignalSet(){
        HashSet<String> blankSignalSet = new HashSet<String>();
        blankSignalSet.add(TAB);blankSignalSet.add(BLANK);
        blankSignalSet.add(NEWLINE);blankSignalSet.add(ENTER);
        return blankSignalSet;
    }

    public static HashSet<String>getCompareSignalSet(){
        HashSet<String> compareSignalSet = new HashSet<String>();
        compareSignalSet.add(LT);compareSignalSet.add(GT);
        compareSignalSet.add(EQUAL);compareSignalSet.add(NEQUAL);
        compareSignalSet.add(LT_EQUAl);compareSignalSet.add(GT_EQUAL);
        return compareSignalSet;
    }

    public static HashSet<String>getDeclareSignalSet(){
        HashSet<String> declareSet = new HashSet<String>();
        declareSet.add(INT);declareSet.add(REAL);
        declareSet.add(BOOL);declareSet.add(STRING);
        return declareSet;
    }

    public static HashSet<String>getCalculateSignalSet(){
        HashSet<String> calculateSet = new HashSet<String>();
        calculateSet.add(PLUS);calculateSet.add(MINUS);
        calculateSet.add(MULTIPLY);calculateSet.add(DIVIDE);
        return calculateSet;
    }

    public static HashSet<String>getValueTypeSignalSet(){
        HashSet<String> valueTypeSet = new HashSet<String>();
        valueTypeSet.add(TYPE_INT);valueTypeSet.add(TYPE_REAL);
        valueTypeSet.add(TYPE_STRING);valueTypeSet.add(TYPE_BOOL);
        return valueTypeSet;
    }
}
