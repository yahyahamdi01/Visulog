package up.visulog.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import up.visulog.analyzer.CountCommitsPerAuthorPlugin.Result;
import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class that counts the number of added and deleted lines per author.
 */
public class CountModifiedLinesPerAuthorPlugin implements AnalyzerPlugin {
	private final Configuration configuration;
	private Result result;

	public CountModifiedLinesPerAuthorPlugin(Configuration generalConfiguration) {
		this.configuration = generalConfiguration;
	}
 
	/**
	 * Return the number of added and deleted lines from a list of commits.
	 * 
	 * @param gitLog a list of commits
	 * @return the number of added and deleted lines per author
	 */
	static Result processLog(List<Commit> gitLog) {
		var result = new Result();
		for (var commit : gitLog) {
			var nb = result.addedLinesPerAuthor.getOrDefault(commit.author, 0).intValue();
			nb += commit.addedLines;
			result.addedLinesPerAuthor.put(commit.author, nb);
			nb = result.deletedLinesPerAuthor.getOrDefault(commit.author, 0).intValue();
			nb += commit.deletedLines;
			result.deletedLinesPerAuthor.put(commit.author, nb);
		}
		return result;
	}

	/**
	 * Get the list of commit depending on PluginConfig and process it to have the
	 * number of added and deleted lines per author.
	 */
	@Override
	public void run() {
		Map<String, String> config = configuration.getPluginConfigs().get("countModifiedLines").getConfig();
		ArrayList<String> command = new ArrayList<String>();
		command.add("git");
		command.add("log");
		command.add("--no-merges");
		command.add("--shortstat");
		String branch = config.get("branch");
		if(branch!=null) command.add(branch);
		String before = config.get("before");
		if (before != null)
			command.add("--before=" + parseDate(before));
		String after = config.get("after");
		if (after != null)
			command.add("--after=" + parseDate(after));
		String author = config.get("author");
		if (author != null)
			command.add("--author=" + author);
		result = processLog(
				Commit.parseLogFromCommand(configuration.getGitPath(), command.toArray(new String[command.size()])));
	}
	
    private String parseDate(String date) {
    	String[] format = date.split("_");
    	if (format.length == 1)
    		return format[0] + " 00:00:00";
    	else
    		return format[0] + " " + format[1].replace("/", ":");
    }

	/**
	 * @return the result after running it
	 */
	@Override
	public Result getResult() {
		if (result == null)
			run();
		Map<String, String> config = configuration.getPluginConfigs().get("countModifiedLines").getConfig();
		String percentage = config.get("percentage");
		if (percentage != null && percentage.equals("true"))
			result.toPercentage();
		return result;
	}

	/**
	 * Class that contains the result.
	 */
	static class Result implements AnalyzerPlugin.Result {
		private final Map<String, Number> addedLinesPerAuthor = new HashMap<>();
		private final Map<String, Number> deletedLinesPerAuthor = new HashMap<>();

		Map<String, Number> getAddedLinesPerAuthor() {
			return addedLinesPerAuthor;
		}

		Map<String, Number> getDeletedLinesPerAuthor() {
			return deletedLinesPerAuthor;
		}

		/**
		 * @return a text representing the result
		 */
		@Override
		public String getResultAsString() {
			return addedLinesPerAuthor.toString() + deletedLinesPerAuthor.toString();
		}

		// /**
		//  * @return an html text representing the result
		//  */
		// @Override
		// public String getResultAsHtmlDiv() {
		// 	StringBuilder html = new StringBuilder(
		// 			"<div>Modified lines per author: <table><thead><tr><th>Author</th><th>Added Lines</th><th>Deleted Lines</th></tr></thead><tbody>");
		// 	for (var item : addedLinesPerAuthor.entrySet()) {
		// 		html.append("<tr><td>").append(item.getKey()).append("</td><td>").append(item.getValue())
		// 				.append("</td><td>").append(deletedLinesPerAuthor.get(item.getKey())).append("</td></tr>");
		// 	}
		// 	html.append("</tbody></table></div>");
		// 	return html.toString();
		// }

		@Override
		public String getResultAsHtmlDiv() {
			generateModifJsFile();

			Scanner sc = null;
			try {
				sc = new Scanner(new File("CountModifiedLinesPerAuthorDesign.html"));
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

		private void generateModifJsFile() {
			try {
				File f = new File("./data.js");
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File f = new File("./data.js");

			StringBuilder js = new StringBuilder(
					"if (typeof arr == \"undefined\" || !(arr instanceof Array)) {\nvar arr = [];\nlet val = \"modified lines\";\nlet name = \"Modified lines per Author:\";\n}\nval = \"modified lines\";\nname = \"Modified lines per Author:\";\n let tabNamesModifiedLines = [];\nlet tabNoAddedLines = [];\nlet tabNoDeletedLines = [];\nlet tabModifiedLines = [];\n");
			for (var item : addedLinesPerAuthor.entrySet()) {
				js.append("tabNamesModifiedLines.push(\"").append(item.getKey()).append("\");\ntabNoAddedLines.push(\"")
						.append(item.getValue()).append("\");\ntabNoDeletedLines.push(\"")
						.append(deletedLinesPerAuthor.get(item.getKey())).append("\");\ntabModifiedLines.push(")
                        .append(item.getValue()).append("+").append(deletedLinesPerAuthor.get(item.getKey())).append(");\n");
			}
			js.append("arr[val] = tabModifiedLines;");
			js.append("arr[name] = tabNamesModifiedLines;");
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
			Percentage percent = new Percentage(addedLinesPerAuthor);
			for (var item : addedLinesPerAuthor.entrySet()) {
				addedLinesPerAuthor.put(item.getKey(), percent.getPercentagePerAuthor(item.getKey()));
			}
			percent = new Percentage(deletedLinesPerAuthor);
			for (var item : deletedLinesPerAuthor.entrySet()) {
				deletedLinesPerAuthor.put(item.getKey(), percent.getPercentagePerAuthor(item.getKey()));
			}
		}
	}
}
