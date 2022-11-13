package up.visulog.analyzer;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that calculates percentage and the total of the values
 */
public class Percentage {
	Map<String, Number> data;
	int total;

	public Percentage(Map<String, Number> data) {
		this.data = data;
		for (var item : data.entrySet()) {
			total += item.getValue().intValue();
		}
	}

	/**
	 * @param author
	 * @return the percentage of the author
	 */
	public double getPercentagePerAuthor(String author) {
		float res = (float) (data.get(author).intValue() * 100) / total;
		return (double) Math.round(res * 10) / 10;
	}

	/**
	 * @return the sum of all the values
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * @return a map with author as key and the percentage of this author as value
	 */
	public Map<String, Number> getPercentageAsDouble() {
		Map<String, Number> percentage = new HashMap<String, Number>();
		for (var item : data.entrySet()) {
			percentage.put(item.getKey(), getPercentagePerAuthor(item.getKey()));
		}
		return percentage;
	}

	/**
	 * This method can be used in Result class to get the result in percentage.
	 * @return a map with author as key and the percentage of this author with "%" as value
	 */
	/*
	public Map<String, String> getPercentageAsString() {
		Map<String, String> percentage = new HashMap<String, String>();
		for (var item : data.entrySet()) {
			percentage.put(item.getKey(), getPercentagePerAuthor(item.getKey()) + "%");
		}
		return percentage;
	}
	*/
	
}
