package wallstreetcnsave;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;


class WallstreetcnSave implements Runnable {
	
	private GetrequestUrl release;
	public WallstreetcnSave(GetrequestUrl url) {
		this.release = url; // all threads share the same GetrequestUrl
	}
	
	private static String DataBaseName = "textclassify";
	private static String CollectionName = "WallstreetSaveJava";
	
	private static String Regex = ".*?\"type\":\"(.*?)\".*?\"contentHtml\":\"<p>(.*?)<\\\\/p>\".*?\"categorySet\":\"(.*?)\".*?";
	private static final String REGEXSTRING1 = "type";
	private static final String REGEXSTRING2 = "content";
	private static final String REGEXSTRING3 = "categoryset";
	
	/ / Map table storage
	public static Map<String, String> GetMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("1", "Forex");
		map.put("2", "Stock Market");
		map.put("3", "item");
		map.put("4", "bond market");
		map.put("9", "China");
		map.put("10", "United States");
		map.put("11", "Eurozone");
		map.put("12", "Japan");
		map.put("13", "United Kingdom");
		map.put("14", "Australia");
		map.put("15", "Canada");
		map.put("16", "Switzerland");
		map.put("17", "Other Areas");
		map.put("5", "Central Bank");
		return map;
	}
	private static String[] ruleList_district = { "9", "10", "11", "12", "13", "14", "15", "16", "17" };
	private static String[] ruleList_property = { "1", "2", "3", "4" };
	private static String[] ruleList_centralbank = { "5" };
	
	// Separate and filter content in x, x, x format
	public static String setCategory(String categorySet, String[] ruleList, Map<String, String> map) {
		StringBuffer disStr = new StringBuffer(); 
		String[] strArray = null;
		strArray = categorySet.split(","); // split the character to "," and then pass the result to the array strArray
		// Get the information you need
		int length_strArray = strArray.length;
		int length_ruleList = ruleList.length;
		
		if (length_strArray > 0) {
			for (int iArr = 0; iArr < length_strArray; iArr++) {
				String s = strArray[iArr];
					for (int iRul=0; iRul < length_ruleList; iRul++) {
						if (s.equals(ruleList[iRul])) {
							disStr.append(map.get(s));
							disStr.append(",");
								break;
							}
						}
				}
			}
			if(disStr.length()>1) {
				disStr = disStr.deleteCharAt(disStr.length()-1);
			}
			return disStr.toString();
		}
	
	// read the entire page, return the html string
	private static String httpRequest(String requestUrl) {
		StringBuffer buffer = null;
		BufferedReader bufferedReader = null;
		InputStreamReader inputStreamReader = null;
		InputStream inputStream = null;
		HttpURLConnection httpUrlConn = null;
		try {
			// Create a get request
			URL url = new URL(requestUrl);
			httpUrlConn = (HttpURLConnection) url.openConnection();
			httpUrlConn.setDoInput(true);
			httpUrlConn.setRequestMethod("GET");
			// Get the input stream
			inputStream = httpUrlConn.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
			bufferedReader = new BufferedReader(inputStreamReader);
			// Get results from the input stream
			buffer = new StringBuffer();
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {
				str = new String(str.getBytes(), "UTF-8");
				buffer.append(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (httpUrlConn != null) {
				httpUrlConn.disconnect();
			}
		}
		return buffer.toString();
	}

	// Filter out useless information
	public static List<Map<String, String>> htmlFiter(String html, String Regex) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		// find the target
		Pattern p = Pattern.compile(Regex);
		Matcher m = p.matcher(html);
		while (m.find()) {
			Map<String, String> map_save = new HashMap<String, String>();
			// modifiable part
			map_save.put(REGEXSTRING1, m.group(1));
			map_save.put(REGEXSTRING2, m.group(2));
			map_save.put(REGEXSTRING3, m.group(3));
			
			list.add(map_save);
		}
		return list;
	}
	
	// unicode format to Chinese
	public static String UnicodeToString(String str) {
			Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))"); // XDigit represents a hexadecimal number, and \p in the regular expression represents a Unicode block.
			Matcher matcher = pattern.matcher(str);
			char ch;
			while (matcher.find()) {
				ch = (char) Integer.parseInt(matcher.group(2), 16); // hexadecimal to decimal as ascii code, then char to character
				str = str.replace(matcher.group(1), ch + "");
			}
			return str;
		}
	
	public void run() {
		while(true) { // Loop body! ! !
			// Connect to the database
			try {
				Mongo mongo = new Mongo("localhost", 27017);
				DB db = mongo.getDB(DataBaseName);
				DBCollection collection = db.getCollection(CollectionName);
				
				// Call the method of grabbing to get content
				String requestUrl = this.release.GetMethod();
				if(requestUrl.equals("")) {
					break;
				} else {
					System.out.println(requestUrl);
					
					String html = httpRequest(requestUrl);
					List<Map<String, String>> resultList = htmlFiter(html, Regex);
					
					if (resultList.isEmpty()) {
						System.out.printf("The end url: %s", requestUrl);
						break;
					} else {
						for (Map<String, String> result : resultList) {
							BasicDBObject dbObject = new BasicDBObject();
							
							String type = result.get(REGEXSTRING1);
							String content = UnicodeToString(result.get(REGEXSTRING2));
							
							Map<String, String> map = GetMap();
							String district = setCategory(result.get(REGEXSTRING3), ruleList_district, map); 
							String property = setCategory(result.get(REGEXSTRING3), ruleList_property, map);
							String centralbank = setCategory(result.get(REGEXSTRING3), ruleList_centralbank, map);
							
							Date date = new Date();
							DateFormat time = DateFormat.getDateTimeInstance();
							String time_str = time.format(date);
							
							String source = "wangstreetcn";
		
							dbObject.put("content", content); // specific contents
							dbObject.put("createdtime", time_str); // create time
							dbObject.put("source", source); // Information source
							dbObject.put("district", district); // region
							dbObject.put("property", property); // asset class
							dbObject.put("centralbank", centralbank); // asset class
							dbObject.put("type", type); //Information type
							
							collection.insert(dbObject);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}

	public void run1() {
		while(true) { // Loop body! ! !
			// Connect to the database
			try {
				Mongo mongo = new Mongo("localhost", 27017);
				DB db = mongo.getDB(DataBaseName);
				DBCollection collection = db.getCollection(CollectionName);
				
				// Call the method of grabbing to get content
				String requestUrl = this.release.GetMethod();
				if(requestUrl.equals("")) {
					break;
				} else {
					System.out.println(requestUrl);
					
					String html = httpRequest(requestUrl);
					List<Map<String, String>> resultList = htmlFiter(html, Regex);
					
					if (resultList.isEmpty()) {
						System.out.printf("The end url: %s\n", requestUrl);
						break;
					} else {
						for (Map<String, String> result : resultList) {
							BasicDBObject dbObject = new BasicDBObject();
							
							String type = result.get(REGEXSTRING1);
							String content = UnicodeToString(result.get(REGEXSTRING2));
							
							Map<String, String> map = GetMap();
							String district = setCategory(result.get(REGEXSTRING3), ruleList_district, map); 
							String property = setCategory(result.get(REGEXSTRING3), ruleList_property, map);
							String centralbank = setCategory(result.get(REGEXSTRING3), ruleList_centralbank, map);
							
							Date date = new Date();
							DateFormat time = DateFormat.getDateTimeInstance();
							String time_str = time.format(date);
							
							String source = "wangstreetcn";
		
							dbObject.put("content", content); // specific contents
							dbObject.put("createdtime", time_str); // create time
							dbObject.put("source", source); // Information source
							dbObject.put("district", district); // region
							dbObject.put("property", property); // asset class
							dbObject.put("centralbank", centralbank); // asset class
							dbObject.put("type", type); //Information type
							
							collection.insert(dbObject);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
}
	
/**
 * contain shared resource
 */
class GetrequestUrl {
	
	private String url = "http://api.wallstreetcn.com/v2/livenews?&page=";
	private int start;
	private int end = 5000;
	
	public GetrequestUrl(int start) 
	{
		this.start = start;
	}
	public GetrequestUrl(int start, int end) 
	{
		this.start = start;
		this.end = end;
	}

	/**
	 * Thread safe method
	 */
	public synchronized String GetMethod() // Synchronize the entire method with the synchronized modifier
	{
		if(this.start <= this.end) {
			String requestUrl = this.url+this.start;
			this.start = this.start+1;
			return requestUrl;
		}
		else {
			return ""; 
		}
	}
}


public class WallstreetcnSaveTest {
	public static void main(String[] args) {		
		// multi-threaded crawling
		int start = 1;
		GetrequestUrl url = new GetrequestUrl(start);
//		int start = 1, end = 3000;
//		GetrequestUrl url = new GetrequestUrl(start, end);
		
		int thread_num = 1;
		while(true) {
			if(thread_num++ > 8) break;
			Thread thread = new Thread(new WallstreetcnSave(url));
			thread.start();
		}
		
	}
}
