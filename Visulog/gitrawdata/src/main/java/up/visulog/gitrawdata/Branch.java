package up.visulog.gitrawdata;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Branch{


	public final String date;
	public final String author;
	public final String name;

	public Branch(String date,String author,String name) {
		this.date = date;
		this.author = author;
		this.name=name;
	}


	public static List<Branch> parseLog2FromCommand(Path gitPath) {
		ProcessBuilder builder2 =
				new ProcessBuilder("git","for-each-ref","--format='%(color:cyan)%(authordate:format:%m/%d/%Y %I:%M %p)    %(align:25,left)%(color:yellow)%(authoremail)%(end) %(color:reset)%(refname:strip=3)'","--sort=authordate","refs/remotes").directory(gitPath.toFile());
		Process process2;
		try {
			process2 = builder2.start();
		} catch (IOException e) {
			throw new RuntimeException("Error running \"git command\".", e);
		}
		InputStream is = process2.getInputStream();
		BufferedReader reader2 = new BufferedReader(new InputStreamReader(is));
		return parseLog2(reader2);
	} 


	public static List<Branch> parseLog2(BufferedReader reader2) {
		var result2 = new ArrayList<Branch>();
		
		try {
			String a=reader2.readLine();
		 
			while(a !=null) {
			    Branch b=parseBranch(a);
	        	result2.
				add(b);
				a=reader2.readLine();
			}
		}

	
		catch (IOException e) {
			parseError2();
		
		}
return result2;
	}


	
	public static  Branch parseBranch(String line) {
		
		Scanner c=new Scanner(line);
		String [] st=new String[4];
	    int a=0;
        
		while(c.hasNext()) {
			st[a]=c.next();
			a++;
		}
		
		st[0]=st[0].substring(1,st[0].length());
	
		st[3]=st[3].substring(0,st[3].length()-1);
		var builder = new BranchBuilder(st[0]);
		builder.setAuthor(st[2]);
		builder.setName(st[3]);
		return builder.createBranch();
 


	}


	// Helper function for generating parsing exceptions. This function *always* quits on an exception. It *never* returns.
	private static void parseError2() {
		throw new RuntimeException("error.");
	}

	@Override
	public String toString() {
		return "Branch{" +
				"date='" + date + '\'' +
				", author='" + author + '\'' +
				", name='" + author + '\''+
				'}';
	}
}
