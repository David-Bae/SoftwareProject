import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.text.SimpleDateFormat;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

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
			
			if(!(inputLine.isEmpty() || inputLine.substring(0, 2).equals("//"))) {		//공백과 주석을 무시.
				Bookmark tmp = new Bookmark(inputLine.split("[,;]"));
	
				if(tmp.checkTimeFormat() && tmp.checkURL()) {
					list.add(tmp);
				}
			}
		}
		
		input.close();
	}
	
	public int numBookmarks() {								// inform number of bookmark list
		return list.size();
	}
	
	public Bookmark getBookmark(int i) {					// inform information of i'th bookmark
		return list.get(i);
	}

	public void mergeByGroup() {		
		for(int i = 0 ; i < list.size()-1 ; i++) {
			if(!list.get(i).getGroupName().equals("")) {
				int j = i+1;
				
				while(!(list.get(i).getGroupName().equals(list.get(j).getGroupName()) || j == list.size()-1))
					j++;
				
				if(list.get(i).getGroupName().equals(list.get(j).getGroupName())) {
					Bookmark tmp = list.remove(j);
					list.add(i+1, tmp);
				}
			}
		}
	}
	
	static BookmarkList getBookmarkList(String filename) {
		return new BookmarkList(filename);
	}
	
}

//////	Bookmark
//////
//////
//////
//////
//////	GUI 

class BookmarkManagerFrame extends JFrame{	//Main Frame
	private static BookmarkList list;
	
	class BookmarkListPanel extends JPanel{
		BookmarkListPanel(){
			setLayout(new GridLayout(1,1));
			
			DefaultTableModel model = new DefaultTableModel(new String[]
					{"", "Group", "Name", "URL", "Created Time", "Memo" }, 0);
			JTable table = new JTable(model);
			JScrollPane scroll = new JScrollPane(table);
			
			String[][] rows = toTable();		//BookmarkList를 읽어서 close된 상태로 출력.
			for(String[] row : rows)				//
				model.addRow(row);					//
			
			table.getTableHeader().setBackground(Color.LIGHT_GRAY);		//JTable color and size
			table.setGridColor(Color.LIGHT_GRAY);
			table.getColumnModel().getColumn(0).setMaxWidth(25);
			table.getColumnModel().getColumn(1).setMinWidth(85);
			table.getColumnModel().getColumn(2).setMinWidth(85);
			table.getColumnModel().getColumn(3).setMinWidth(230);
			table.getColumnModel().getColumn(4).setMinWidth(180);
			
			add(scroll);
		}
	}
	
	class MenuPanel extends JPanel{	//ADD, DEL, UP... Menu Panel
		JButton addBtn = new JButton("ADD");
		JButton delBtn = new JButton("DELETE");
		JButton upBtn = new JButton("UP");
		JButton downBtn = new JButton("DOWN");
		JButton saveBtn = new JButton("SAVE");
		
		MenuPanel(){
			setLayout(new GridLayout(5,1));
			add(addBtn);
			add(delBtn);
			add(upBtn);
			add(downBtn);
			add(saveBtn);
			
			addBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BookmarkInfoFrame fff = new BookmarkInfoFrame();
				}							
			});
		}
	}
	
	BookmarkManagerFrame(BookmarkList list){
		super("Bookmark Manager");
		list.mergeByGroup();
		this.list = list;
		
		setLayout(new BorderLayout());
		add(new BookmarkListPanel(), BorderLayout.CENTER);
		add(new MenuPanel(), BorderLayout.EAST);
		
		setLocation(450, 200);
		setSize(800, 350);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	static String[][] toTable(){							
		String[][] rowBuffer = new String[BookmarkManagerFrame.list.numBookmarks()][6];		
		
		int row = 0;
		int listIndex = 0;
		String groupName;
		
		while(listIndex != BookmarkManagerFrame.list.numBookmarks()) {		//모든 북마크를 참조할 때까지 반복.
			if(BookmarkManagerFrame.list.getBookmark(listIndex).getGroupName().isEmpty()) {
				String[] bookmarkContent = { 
						"",
						"",
						BookmarkManagerFrame.list.getBookmark(listIndex).getName(),
						BookmarkManagerFrame.list.getBookmark(listIndex).getURL(),
						BookmarkManagerFrame.list.getBookmark(listIndex).getTime(),
						BookmarkManagerFrame.list.getBookmark(listIndex).getMemo()
					};
				rowBuffer[row++] = bookmarkContent;
				listIndex++;
			} else {
				groupName = BookmarkManagerFrame.list.getBookmark(listIndex).getGroupName();
				String[] bookmarkContent = { "  >", groupName, "", "", "", ""};
				rowBuffer[row++] = bookmarkContent;
				listIndex++;
				
				while(listIndex != BookmarkManagerFrame.list.numBookmarks() && 
						BookmarkManagerFrame.list.getBookmark(listIndex).getGroupName().equals(groupName))
					listIndex++;
			}
		}
		
		String[][] rows = new String[row][6];
		for(int i=0 ; i < row ; i++)
			rows[i] = rowBuffer[i];
		
		return rows;
	}
	
}

class BookmarkInfoFrame extends JFrame{
	class BookmarkAddPanel extends JPanel{
		BookmarkAddPanel(){
			setLayout(new GridLayout(1,1));
			DefaultTableModel model = new DefaultTableModel(new String[]{"Group", "Name", "URL", "Memo"}, 0);
			JTable table = new JTable(model);
			JScrollPane scroll = new JScrollPane(table);
			model.addRow(new String[] {"", "", "", ""});
			
			table.getTableHeader().setBackground(Color.LIGHT_GRAY);		//JTable color and size
			table.setGridColor(Color.LIGHT_GRAY);
			table.getColumnModel().getColumn(0).setMinWidth(120);
			table.getColumnModel().getColumn(0).setMaxWidth(120);
			table.getColumnModel().getColumn(1).setMinWidth(120);
			table.getColumnModel().getColumn(1).setMaxWidth(120);
			table.getColumnModel().getColumn(2).setMinWidth(230);
			table.getColumnModel().getColumn(3).setMinWidth(80);
			
			add(scroll);
		}
	}
	
	class InputPanel extends JPanel{	//Input Button Panel - Event Listener needed
		JButton inputBtn = new JButton("Input");
		
		InputPanel(){
			setLayout(new GridLayout(1,1));
			add(inputBtn);
		}
	}
	
	BookmarkInfoFrame(){
		super("Input New Bookmark");
		
		setLayout(new BorderLayout());
		add(new BookmarkAddPanel(), BorderLayout.CENTER);
		add(new InputPanel(), BorderLayout.EAST);
		
		setLocation(450, 570);
		setSize(800, 100);
		setVisible(true);
	}
}

//////	GUI
//////
//////
//////
//////	Main

public class AutoTest {
	public static void main(String[] args) {
		BookmarkList list = new BookmarkList("bookmark.txt");
		//BookmarkList list = new BookmarkList("bookmark-org.txt");
		
		new BookmarkManagerFrame(list);
	}
}