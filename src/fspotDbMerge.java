import java.io.File;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

public class fspotDbMerge {

	/**
	 * @param args
	 * @throws SQLiteException 
	 */
	public static void main(String[] args) throws SQLiteException {
		SQLiteConnection db1 = new SQLiteConnection(new File(args[0]));
		db1.open(true);
		
		SQLiteConnection db2 = new SQLiteConnection(new File(args[1]));
		db2.open(true);

		
		SQLiteStatement st = db1.prepare("SELECT * from photos");
	    try {
	      while (st.step()) {
	        System.out.println(st.columnLong(0));
	      }
	    } finally {
	      st.dispose();
	    }
	    db1.dispose();
	    db2.dispose();
		
	}

}
