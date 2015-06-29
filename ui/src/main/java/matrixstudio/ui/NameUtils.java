package matrixstudio.ui;

import matrixstudio.model.Named;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameUtils {

	private final static Pattern endWithNumberPattern = Pattern.compile("^([^\\d]*)(\\d+)$");
	
	/**
	 * <p>From given it constructs an other name that is considered as 
	 * successor. For example, 'foo' becomes 'foo1' and 'foo12' becomes 'foo13'.
	 * </p>
	 * @param name base
	 * @return successor, null if name is null.
	 */
	public static String successorName(String name) {
		if ( name == null ) return null;
		Matcher matcher = endWithNumberPattern.matcher(name);
		if (matcher.matches() ) {
			// name ends with integer, add one to integer
			String baseName = matcher.group(1);
			int currentNumber = Integer.parseInt(matcher.group(2));
			return baseName + (currentNumber+1);
		} else {
			// name doesn't ends with an integer, adds one
			return name + "1";
		}
	}
	
	/**
	 * <p>Constructs a available name from given one.</p>
	 * @param name base to use for name.
	 * @param others {@link Named} elements that already exist.
	 * @return a name with no conflict with the others.
	 */
	public static <T extends Named> String availableName(String name, List<T> others) {
		if ( name == null ) name = "NotNamed";
		boolean available = false;
		while (available == false) {
			available = true;
			for ( Named named : others ) {
				if ( name.equals(named.getName()) ) {
					available = false;
					break;
				}
			}
			if ( available == false ) {
				name = successorName(name);
			}
		}
		return name;
	}
	
}
