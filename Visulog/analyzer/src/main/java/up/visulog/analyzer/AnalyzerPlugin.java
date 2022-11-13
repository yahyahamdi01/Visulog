package up.visulog.analyzer;

import java.lang.module.Configuration;

public interface AnalyzerPlugin {
    //contains an interface which contains methods to get the results of the plugin

    interface Result {

        //methods that are implemented in other classes, to get the result in String or HTML text
        String getResultAsString();
        String getResultAsHtmlDiv();
    }

    /**
     * run this analyzer plugin
     */
    void run();

    /**
     *
     * @return the result of this analysis. Runs the analysis first if not already done.
     */
    Result getResult();
}
