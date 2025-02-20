import java.io.*;
import java.util.*;
import java.nio.file.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class Compiler {


    public static class DFA {
        
        
        private static final int FINAL_STATE_GLOBAL_VAR = 2;
        private static final int FINAL_STATE_LOCAL_VAR = 1;
        private static final int FINAL_STATE_MULTILINE_COMMENT = 4;
        private static final int FINAL_STATE_SINGLELINE_COMMENT = 2;
        private static final int FINAL_STATE_FLOAT = 3;
        private static final int FINAL_STATE_INTEGER = 1;

        public static boolean matchGlobalVariable(String input , boolean toPrint) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0: state = (c == '_') ? 1 : -1; break;
                    case 1: state = Character.isLowerCase(c) ? FINAL_STATE_GLOBAL_VAR : -1; break;
                    case FINAL_STATE_GLOBAL_VAR: state = Character.isLowerCase(c) ? FINAL_STATE_GLOBAL_VAR : -1; break;
                    default: return false;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return false;
                }
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return state == FINAL_STATE_GLOBAL_VAR;
        }
        
        public static boolean matchLocalVariable(String input , boolean toPrint) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0: state = Character.isLowerCase(c) ? FINAL_STATE_LOCAL_VAR : -1; break;
                    case FINAL_STATE_LOCAL_VAR: state = Character.isLowerCase(c) ? FINAL_STATE_LOCAL_VAR : -1; break;
                    default: return false;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return false;
                }
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return state == FINAL_STATE_LOCAL_VAR;
        }

        public static boolean matchDatatype(String input , boolean toPrint) {
            return matchDFA(input, new String[]{"bitz", "numz", "floatz", "charz"} , toPrint);
        }

        public static boolean matchBoolean(String input , boolean toPrint) {
            return matchDFA(input, new String[]{"true", "false"} , toPrint);
        }

        public static boolean matchChar(String input, boolean toPrint) {
            return matchDFA(input, new String[]{"'a'", "'b'", "'c'", "'d'", "'e'", "'f'", "'g'", "'h'", "'i'", "'j'", "'k'", "'l'", "'m'", "'n'", "'o'", "'p'", "'q'", "'r'", "'s'", "'t'", "'u'", "'v'", "'w'", "'x'", "'y'", "'z'"}, toPrint);
        }

        public static boolean matchFloat(String input , boolean toPrint) {
            return processNumber(input, true , toPrint);
        }

        public static boolean matchInteger(String input , boolean toPrint) {
            return processNumber(input, false , toPrint);
        }

        public static boolean matchOperator(String input ,  boolean toPrint) {
            return matchDFA(input, new String[]{"+", "-", "*", "/", "%", "^" , "="} , toPrint);
        }

        public static boolean matchTerminator(String input , boolean toPrint) {
            return matchDFA(input, new String[]{";"} , toPrint);
        }

       public static boolean matchPrintStatement(String input , boolean toPrint) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            String keyword = "print<<";
            for (char c : input.toCharArray()) {
                if (state < keyword.length() && c == keyword.charAt(state)) {
                    state++;
                } else if (Character.isLetter(c) && state >= keyword.length()) {
                    state++;
                } else if (c == '>' && state > keyword.length()) {
                    state++;
                } else {
                    state = -1;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return false;
                }
            }
            // The final state should be length of keyword + length of letters + 1 (for the semicolon)
            boolean isValid = (input.endsWith(">") && state == keyword.length() + (input.length() - keyword.length()));
            if (!isValid) {
                path.append(" (Reject)");
                if(toPrint)
                System.out.println(path);
                return isValid;
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return isValid;
        }
       
       public static boolean matchMultilineComment(String input) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");

            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0:
                        state = (c == '$') ? 1 : -1;
                        break;
                    case 1:
                        state = (c == '$') ? 2 : -1;
                        break;
                    case 2:
                        if (c == '$') {
                            state = 3; // Found a `$` inside, check next character
                        } else {
                            state = 2; // Stay in the comment content state
                        }
                        break;
                    case 3:
                        if (c == '$') {
                            state = FINAL_STATE_MULTILINE_COMMENT; // Found `$$`, accept
                        } else {
                            state = 2; // Any other character means it's normal content, back to q2
                        }
                        break;
                    default:
                        return false;
                }

                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    System.out.println(path);
                    return false;
                }
            }
            path.append(" (Accept)");
            System.out.println(path);
            return state == FINAL_STATE_MULTILINE_COMMENT;
        }


       public static boolean matchSingleLineComment(String input) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0: state = (c == '/') ? 1 : -1; break;
                    case 1: state = (c == '/') ? FINAL_STATE_SINGLELINE_COMMENT : -1; break;
                    case FINAL_STATE_SINGLELINE_COMMENT: state = (c != '\n') ? FINAL_STATE_SINGLELINE_COMMENT : -1; break;
                    default: return false;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    System.out.println(path);
                    return false;
                }
            }
            path.append(" (Accept)");
            System.out.println(path);
            return state == FINAL_STATE_SINGLELINE_COMMENT;
        }

        private static boolean matchDFA(String input, String[] validWords , boolean toPrint) {
            StringBuilder path = new StringBuilder("q0");
            int state = 0;
            for (String word : validWords) {
                if (input.equals(word)) {
                    for (char c : input.toCharArray()) {
                        state++;
                        path.append(" --").append(c).append("--> q").append(state);
                    }
                    path.append(" (Accept)");
                    if(toPrint)
                    System.out.println(path);
                    return true;
                }
            }
            path.append(" (Reject)");
            if(toPrint)
            System.out.println(path);
            return false;
        }

        private static boolean processNumber(String input, boolean allowDecimal , boolean toPrint) {
            int state = 0;
            int decimalCount = 0;
            StringBuilder path = new StringBuilder("q0");

            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0:
                        state = Character.isDigit(c) ? 1 : -1;
                        break;
                    case 1:
                        if (Character.isDigit(c)) {
                            state = 1;
                        } else if (allowDecimal && c == '.') {
                            state = 2;
                        } else {
                            state = -1;
                        }
                        break;
                    case 2:
                        if (Character.isDigit(c)) {
                            state = FINAL_STATE_FLOAT;
                            decimalCount = 1; // Start counting decimal digits
                        } else {
                            state = -1;
                        }
                        break;
                    case FINAL_STATE_FLOAT:
                        if (Character.isDigit(c)) {
                            decimalCount++;
                            if (decimalCount > 5) {
                                state = -1; // Reject if more than 5 decimal digits
                            }
                        } else {
                            state = -1;
                        }
                        break;
                    default:
                        return false;
                }

                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return false;
                }
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return state == FINAL_STATE_INTEGER || state == FINAL_STATE_FLOAT;
        }
    
    }
    
    public static class DFAPathReturner {
        
        
        private static final int FINAL_STATE_GLOBAL_VAR = 2;
        private static final int FINAL_STATE_LOCAL_VAR = 1;
        private static final int FINAL_STATE_MULTILINE_COMMENT = 4;
        private static final int FINAL_STATE_SINGLELINE_COMMENT = 2;
        private static final int FINAL_STATE_FLOAT = 3;
        private static final int FINAL_STATE_INTEGER = 1;

        static StringBuilder matchGlobalVariable(String input , boolean toPrint) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0: state = (c == '_') ? 1 : -1; break;
                    case 1: state = Character.isLowerCase(c) ? FINAL_STATE_GLOBAL_VAR : -1; break;
                    case FINAL_STATE_GLOBAL_VAR: state = Character.isLowerCase(c) ? FINAL_STATE_GLOBAL_VAR : -1; break;
                    default: return path;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return path;
                }
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return path;
        }
        
        public static StringBuilder matchLocalVariable(String input , boolean toPrint) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0: state = Character.isLowerCase(c) ? FINAL_STATE_LOCAL_VAR : -1; break;
                    case FINAL_STATE_LOCAL_VAR: state = Character.isLowerCase(c) ? FINAL_STATE_LOCAL_VAR : -1; break;
                    default: return path;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return path;
                }
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return path;
        }

        public static StringBuilder matchDatatype(String input , boolean toPrint) {
            return matchDFA(input, new String[]{"bitz", "numz", "floatz", "charz"} , toPrint);
        }

        public static StringBuilder matchBoolean(String input , boolean toPrint) {
            return matchDFA(input, new String[]{"true", "false"} , toPrint);
        }

        public static StringBuilder matchChar(String input, boolean toPrint) {
            return matchDFA(input, new String[]{"'a'", "'b'", "'c'", "'d'", "'e'", "'f'", "'g'", "'h'", "'i'", "'j'", "'k'", "'l'", "'m'", "'n'", "'o'", "'p'", "'q'", "'r'", "'s'", "'t'", "'u'", "'v'", "'w'", "'x'", "'y'", "'z'"}, toPrint);
        }

        public static StringBuilder matchFloat(String input , boolean toPrint) {
            return processNumber(input, true , toPrint);
        }

        public static StringBuilder matchInteger(String input , boolean toPrint) {
            return processNumber(input, false , toPrint);
        }

        public static StringBuilder matchOperator(String input ,  boolean toPrint) {
            return matchDFA(input, new String[]{"+", "-", "*", "/", "%", "^" , "="} , toPrint);
        }

        public static StringBuilder matchTerminator(String input , boolean toPrint) {
            return matchDFA(input, new String[]{";"} , toPrint);
        }

       public static StringBuilder matchPrintStatement(String input , boolean toPrint) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            String keyword = "print<<";
            for (char c : input.toCharArray()) {
                if (state < keyword.length() && c == keyword.charAt(state)) {
                    state++;
                } else if (Character.isLetter(c) && state >= keyword.length()) {
                    state++;
                } else if (c == '>' && state > keyword.length()) {
                    state++;
                } else {
                    state = -1;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return path;
                }
            }
            // The final state should be length of keyword + length of letters + 1 (for the semicolon)
            boolean isValid = (input.endsWith(">") && state == keyword.length() + (input.length() - keyword.length()));
            if (!isValid) {
                path.append(" (Reject)");
                if(toPrint)
                System.out.println(path);
                return path;
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return path;
        }
       
       public static StringBuilder matchMultilineComment(String input) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");

            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0:
                        state = (c == '$') ? 1 : -1;
                        break;
                    case 1:
                        state = (c == '$') ? 2 : -1;
                        break;
                    case 2:
                        if (c == '$') {
                            state = 3; // Found a `$` inside, check next character
                        } else {
                            state = 2; // Stay in the comment content state
                        }
                        break;
                    case 3:
                        if (c == '$') {
                            state = FINAL_STATE_MULTILINE_COMMENT; // Found `$$`, accept
                        } else {
                            state = 2; // Any other character means it's normal content, back to q2
                        }
                        break;
                    default:
                        return path;
                }

                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    System.out.println(path);
                    return path;
                }
            }
            path.append(" (Accept)");
            System.out.println(path);
            return path;
        }


       public static StringBuilder matchSingleLineComment(String input) {
            int state = 0;
            StringBuilder path = new StringBuilder("q0");
            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0: state = (c == '/') ? 1 : -1; break;
                    case 1: state = (c == '/') ? FINAL_STATE_SINGLELINE_COMMENT : -1; break;
                    case FINAL_STATE_SINGLELINE_COMMENT: state = (c != '\n') ? FINAL_STATE_SINGLELINE_COMMENT : -1; break;
                    default: return path;
                }
                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    System.out.println(path);
                    return path;
                }
            }
            path.append(" (Accept)");
            System.out.println(path);
            return path;
        }

        private static StringBuilder matchDFA(String input, String[] validWords , boolean toPrint) {
            StringBuilder path = new StringBuilder("q0");
            int state = 0;
            for (String word : validWords) {
                if (input.equals(word)) {
                    for (char c : input.toCharArray()) {
                        state++;
                        path.append(" --").append(c).append("--> q").append(state);
                    }
                    path.append(" (Accept)");
                    if(toPrint)
                    System.out.println(path);
                    return path;
                }
            }
            path.append(" (Reject)");
            if(toPrint)
            System.out.println(path);
            return path;
        }

        private static StringBuilder processNumber(String input, boolean allowDecimal , boolean toPrint) {
            int state = 0;
            int decimalCount = 0;
            StringBuilder path = new StringBuilder("q0");

            for (char c : input.toCharArray()) {
                switch (state) {
                    case 0:
                        state = Character.isDigit(c) ? 1 : -1;
                        break;
                    case 1:
                        if (Character.isDigit(c)) {
                            state = 1;
                        } else if (allowDecimal && c == '.') {
                            state = 2;
                        } else {
                            state = -1;
                        }
                        break;
                    case 2:
                        if (Character.isDigit(c)) {
                            state = FINAL_STATE_FLOAT;
                            decimalCount = 1; // Start counting decimal digits
                        } else {
                            state = -1;
                        }
                        break;
                    case FINAL_STATE_FLOAT:
                        if (Character.isDigit(c)) {
                            decimalCount++;
                            if (decimalCount > 5) {
                                state = -1; // Reject if more than 5 decimal digits
                            }
                        } else {
                            state = -1;
                        }
                        break;
                    default:
                        return path;
                }

                path.append(" --").append(c).append("--> q").append(state);
                if (state == -1) {
                    path.append(" (Reject)");
                    if(toPrint)
                    System.out.println(path);
                    return path;
                }
            }
            path.append(" (Accept)");
            if(toPrint)
            System.out.println(path);
            return path;
        }
    
    }
    
    
    public static String removeExtraSpaces(String input) {
        return input.replaceAll("\\s+", " ").trim();
    }
    
    public static String ensureSpaceBeforeSemicolon(String input) {
        return input.replace(";", " ;");
    }
    
    public static String endline(String input) {
        return input.replace(" ", "\n");
    }
    
    public static void printSymbolTable(List<Map.Entry<String, String>> symbolTable) {
        System.out.println("\nSymbol Table:");
        for (Map.Entry<String, String> entry : symbolTable) {
            System.out.println("Name: \t\t" + entry.getKey() + "\t\t,\t\t Type: \t\t" + entry.getValue());
        }
    }
    
    public static void preProcessing() {
    	 String inputFile = "data.txt.txt";
         String outputFile = "edit.txt";

         try {
             // Read the file
             BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             StringBuilder code = new StringBuilder();
             String line;
             boolean inMultilineComment = false;

             while ((line = reader.readLine()) != null) {
                 line = line.trim();
                 
               
                 if (line.startsWith("$$")) {
                     inMultilineComment = !inMultilineComment;
                     continue;
                 }
                 if (inMultilineComment) continue;
                 
                 // Remove single-line comments
                 int commentIndex = line.indexOf("//");
                 if (commentIndex != -1) {
                     line = line.substring(0, commentIndex).trim();
                 }
                 
                 if (!line.isEmpty()) {
                 	 line = removeExtraSpaces(line);
                      line = ensureSpaceBeforeSemicolon(line);
                      line = endline(line);
                      code.append(line).append("\n");
                 }
             }
             reader.close();

             
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
             writer.write(code.toString());
             writer.close();

             System.out.println("Comments removed successfully.");
         } catch (IOException e) {
             System.err.println("Error processing the file: " + e.getMessage());
         }
         
    }
    
    public static void addToDfaFile(StringBuilder var) {
    	var.append("\n\n");
        try {
            Files.write(Paths.get("passedDFA.txt"), var.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void processTokens() {
        String inputFile = "edit.txt";
        int printStatmentCount = 0;
        int datatypeCount = 0, localVarCount = 0, globalVarCount = 0, operatorCount = 0, terminatorCount = 0;
        List<String> errors = new ArrayList<>();
        List<Map.Entry<String, String>> symbolTable = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            while ((line = reader.readLine()) != null) {
                boolean matched = false;
                
                if (DFA.matchDatatype(line , false)) {
                    datatypeCount++;
                    symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Datatype"));
                    matched = true;
                    StringBuilder var = DFAPathReturner.matchDatatype(line, false);
                    addToDfaFile(var);
                    
                } else if (DFA.matchBoolean(line , false)  ) {
                	symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Bool Literals"));
                	matched = true;
                	StringBuilder var = DFAPathReturner.matchBoolean(line, false);
                	addToDfaFile(var);
                	
                }else if(DFA.matchChar(line, false)) {
                	symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Char Literals"));
                	matched = true;
                	
                	StringBuilder var = DFAPathReturner.matchChar(line, false);
                	addToDfaFile(var);
                	
                }else if(DFA.matchFloat(line, false)) {
                	symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Float Literals"));
                	matched = true;
                	
                	StringBuilder var = DFAPathReturner.matchFloat(line, false);
                	addToDfaFile(var);
                	
                } else if(DFA.matchInteger(line, false)) {
                	symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Integer Literals"));
                	matched = true;
                	
                	StringBuilder var = DFAPathReturner.matchInteger(line, false);
                	addToDfaFile(var);
                	
                } else if (DFA.matchGlobalVariable(line, false)) {
                    globalVarCount++;
                    symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Global Variable"));
                    matched = true;
                    
                    StringBuilder var = DFAPathReturner.matchGlobalVariable(line, false);
                    addToDfaFile(var);
                    
                } else if (DFA.matchLocalVariable(line, false)) {
                    localVarCount++;
                    symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Local Variable"));
                    matched = true;
                    
                    StringBuilder var = DFAPathReturner.matchLocalVariable(line, false);
                    addToDfaFile(var);
                    
                } else if (DFA.matchOperator(line, false)) {
                    operatorCount++;
                    symbolTable.add(new AbstractMap.SimpleEntry<>(line, "Operator"));
                    matched = true;
                    
                    StringBuilder var = DFAPathReturner.matchOperator(line, false);
                    addToDfaFile(var);
                    
                } else if (DFA.matchTerminator(line, false)) {
                    terminatorCount++;
                    matched = true;
                    
                    StringBuilder var = DFAPathReturner.matchTerminator(line, false);
                    addToDfaFile(var);
                    
                } else if (DFA.matchPrintStatement(line , false)) {
                    printStatmentCount++;
                    matched = true;
                    
                    StringBuilder var = DFAPathReturner.matchPrintStatement(line, false);
                    addToDfaFile(var);
                    
                }
                
                if (!matched && !errors.contains(line)) {
                    errors.add(line);
                }

            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return;
        }
        
        System.out.println("Datatype Count: " + datatypeCount);
        System.out.println("Global Variable Count: " + globalVarCount);
        System.out.println("Local Variable Count: " + localVarCount);
        System.out.println("Operator Count: " + operatorCount);
        System.out.println("Terminator Count: " + terminatorCount);
        System.out.println("Print Statement Count: " + printStatmentCount);
        
        if (!errors.isEmpty()) {
        	if (!errors.isEmpty()) {
                findErrorLines(errors , "data.txt.txt");
            }
        }
        
        printSymbolTable(symbolTable);
    }
    
    
    /*public static void findErrorLines(List<String> errors) {
        String inputFile = "data.txt.txt";
        Map<String, Integer> errorLines = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                for (String error : errors) {
                    if (line.contains(error) && !errorLines.containsKey(error)) {
                        errorLines.put(error, lineNumber);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
            return;
        }

        // Append line numbers to errors
        System.out.println("Errors with line numbers:");
        for (String error : errors) {
            int lineNum = errorLines.getOrDefault(error, -1);
            System.out.println(error + " (Line: " + lineNum + ")");
        }
    }*/
    
    public static void findErrorLines(List<String> errors, String dataFile) {
    Map<String, List<Integer>> errorLines = new HashMap<>();
    
    try (BufferedReader reader = new BufferedReader(new FileReader(dataFile))) {
        String line;
        int lineNumber = 1;
        
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            for (String error : errors) {
                // Check if the full line matches OR if the error appears as a standalone word in the line
                if (line.equals(error) || line.matches(".*\\b" + error + "\\b.*")) {
                    errorLines.computeIfAbsent(error, k -> new ArrayList<>()).add(lineNumber);
                }
            }
            
            lineNumber++;
        }
        
    } catch (IOException e) {
        System.err.println("Error reading the file: " + e.getMessage());
        return;
    }
    
    // Print errors with line numbers
    System.out.println("Errors with Line Numbers:");
    for (Map.Entry<String, List<Integer>> entry : errorLines.entrySet()) {
        System.out.println(entry.getKey() + " (line: " + entry.getValue() + ")");
    }
}
 
    
    public static void main(String[] args) {

    	//System.out.println("print<<hello>: " + DFA.matchPrintStatement("print<<hello>" , true)); // true
      //  System.out.println("$$ $$: " + DFA.matchMultilineComment("$$ This is$ a comment $$")); // true
       // System.out.println("// This is a comment: " + DFA.matchSingleLineComment("// This is a comment")); // true
        
       // System.out.println("_global: " + DFA.matchGlobalVariable("_global" , true)); // true
       // System.out.println("global: " + DFA.matchGlobalVariable("global"  , true)); // false
        
       // System.out.println("value: " + DFA.matchLocalVariable("value" , true)); // true
       // System.out.println("_value: " + DFA.matchLocalVariable("_value" , true)); // false
        
      //  System.out.println("numz: " + DFA.matchDatatype("numz", true)); // true
     //   System.out.println("true: " + DFA.matchBoolean("true" , true)); // true
       // System.out.println("'a': " + DFA.matchChar("'a'" , true)); // true
       // System.out.println("3.14: " + DFA.matchFloat("3.14" , true)); // true
      //  System.out.println("10: " + DFA.matchInteger("10" , true)); // true
      //  System.out.println("+ : " + DFA.matchOperator("+" , true)); // true
       // System.out.println("; : " + DFA.matchTerminator(";", true)); // true
        
        
        preProcessing();
        processTokens();
        
        
        

    }

    

}