package up.visulog.gitrawdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Line {
	public final String id;
	public final String author;
    public final String date;
    public final String lineNumber;
    public final String content;
    
    public Line(String id, String author, String date, String lineNumber, String content) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.lineNumber = lineNumber;
        this.content = content;
    }
    
    @Override
    public String toString() {
        return "Line{" +
                "id='" + id + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", lineNumber='" + lineNumber + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
    
    public static LinkedList<File> listFilesForFolder(final File folder) { //returns all the files of a folder
    	LinkedList<File> list = new LinkedList<File>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                list.addAll(listFilesForFolder(fileEntry));
            } else {
            	list.addLast(fileEntry);
            }
        }
        return list;
    }
    
    

    public static List<Line> executeBlame(Path gitPath) { //takes in argument all the files of the project and executes the method parseBlameFromCommand() on it -> returns all the lines of the project
    	LinkedList<File> list = listFilesForFolder(gitPath.toFile());
    	List<Line> lineList= new ArrayList<Line>();
    	for(File file : list) {
    		lineList.addAll(parseLogBlameFromCommand(file));
    	}
    	return lineList;
    }

    
    /**
     * Execute the command "git blame" in a file and returns the output as a list of lines.
     * If the execution fail, it returns an error.
     * Else it uses the parseLog method on the output of the command. 
     * @param file a file of the project
     * @return a list of lines
     */
    
    public static List<Line> parseLogBlameFromCommand(File file) { //apply the method git blame to the file and returns the lines of the file
        String fileName = file.getName();
    	ProcessBuilder builder =
                new ProcessBuilder("git", "blame", fileName).directory(file.getParentFile()); 
        /*builder allow to execute the command git blame on the file in the directory where it is located 
         * ProcessBuilder("commandName","arg1","arg2)
         * directory() sets this process builder's working directory
         * */
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new RuntimeException("Error running \"git blame\".", e);
        }
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is)); // a BufferedReader eases the reading of a text
        return parseLogBlame(reader);
    }
    
    /**
     * Returns a list of lines from the output of "git blame".
     * Lines are created with parseLine method.
     * @param reader the output of "git blame" command
     * @return a list of lines
     */
    public static List<Line> parseLogBlame(BufferedReader reader) {
        var result = new ArrayList<Line>();
        Optional<Line> line = parseLine(reader);
        while (line.isPresent()) {
            result.add(line.get());
            line = parseLine(reader);
        }
        return result;
    }
    
    
    /**
     * Parses a blame item and outputs a line object. Exceptions will be thrown in case the input does not have the proper format.
     * Returns an empty optional if there is nothing to parse anymore.
     * @param input a blame item
     * @return an optional with a line if it exists or an empty optional
     */
    public static Optional<Line> parseLine(BufferedReader input) { 
        try {

            var fileLine = input.readLine();
            if (fileLine == null) return Optional.empty(); // if no line can be read, we are done reading the buffer
            
            String pattern = "^(.{8}).*[(](.*)\\p{Space}*(\\d{4}-\\d{2}-\\d{2}\\p{Space}*\\d{2}:\\d{2}:\\d{2}\\p{Space}*\\p{Punct}\\d{4})\\p{Space}*(\\d+)[)](.*)$";

            Pattern r = Pattern.compile(pattern);

    	    Matcher m = r.matcher(fileLine);
    	    
    	    if(m.find()){
    	    	String id = m.group(1);
    	    	var builder = new LineBuilder(id);
    	    	
	            var author = m.group(2).trim();
	            builder.setAuthor(author);
	            
	            var date=m.group(3);
	            builder.setDate(date);
	            
	            var lineNumber = m.group(4);
	            builder.setLineNumber(lineNumber);

	            var content=m.group(5);
	            builder.setContent(content);
	            
	            return Optional.of(builder.createLine());
    	    }
 
        } catch (IOException e) {
            parseError();
        }
        return Optional.empty(); // this is supposed to be unreachable, as parseError should never return
    }

    // Helper function for generating parsing exceptions. This function *always* quits on an exception. It *never* returns.
    private static void parseError() {
        throw new RuntimeException("Wrong line format.");
    }


}


