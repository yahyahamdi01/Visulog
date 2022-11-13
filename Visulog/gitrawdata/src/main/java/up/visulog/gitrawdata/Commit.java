package up.visulog.gitrawdata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Commit class contains commit informations and methods to create it from "git log" command.
 */
public class Commit {
    // FIXME: (some of) these fields could have more specialized types than String
    public final String id;
    public final String date;
    public final String day;
    public final String author;
    public final String description;
    public final String mergedFrom;
    public final int changedFiles;
    public final int addedLines;
    public final int deletedLines;

    public Commit(String id, String author, String date, String description, String mergedFrom, int changedFiles, int addedLines, int deletedLines) {
        this.id = id;
        this.author = author;
        this.date = date;
        day = getDay(date);
        this.description = description;
        this.mergedFrom = mergedFrom;
        this.changedFiles = changedFiles;
        this.addedLines = addedLines;
        this.deletedLines = deletedLines;
    }

	public Commit(String author){
        this.date = "";
        this.id = "";
	day="";
	description="";
	mergedFrom="";
	changedFiles=0;
	addedLines=0;
	deletedLines=0;
		this.author=author;
	}	
    
    public static String getDay(String date) { //return the date of the commit under the format "d/m/y"
    	String[] tab=date.split(" ");
    	String day=tab[2];
    	String month=tab[1];
    	switch(month) {
    	case "Jan" : month="01";break;
    	case "Feb" : month="02";break;
    	case "Mar" : month="03";break;
    	case "Apr" : month="04";break;
    	case "May" : month="05";break;
    	case "Jun" : month="06";break;
    	case "Jul" : month="07";break;
    	case "Aug" : month="08";break;
    	case "Sep" : month="09";break;
    	case "Oct" : month="10";break;
    	case "Nov" : month="11";break;
    	case "Dec" : month="12";break;
    	}
    	String year=tab[4];
    	return day+"/"+month+"/"+year;
    	
    }

    /**
     * Execute the command "git log" in a directory and returns the output as a list of commit.
     * If the execution fail, it returns an error.
     * Else it uses the parseLog method on the output of the command. 
     * @param gitPath a path to a directory
     * @return a list of commit
     */
    public static List<Commit> parseLogFromCommand(Path gitPath, String ... command) {
        ProcessBuilder builder =
                new ProcessBuilder(command).directory(gitPath.toFile());
        Process process;
        try {
            process = builder.start();
        } catch (IOException e) {
            String res = "\"";
            for (String s : command)
                res += " " + s;
            throw new RuntimeException("Error running " + res + "\".", e);
        }
        InputStream is = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return parseLog(reader);
    }

    /**
     * Returns a list of commit from the output of "git log".
     * Commits are created with parseCommit method.
     * @param reader the output of "git log" command
     * @return a list of commit
     */
    public static List<Commit> parseLog(BufferedReader reader) {
        var result = new ArrayList<Commit>();
        Optional<Commit> commit = parseCommit(reader);
        while (commit.isPresent()) {
            result.add(commit.get());
            commit = parseCommit(reader);
        }
        return result;
    }

    /**
     * Parses a log item and outputs a commit object. Exceptions will be thrown in case the input does not have the proper format.
     * Returns an empty optional if there is nothing to parse anymore.
     * @param input a log item
     * @return an optional with a commit if it exists or an empty optional
     */
    public static Optional<Commit> parseCommit(BufferedReader input) {
        try {

            var line = input.readLine();
            if (line == null) return Optional.empty(); // if no line can be read, we are done reading the buffer
            var idChunks = line.split(" ");
            if (!idChunks[0].equals("commit")) parseError();
            var builder = new CommitBuilder(idChunks[1]);

            line = input.readLine();
            while (!line.isEmpty()) {
                var colonPos = line.indexOf(":");
                var fieldName = line.substring(0, colonPos);
                var fieldContent = line.substring(colonPos + 1).trim();
                switch (fieldName) {
                    case "Author":
                        builder.setAuthor(fieldContent);
                        break;
                    case "Merge":
                        builder.setMergedFrom(fieldContent);
                        break;
                    case "Date":
                        builder.setDate(fieldContent);
                        break;
                    default: // TODO: warn the user that some field was ignored
                }
                line = input.readLine(); //prepare next iteration
                if (line == null) parseError(); // end of stream is not supposed to happen now (commit data incomplete)
            }

            // now read the commit message per se
            var description = input
                    .lines() // get a stream of lines to work with
                    .takeWhile(currentLine -> !currentLine.isEmpty()) // take all lines until the first empty one (commits are separated by empty lines). Remark: commit messages are indented with spaces, so any blank line in the message contains at least a couple of spaces.
                    .map(String::trim) // remove indentation
                    .reduce("", (accumulator, currentLine) -> accumulator + currentLine); // concatenate everything
            builder.setDescription(description);
            input.mark(100000);
            line = input.readLine();
            // TODO: other case ?
            //for --shortstat
            if (line == null || !line.matches("^ \\d+ files? changed, \\d+ (insertions?|deletions?).*"))
                input.reset(); //back to the marked line
            else {
                for (var field : line.trim().split(", ")) {
                    var value = Integer.valueOf(field.split(" ")[0]);
                    if (field.matches(".*\\(\\+\\)$"))
                        builder.setAddedLines(value);
                    else if (field.matches(".*\\(\\-\\)$"))
                        builder.setDeletedLines(value);
                    else builder.setChangedFiles(value);
                }
                line = input.readLine();
            }
            return Optional.of(builder.createCommit());
        } catch (IOException e) {
            parseError();
        }
        return Optional.empty(); // this is supposed to be unreachable, as parseError should never return
    }

    // Helper function for generating parsing exceptions. This function *always* quits on an exception. It *never* returns.
    private static void parseError() {
        throw new RuntimeException("Wrong commit format.");
    }

    /**
     * @return a description of the commit
     */
    @Override
    public String toString() {
        return "Commit{" +
                "id='" + id + '\'' +
                (mergedFrom != null ? (", mergedFrom...='" + mergedFrom + '\'') : "") + //TODO: find out if this is the only optional field
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                ", description='" + description + '\'' +
                (changedFiles != 0 ? (", changedFiles='" + changedFiles + '\'' + ", addedLines='" + addedLines + '\'' + ", deletedLines='" + deletedLines + '\'') : "") +
                '}';
    }
}
