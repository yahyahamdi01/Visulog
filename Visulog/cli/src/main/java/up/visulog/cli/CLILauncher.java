package up.visulog.cli;

import up.visulog.analyzer.Analyzer;
import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;

import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CLILauncher {

    public static void main(String[] args) {
        var config = makeConfigFromCommandLineArgs(args);
        if (config.isPresent()) {
            var analyzer = new Analyzer(config.get());
            var results = analyzer.computeResults();
            try {
                File js = new File("./data.js");
                js.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File js = new File("./data.js");
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(js));
                bw.write("");
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                File f = new File("./test.html");
                if (f.createNewFile()) {
                    System.out.println("File created");
                } else {
                    System.out.println("File overwritten");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            File f = new File("./test.html");
            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(results.toHTML());
                bw.close();
                Desktop.getDesktop().browse(f.toURI());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else
            displayHelpAndExit();
    }

    static Optional<Configuration> makeConfigFromCommandLineArgs(String[] args) {
        var gitPath = FileSystems.getDefault().getPath("..").toAbsolutePath();
        var plugins = new HashMap<String, PluginConfig>();
        for (var arg : args) {
            if (arg.startsWith("--")) {
                if (arg.equals("--help"))
                    displayHelpAndExit();
                String[] parts = arg.split("=");
                if (parts.length != 2)
                    return Optional.empty();
                else {
                    String pName = parts[0];
                    String pValue = parts[1];
                    switch (pName) {
                        case "--addPlugin":
                            String[] config = pValue.split("\\?");
                            if (config[0].equals("countCommits")) {
                                System.out.println(" COUNTCOMMIT IS LOADING ");
                                plugins.put("countCommits", parseArgument(config));

                            } else if (config[0].equals("countModifiedLines")) {
                                System.out.println("COUNTMODIFIEDLINES IS LOADING");
                                plugins.put("countModifiedLines", parseArgument(config));

                            } else if (config[0].equals("countLines")) {
                                System.out.println(
                                        "COUNTLINES IS LOADING , MAY TAKE SOMETIMES IF YOU HAVE A CONSIDERABLE AMOUNT OF LINES   ");
                                plugins.put("countLines", parseArgument(config));

                            } else if (config[0].equals("countCommitsPerDate")) {
                                System.out.println("COUNTCOMMITS_PER_DATE IS LOADING");
                                config = (pValue + "?perDate").split("\\?");
                                plugins.put("countCommits", parseArgument(config));

                            } else if (config[0].equals("countTotalCommits")) {
                                System.out.println("COUNTTOTALCOMMITS IS LOADING");
                                plugins.put("countTotalCommits", parseArgument(config));

                            } else if (pValue.equals("countBranches")) {
                                System.out.println("COUNTBRANCHES IS LOADING");
                                plugins.put("countBranches", new PluginConfig() {
                                });
                            } else {
                                displayHelpAndExit();
                            }

                            break;
                        case "--loadConfigFile":
                            // TODO (load options from a file)
                            break;
                        case "--justSaveConfigFile":
                            // TODO (save command line options to a file instead of running the analysis)
                            break;

                        default: {
                            System.out.println("The argument " + pName + " does not exist");
                            System.out.println("--help to display the help");
                            System.exit(0);
                        }
                    }
                }
            } else {
                if (arg.charAt(0) != '/') {
                    arg = "../" + arg; // get visolog directory instead of cli
                }
                gitPath = FileSystems.getDefault().getPath(arg).toAbsolutePath();
            }
        }
        return Optional.of(new Configuration(gitPath, plugins));
    }

    /**
     * Parse argument and make an instance of PluginConfig
     * 
     * @param args
     * @return a PluginConfig
     */
    private static PluginConfig parseArgument(String[] args) {
        return new PluginConfig() {
            Map<String, String> config = new HashMap<String, String>();
            {
                for (int i = 1; i < args.length; i++) {
                    String[] arg = args[i].split(":");
                    if (arg.length == 2) {
                        config.put(arg[0], arg[1]);
                    } else
                        config.put(arg[0], "true");
                }
            }

            public Map<String, String> getConfig() {
                return config;
            }
        };
    }

    // TODO: print the list of options and their syntax
    private static void displayHelpAndExit() {
        System.out.println("Wrong command...");
        // System.out.println("--help to display the help");
        System.out.println("Command syntaxe : --args='[<directory>] argument1 argument2 ...' \n");
        System.out.println("Arguments :");
        System.out.println("   --addPlugin=<value>[options]");
        System.out.println("      list of the values :");
        System.out.println("         countCommits        - count the number of commits per author");
        System.out.println("         countCommitsPerDate - count the number of commits per day per author");
        System.out.println("         countLines          - count the number of lines by author");
        System.out.println("         countModifiedLines  - count the number of lines added and deleted by author");
        System.out.println("         countTotalCommits   - count the total number of commits");
        System.out.println("\n    Options syntaxe : ?<option1>:[<value1>]?<option2>:[<value2>]...");
        System.out.println(
                "         percentage (except for countCommitsPerDate and countTotalCommits) - get the results in percentage");
        System.out.println("      Only for countCommits, countModifiedLines and countCommitsPerDate :");
        System.out.println("         merge  - count only merge commits or exclude merge commits");
        System.out.println("                value : true or false (true if the value is not specified)");
        System.out.println("         before - count commits before a date");
        System.out.println("                value : date in format mm/dd/yyyy");
        System.out.println("         after  - count commits after a date");
        System.out.println("                value : date in format mm/dd/yyyy");
        System.out.println("         author - count commits from an author");
        System.out.println("                value : author name");
        System.out.println("\n   --help : display this help");
        System.out.println("\nExample : --args='. --addPlugin=countCommits?merge:false?percentage'");
        System.out.println("Will count commits that are not merge. The results will be in percentage by author");
        System.exit(0);
    }

}
