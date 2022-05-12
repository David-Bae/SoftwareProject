import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.awt.*;
import javax.swing.*;

class Bookmark {
	private String name = new String();
	private String time = new String();
	private String url = new String();
	private String groupName = new String();
	private String memo = new String();
	
	Bookmark() { }
	
	Bookmark(String url){
		LocalDateTime time = LocalDateTime.now();
		this.time = time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"));
		this.url = url;
	}
	
	Bookmark(String[] data){		
		String[] buffer = {"", "", "", "", ""};
		
		for(int i = 0 ; i < data.length ; i++) {
			buffer[i] = data[i].trim();
		}
		
		name = buffer[0];
		time = buffer[1];
		url = buffer[2];
		groupName = buffer[3];
		memo = buffer[4];
	}
	
	public String getName() {
		return name;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getURL() {
		return url;
	}
	
	public String getGroupName() {
		return groupName;
	}
	
	public String getMemo() {
		return memo;
	}
	
	public void setName(String name) {
		this.name = name.trim();
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public void setURL(String url) {
		this.url = url.trim();
	}
	
	public void setGroupName(String groupName) {
		this.groupName = groupName.trim();
	}
	
	public void setMemo(String memo) {
		this.memo = memo.trim();
	}
	
	
	public void print() {
		System.out.println(name + "," + time + "," + url + "," + groupName + "," + memo);
	}
	
	public boolean checkTimeFormat() {
		try {				
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
			format.setLenient(false);
			format.parse(time);
	
		} catch(Exception e) {
			System.out.println("Date Format Error -> No created Time invalid Bookmark info line: "
								+ name + ";" + time + ";" + url + ";" + groupName + ";" + memo);
			return false;
		}
		
		return true;
	}
	
	public boolean checkURL() {
		if(url.equals("")) {
			System.out.println("MalformedURLException: wrong URL - No URL ; invalid Bookmark info line: "		//No URL
					+ name + ";" + time + ";" + url + ";" + groupName + ";" + memo);
			return false;
		}
		else
			return true;
	}
	
}

class BookmarkList {
	private ArrayList<Bookmark> list = new ArrayList<Bookmark>();
	private int listNum = 0;
	
	BookmarkList(String filename){
		File file = new File(filename);			//create File instance of filename
		Scanner input = null;
		
		try {
			input = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.out.println("Unknwon BookmarkList data File");
			return;
		}
		
		while(input.hasNext()) {
			String inputLine = input.nextLine().trim();					
			
			if(!(inputLine.equals("")) && !(inputLine.substring(0, 2).equals("//"))) {		//공백과 주석을 무시.
				
				Bookmark tmp = new Bookmark(inputLine.split("[,;]"));
				
				if(tmp.checkTimeFormat() && tmp.checkURL()) {
					list.add(tmp);
					listNum++;
				}
			}
		}
		
		input.close();
	}
	
	public int numBookmarks() {								// inform number of bookmark list
		return listNum;
	}
	
	public Bookmark getBookmark(int i) {					// inform information of i'th bookmark
		return list.get(i);
	}

	public void mergeByGroup() {
		int sorted = 0;					//index of sorted

		while(sorted != listNum - 2) {						//n-1번째 요소까지 정렬.
			int find = sorted + 1;
			
			if(list.get(sorted).getGroupName().equals(""))
				sorted++;
			else {
				while(find != listNum && !(list.get(sorted).getGroupName().equals(list.get(find).getGroupName())))		//그룹이름이 같고, 인덱스가 가장 작은 것 구하기.
					find++;
				
				if(find != listNum) {
					for(int i = find; i > sorted + 1; i--) {
						Bookmark tmp = new Bookmark();
						
						tmp = list.get(i);
						list.add(i, list.get(i-1));
						list.add(i-1, tmp);
					}
				}
			}

				sorted++;
		}
	}
	
	static BookmarkList getBookmarkList(String filename) {
		return new BookmarkList(filename);
	}
	
}

//GUI classes

class BookmarkManagerFrame extends JFrame{
	class MenuPanel extends JPanel{
		MenuPanel(){
			setLayout(new GridLayout(5,1));
			add(new JButton("ADD"));
			add(new JButton("DELETE"));
			add(new JButton("UP"));
			add(new JButton("DOWN"));
			add(new JButton("SAVE"));
		}
	}
	
	BookmarkManagerFrame(BookmarkList list){
		super("Bookmark Manager");
		setLayout(new BorderLayout());
		
		BookmarkTable table = new BookmarkTable(toTable(list));
		JScrollPane scrollPane = new JScrollPane(table);
		
		add(scrollPane, BorderLayout.CENTER);
		add(new MenuPanel(), BorderLayout.EAST);
		
		pack();
		setVisible(true);
		setDefaultCloseOperation(super.EXIT_ON_CLOSE);
	}
	
	static String[][] toTable(BookmarkList list){
		String[][] contents = new String[list.numBookmarks()][6];
		
		for(int i = 0 ; i < list.numBookmarks() ; i++) {
			String[] bookmarkContent = { 
											"",
											list.getBookmark(i).getGroupName(),
											list.getBookmark(i).getName(),
											list.getBookmark(i).getURL(),
											list.getBookmark(i).getTime(),
											list.getBookmark(i).getMemo()
										};
			contents[i] = bookmarkContent;
		}
			
		return contents;
	}
	
}

class BookmarkTable extends JTable{
	static String[] header = {"", "Group", "Name", "URL", "Created Time", "Memo" };
	
	BookmarkTable(String[][] contents){
		super(contents, header);
	}
	
}









public class AutoTest {
	public static void main(String[] args) {
		BookmarkList test = new BookmarkList("bigbang.txt");
		test.mergeByGroup();
		
		for(int i =0; i< test.numBookmarks() ; i++) {
			test.getBookmark(i).print();
		}
		
		BookmarkManagerFrame aaaa = new BookmarkManagerFrame(test);
		
	}
}