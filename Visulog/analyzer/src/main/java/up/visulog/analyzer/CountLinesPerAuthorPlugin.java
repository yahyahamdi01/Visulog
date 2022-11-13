package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Line;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CountLinesPerAuthorPlugin implements AnalyzerPlugin {
	private final Configuration configuration;
	private Result result;

	public CountLinesPerAuthorPlugin(Configuration generalConfiguration) {
		this.configuration = generalConfiguration;
	}

	static Result processBlame(List<Line> gitBlame) { // takes into arguments a list of lines and return the number of lines per author
		var result = new Result();
		for (var line : gitBlame) {
			if (!line.author.equals("Not Committed Yet")){
				var nb = result.linesPerAuthor.getOrDefault(line.author, 0);
				result.linesPerAuthor.put(line.author, nb.intValue() + 1);
			}
		}
		return result;
	}

	@Override
	public void run() {
		result = processBlame(Line.executeBlame(configuration.getGitPath())); // retrieve the list of lines and process it to get the number of lines per author
	}
	

	@Override
	public Result getResult() {
		if (result == null)
			run();
		Map<String, String> config = configuration.getPluginConfigs().get("countLines").getConfig();
		String percentage = config.get("percentage");
		if (percentage != null && percentage.equals("true")) {
			result.toPercentage();
		}
		return result;
	}

	static class Result implements AnalyzerPlugin.Result { // A result is a list of couples (nameAuthor, nbrLines)
		private final Map<String, Number> linesPerAuthor = new HashMap<>();

		Map<String, Number> getLinesPerAuthor() {
			return linesPerAuthor;
		}

		// the following code should be in a class Result (factor the code)
		@Override
		public String getResultAsString() { // transforme de resultat en chaine de caractère
			return linesPerAuthor.toString();
		}

		@Override
        public String getResultAsHtmlDiv() {
            generateJsFile();

            Scanner sc = null;
            try {
                sc = new Scanner(new File("CountLinesPerAuthorDesign.html"));
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
                    "if (typeof arr == \"undefined\" || !(arr instanceof Array)) {\nvar arr = [];\nlet val = \"lines\";\nlet name = \"Lines per Author:\";\n}\nval = \"lines\";\nname = \"Lines per Author:\";\n let tabNamesLines = [];\nlet tabLines = [];\n");
            for (var item : linesPerAuthor.entrySet()) {
                js.append("tabNamesLines.push(\"").append(item.getKey()).append("\");\ntabLines.push(\"")
                        .append(item.getValue()).append("\");\n");
            }
            js.append("arr[val] = tabLines;");
            js.append("arr[name] = tabNamesLines;");
			
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
			Percentage percent = new Percentage(linesPerAuthor);
			for (var item : linesPerAuthor.entrySet()) {
				linesPerAuthor.put(item.getKey(), percent.getPercentagePerAuthor(item.getKey()));
			}
		}
	}

}
