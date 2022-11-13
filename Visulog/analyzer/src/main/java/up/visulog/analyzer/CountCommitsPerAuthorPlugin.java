// permet d obtenir un texte html indiquant le nombre de commit par auteur

package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CountCommitsPerAuthorPlugin implements AnalyzerPlugin { // prend en argument une configuration et renvoie un resultat
	private final Configuration configuration;
	private Result result;

	public CountCommitsPerAuthorPlugin(Configuration generalConfiguration) {
		this.configuration = generalConfiguration;
	}

    Result processLog(List<Commit> gitLog) {  //prend en argument une liste de commits et renvoie le nombre de commits par auteur (si la variable  perDate vaut true le resultat a une forme differente : c'est une liste de jours pour lesquels on associe le nombre de commits par auteur fait ce jour la)
    	String date = configuration == null ? null : configuration.getPluginConfigs().get("countCommits").getConfig().get("perDate");
    	if (date != null && date.equals("true")) {
        	var result = new ResultPerDate();
	        for (var commit : gitLog) {
	        	result.addCommit(commit.day, commit.author);
	        }
	        return result;
        }
        else {
        	var result = new ResultPerAuthor();
	        for (var commit : gitLog) {
	            var nb = result.commitsPerAuthor.getOrDefault(commit.author, 0);
	            result.commitsPerAuthor.put(commit.author, nb.intValue() + 1);
	        }
	        return result;
        }
    	
    }

    @Override
    /**
     * Get the list of commit depending on PluginConfig and parse the list to get the number of commit per author
     */
    public void run() {
    	Map<String, String> config = configuration.getPluginConfigs().get("countCommits").getConfig();
    	ArrayList<String> command = new ArrayList<String>();
    	command.add("git");
        command.add("log");
        String branch = config.get("branch");
        if(branch!=null) command.add(branch);
    	String merge = config.get("merge");
    	if (merge != null && merge.equals("true"))
    		command.add("--merges");
    	if (merge != null && merge.equals("false"))
    		command.add("--no-merges");
    	String before = config.get("before");
    	if (before != null)
    		command.add("--before=" + parseDate(before));
    	String after = config.get("after");
    	if (after != null)
    		command.add("--after=" + parseDate(after));
    	String author = config.get("author");
    	if (author != null)
    		command.add("--author=" + author);
    	result = processLog(Commit.parseLogFromCommand(configuration.getGitPath(), command.toArray(new String[command.size()])));
    }
    
    private String parseDate(String date) {
    	String[] format = date.split("_");
    	if (format.length == 1)
    		return format[0] + " 00:00:00";
    	else
    		return format[0] + " " + format[1].replace("/", ":");
    }

	@Override
	public Result getResult() {
		if (result == null)
			run();
		Map<String, String> config = configuration.getPluginConfigs().get("countCommits").getConfig();
		String percentage = config.get("percentage");
		if (percentage != null && percentage.equals("true"))
			if (result instanceof ResultPerAuthor)
			((ResultPerAuthor)result).toPercentage();
		return result;
	}
    
    static class Result implements AnalyzerPlugin.Result{
    	@Override
        public String getResultAsString() { //transforme de resultat en chaine de caractère
            return "";
        }
        
        @Override
        public String getResultAsHtmlDiv() { // presente le resultat sous forme d un texte html
            return "";
        }
    	
    }

    static class ResultPerAuthor extends Result { // un resulat est une liste de couples (nomAuteur, nbrCommit) qui associe a chaque auteur le nombre de commit qu'il a crée
        private final Map<String, Number> commitsPerAuthor = new HashMap<>();
        
		Map<String, Number> getCommitsPerAuthor() {
			return commitsPerAuthor;
		}

		@Override
		public String getResultAsString() { // transforme le resultat en chaine de caractère
			return commitsPerAuthor.toString();
		}

		@Override
        public String getResultAsHtmlDiv() {
            generateJsFile();

            Scanner sc = null;
            try {
                sc = new Scanner(new File("CountCommitsPerAuthorDesign.html"));
            } catch (Exception e) {
                System.out.println("Erreur lors d'ouverture fichier:");
                e.printStackTrace();
                System.exit(1);
            }
            StringBuilder html = new StringBuilder();
            sc.useDelimiter("\n");
            while (sc.hasNext()) {
                html.append(sc.next() + "\n");

            }
            return html.toString();
        }

        private void generateJsFile(){
            try {
                File f = new File("./data.js");
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File f = new File("./data.js");
            
            StringBuilder js = new StringBuilder(
                    "if (typeof arr == \"undefined\" || !(arr instanceof Array)) {\nvar arr = [];\nlet val = \"commits\";\nlet name = \"Commits per Author:\";\n}\nval = \"commits\";\nname = \"Commits per Author:\";\n let tabNamesCommits = [];\nlet tabCommits = [];\n");
            for (var item : commitsPerAuthor.entrySet()) {
                js.append("tabNamesCommits.push(\"").append(item.getKey()).append("\");\ntabCommits.push(\"")
                        .append(item.getValue()).append("\");\n");
            }
            js.append("arr[val] = tabCommits;");
            js.append("arr[name] = tabNamesCommits;");
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                bw.write(js.toString());
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

		/**
		 * Changes values by their percentage
		 */
		public void toPercentage() {
			Percentage percent = new Percentage(commitsPerAuthor);
			for (var item : commitsPerAuthor.entrySet()) {
				commitsPerAuthor.put(item.getKey(), percent.getPercentagePerAuthor(item.getKey()));
			}
		}
		
        
        public String getResultAsLine() { // presente le resultat sous forme d un texte html
            StringBuilder line = new StringBuilder("");
            for (var item : commitsPerAuthor.entrySet()) {
               line.append(item.getKey()).append("(").append(item.getValue()).append(")").append(" ");
            }
            return line.toString();
        }
    }
    
    static class ResultPerDate extends Result { // a ResultPerDate is a list of couples (day, nbrCommitsPerAuthor)
        private ArrayList<CommitsPerDay> commitsPerDate = new ArrayList<CommitsPerDay>(); //list of days where each day represent the number of commits per author made during this day
        
        static class CommitsPerDay{
        	String day;
        	ResultPerAuthor nbrCommitsPerAuthor;
        	
        	public CommitsPerDay(String d) {
        		day=d;
        		nbrCommitsPerAuthor=new ResultPerAuthor();
        	}
        	
        	public boolean addCommit(String day, String author) {
        		if(this.day.equals(day)) {
        			var nb = nbrCommitsPerAuthor.commitsPerAuthor.getOrDefault(author, 0);
     	            nbrCommitsPerAuthor.commitsPerAuthor.put(author, nb.intValue() + 1);
     	            return true;
        		}
        		return false;
        	}
        }
        
        public void addCommit(String day, String author) {
        	for(CommitsPerDay commitsPerDay:commitsPerDate) {
        		if(commitsPerDay.addCommit(day, author)) {
        			return;
        		}
        	}
		CommitsPerDay commitsPerDay=new CommitsPerDay(day);
		commitsPerDay.addCommit(day, author);
        	commitsPerDate.add(commitsPerDay);
        	
        }

        ArrayList<CommitsPerDay> getCommitsPerDate() {
            return commitsPerDate;
        }
        
        @Override
        public String getResultAsString() { //transforme de resultat en chaine de caractère
            return commitsPerDate.toString();
        }

        @Override
        public String getResultAsHtmlDiv() { // presente le resultat sous forme d un texte html
            StringBuilder html = new StringBuilder("<div>Commits per date: <ul>");
            for (var item : commitsPerDate) {
                html.append("<li>").append(item.day).append(": ").append(item.nbrCommitsPerAuthor.getResultAsLine()).append("</li>");
            }
            html.append("</ul></div>");
            return html.toString();
        }
    }
    
}