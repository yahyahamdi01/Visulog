package up.visulog.gitrawdata;

/**
 * The CommitBuilder class is used to create an instance of Commit, which represents a commit.
 */
public class CommitBuilder {
    private final String id;
    private String author;
    private String date;
    private String description;
    private String mergedFrom;
    private int changedFiles;
    private int addedLines;
    private int deletedLines;

    /**
     * Constructor that initializes the identifier of the commit.
     * @param id the commit identifier
     */
    public CommitBuilder(String id) {
        this.id = id;
    }
    
    /**
     * Set the author.
     * @param author the commit author
     * @return the instance with the new author
     */
    public CommitBuilder setAuthor(String author) {
        this.author = author;
        return this;
    }

    /**
     * Set the date.
     * @param date the commit date
     * @return the instance with the new date
     */
    public CommitBuilder setDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * Set the description.
     * @param description the commit description
     * @return the istance with the new description
     */
    public CommitBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Set the identifiers of the merged branches during the commit.
     * @param mergedFrom the identifiers of the merged branches
     * @return the instance with the new identifiers
     */
    public CommitBuilder setMergedFrom(String mergedFrom) {
        this.mergedFrom = mergedFrom;
        return this;
    }

    /**
     * Set the number of changed files
     * @param mergedFrom the number of changed files
     * @return the instance with the new value
     */
    public CommitBuilder setChangedFiles(int changedFiles) {
        this.changedFiles = changedFiles;
        return this;
    }
    
    /**
     * Set the number of added lines
     * @param mergedFrom the number of added lines
     * @return the instance with the new value
     */
    public CommitBuilder setAddedLines(int addedLines) {
        this.addedLines = addedLines;
        return this;
    }
    
    /**
     * Set the number of deleted lines
     * @param mergedFrom the number of deleted lines
     * @return the instance with the new value
     */
    public CommitBuilder setDeletedLines(int deletedLines) {
        this.deletedLines = deletedLines;
        return this;
    }

    /**
     * @return a commit with the same values for each attribute
     */
    public Commit createCommit() {
        return new Commit(id, author, date, description, mergedFrom, changedFiles, addedLines, deletedLines);
    }
}