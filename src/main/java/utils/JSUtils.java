package utils;

public class JSUtils {
	public static final String getElementByXpathDef =
	"function getElementByXPath(p, d) {" + 
			"d = d || document;" +
			"return d.evaluate(p, d, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;" +
	"};";

}
