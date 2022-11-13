package up.visulog.gitrawdata;

public class BranchBuilder {
    
    private String author;
    private String date;
    private String name;


    public BranchBuilder(String date) {
        this.date = date;
       
    }

    public BranchBuilder setAuthor(String author) {
        this.author = author;
        return this;
    }

    public BranchBuilder setDate(String date) {
        this.date = date;
        return this;
    }

   public BranchBuilder setName(String name) {
	   this.name=name;
	   return this;
   }
	
    public Branch createBranch() {
        return new Branch( date, author, name);
    }
}

