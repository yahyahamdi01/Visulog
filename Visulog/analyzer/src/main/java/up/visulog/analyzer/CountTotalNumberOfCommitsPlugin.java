package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.gitrawdata.Commit;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;


//to obtain the total number of commits made by all the contributors
public class CountTotalNumberOfCommitsPlugin implements AnalyzerPlugin{
   private final Configuration configuration;
   private Result result;

   public CountTotalNumberOfCommitsPlugin(Configuration generalConfiguration){
      this.configuration=generalConfiguration;
   }

   //take a list of commit and return the number of commits
   static Result processLog(List<Commit> gitLog){ 
      var result = new Result();
      result.totalCommits=gitLog.size();
      return result;
   }

   //take the list of commit and process it with processLog
   @Override
   public void run() {
      result = processLog(Commit.parseLogFromCommand(configuration.getGitPath(), "git", "log"));
   }

   @Override
   public Result getResult() {
      if (result == null) run();
      return result;
  }

  //result is an int : the number of total commits
  static class Result implements AnalyzerPlugin.Result{
     private int totalCommits=0;

     int getTotalCommits(){
        return totalCommits;
     }

     //transform result into a String
     @Override
     public String getResultAsString(){
        return String.valueOf(totalCommits);
     }

     //transform result into a HTML text
     @Override
     public String getResultAsHtmlDiv() {
       StringBuilder html = new StringBuilder("<div>Total commits: <ul>");
       html.append("<li>").append(totalCommits).append("</li>");
       html.append("</ul></div>");
       return html.toString();
     }

  }

}
