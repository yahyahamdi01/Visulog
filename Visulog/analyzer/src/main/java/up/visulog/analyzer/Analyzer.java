package up.visulog.analyzer;

import up.visulog.config.Configuration;
import up.visulog.config.PluginConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Analyzer {
    //take a configuration, can run the plugins & return its results

    private final Configuration config;

    private AnalyzerResult result;

    public Analyzer(Configuration config) { //builder
        this.config = config;
    }

    public AnalyzerResult computeResults() { //return in an AnalyzerResult the result of all the plugins not empty
        List<AnalyzerPlugin> plugins = new ArrayList<>();
        //go through the values of the config
        for (var pluginConfigEntry: config.getPluginConfigs().entrySet()) {
            var pluginName = pluginConfigEntry.getKey();
            var pluginConfig = pluginConfigEntry.getValue();
            var plugin = makePlugin(pluginName, pluginConfig);
            plugin.ifPresent(plugins::add);
        }
        // run all the plugins
        // TODO: try running them in parallel
        for (var plugin: plugins) {
        	plugin.run();
        }

        // store the results together in an AnalyzerResult instance and return it
        return new AnalyzerResult(plugins.stream().map(AnalyzerPlugin::getResult).collect(Collectors.toList()));
    }

    // TODO: find a way so that the list of plugins is not hardcoded in this factory
    private Optional<AnalyzerPlugin> makePlugin(String pluginName, PluginConfig pluginConfig) {
        //if the name of the plugin is "countCommits" & the number of commits of the config is not nul, it returns its value
        //else it returns the Optional empty
        switch (pluginName) {
            case "countCommits" : return Optional.of(new CountCommitsPerAuthorPlugin(config));
            case "countTotalCommits" : return Optional.of(new CountTotalNumberOfCommitsPlugin(config));
            case "countCommitsPerDate" : return Optional.of(new CountCommitsPerAuthorPlugin(config));
            case "countModifiedLines" : return Optional.of(new CountModifiedLinesPerAuthorPlugin(config));
            case "countLines" : return Optional.of(new CountLinesPerAuthorPlugin(config));
            case "countBranches" :return Optional.of(new CountBranchesPerAuthorPlugin(config));
            default : return Optional.empty();
        }
    }

}



