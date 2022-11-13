package up.visulog.gitrawdata;

/**
 * The LineBuilder class is used to create an instance of Line, which represents a line.
 */

public class LineBuilder {
	public String id;
	public String author;
    public String date;
    public String lineNumber;
    public String content;

    /**
     * Constructor that initializes the identifier of the line.
     * @param id the line identifier
     */
    public LineBuilder(String id) {
        this.id = id;
    }
    
    /**
     * Set the author.
     * @param author the line author
     * @return the instance with the new author
     */
    public LineBuilder setAuthor(String author) {
        this.author = author;
        return this;
    }

    /**
     * Set the date.
     * @param date the line date
     * @return the instance with the new date
     */
    public LineBuilder setDate(String date) {
        this.date = date;
        return this;
    }

    /**
     * Set the lineNumber.
     * @param lineNumber the line lineNumber
     * @return the instance with the new lineNumber
     */
    public LineBuilder setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    /**
     * Set the content.
     * @param content the line content
     * @return the instance with the new content
     */
    public LineBuilder setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * @return a line with the same values for each attribute
     */
    public Line createLine() {
        return new Line(id, author, date, lineNumber, content);
    }


}
