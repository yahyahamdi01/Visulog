
package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Branch;
import up.visulog.gitrawdata.Commit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CountBranchesPerAuthorPlugin implements AnalyzerPlugin {
    private final Configuration configuration;
    private Result result;

    public CountBranchesPerAuthorPlugin(Configuration generalConfiguration) {
        this.configuration = generalConfiguration;
    }

    static Result processLog(List<Branch> gitLog) {
        var result = new Result();
        for (var branch : gitLog) {
            int nb = result.branchesPerAuthor.getOrDefault(branch.author, 0);
            result.branchesPerAuthor.put(branch.author, nb + 1);
        }
        return result;
    }

    @Override
    public void run() {
        result = processLog(Branch.parseLog2FromCommand(configuration.getGitPath()));
    }

    @Override
    public Result getResult() {
        if (result == null) run();
        return result;
    }

    static class Result implements AnalyzerPlugin.Result {
        private final Map<String, Integer> branchesPerAuthor = new HashMap<>();

        Map<String, Integer> getBranchesPerAuthor() {
            return branchesPerAuthor;
        }

        @Override
        public String getResultAsString() {
            return branchesPerAuthor.toString();
        }

        @Override
        public String getResultAsHtmlDiv() {
            generateJsFile();

            Scanner sc = null;
            try {
                sc = new Scanner(new File("CountBranchesPerAuthorDesign.html"));
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
                "if (typeof arr == \"undefined\" || !(arr instanceof Array)) {\nvar arr = [];\nlet val = \"branches\";\nlet name = \"Branches per Author:\";\n}\nval = \"branches\";\nname = \"Branches per Author:\";\n let tabNamesBranches = [];\nlet tabBranches = [];\n");
                for (var item : branchesPerAuthor.entrySet()) {
                    js.append("tabNamesBranches.push(\"").append(item.getKey()).append("\");\ntabBranches.push(\"")
                            .append(item.getValue()).append("\");\n");
                }
                js.append("arr[val] = tabBranches;");
                js.append("arr[name] = tabNamesBranches;");
            

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f, true));
                bw.write(js.toString());
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        
        }

    }
}

