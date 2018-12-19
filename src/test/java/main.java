import interpretor.CMMLexer;
import interpretor.CMMParser;
import interpretor.CMMSemanticAnalysis;

import java.io.File;
import java.util.Scanner;

public class main {
    public static void main(String[] args){
        CMMLexer lexer = new CMMLexer();
        lexer.Lexer_Analysis(main.class.getClassLoader().getResource("test/test2.cmm").getPath());
        lexer.Print_Lexer_Result();

        CMMParser parser = new CMMParser(lexer.getTokenList());
        parser.Parser();
        parser.PrintGrammerTree();

        CMMSemanticAnalysis analysis = new CMMSemanticAnalysis(parser.getRoot());
        analysis.analysis();
        analysis.PrintSymbolTable();
        analysis.PrintError();
    }

}
