import java.io.File;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteStatement;

import gnu.getopt.*;

public class fspotDbMerge {

	/**
	 * @param args
	 * @throws SQLiteException 
	 */
	public static void main(String[] args) throws SQLiteException {
		
		Getopt opts = new Getopt("fspotDbMerge",args,"l:r:s:d:h");
		int c;
		String local_base="";
		String remote_base="";
		String source_db="";
		String destination_db="";
		while ((c = opts.getopt()) != -1)
		   {
		     switch(c)
		       {
		       		case 'l':
		       			local_base=opts.getOptarg();
		       			break;
		       		case 'r':
		       			remote_base=opts.getOptarg();
		       			break;
		       		case 's':
		       			source_db=opts.getOptarg();
		       			break;
	       			case 'd':
		       			destination_db=opts.getOptarg();
	       				break;
		       		case 'h':
		       			System.out.println("Usage: fpotDbMerge -l local_base -r remote_base -s source_db -d destination_db");
		       			break;
		       		default:
		       			break;
		       }
		   }
		
		SQLiteConnection dbSource = new SQLiteConnection(new File(source_db));
		dbSource.open(true);
		
		SQLiteConnection dbDestination = new SQLiteConnection(new File(destination_db));
		dbDestination.open(true);
		
		SQLiteStatement stSrcFotos = dbSource.prepare("SELECT id,time,base_uri,filename,description,roll_id,default_version_id,rating,md5_sum from photos");
		SQLiteStatement stRemoteFotos = dbDestination.prepare("SELECT id,time,base_uri,filename,description,roll_id,default_version_id,rating,md5_sum from photos where filename=?"); 
		SQLiteStatement stSrcRoll = dbSource.prepare("SELECT time from rolls where id = ?");
		SQLiteStatement stRemoteRoll = dbDestination.prepare("SELECT id from rolls where time = ?");
		SQLiteStatement stLocalVersions = dbSource.prepare("SELECT version_id,name,base_uri,filename,md5_sum,protected from photo_versions where photo_id = ?");
		
		SQLiteStatement stInsertRoll = dbDestination.prepare("INSERT into rolls (time) VALUES (?)");
		SQLiteStatement stInsertFoto = dbDestination.prepare("INSERT into photos (time,base_uri,filename,description,roll_id,default_version_id,rating,md5_sum) VALUES (?,?,?,?,?,?,?,?)");
		SQLiteStatement stInsertVersion = dbDestination.prepare("INSERT into photo_versions (photo_id,version_id,name,base_uri,filename,md5_sum,protected) VALUES (?,?,?,?,?,?,?)");
		
	    try {
	      while (stSrcFotos.step()) {
	    	  stRemoteFotos.reset();
	    	  stRemoteFotos.bind(1, stSrcFotos.columnString(3));
	    	  if (!stRemoteFotos.step()) {
	    		  Long roll=0L;
	    		  Long photo_id=0L;
	    		  stSrcRoll.reset();
	    		  stSrcRoll.bind(1, stSrcFotos.columnLong(5));
	    		  if (stSrcRoll.step()) {
	    			  stRemoteRoll.reset();
	    			  stRemoteRoll.bind(1, stSrcRoll.columnLong(0));
	    			  if (!stRemoteRoll.hasRow()) {
	    				  System.out.println("Will insert "+stSrcRoll.columnLong(0)+" roll");
	    				  stInsertRoll.reset();
	    				  stInsertRoll.bind(1, stSrcRoll.columnLong(0));
	    				  stInsertRoll.step();
		    			  stRemoteRoll.reset();
		    			  stRemoteRoll.bind(1, stSrcRoll.columnLong(0));
		    			  if (stRemoteRoll.step()) {
		    				  roll=stRemoteRoll.columnLong(0);
		    			  }
		    			  else {
		    				continue;  
		    			  }
	    			  }
	    			  else {
	    				  roll=stRemoteRoll.columnLong(0);
	    			  }
	    		  }
	    		  if (roll==0L) {
	    			  System.out.println("Error inserting roll (inserted roll not found)!");
	    			  continue;
	    		  }
	    		  System.out.println("Roll id is "+roll);
	    		  String base_uri=stSrcFotos.columnString(2);
	    		  base_uri = base_uri.replaceFirst(local_base, remote_base);
	    		  System.out.println("base_uri is now "+base_uri);
	    		  System.out.println("Will insert "+stSrcFotos.columnString(3)+" foto");
	    		  stInsertFoto.reset();
	    		  stInsertFoto.bind(1, stSrcFotos.columnLong(1));
	    		  stInsertFoto.bind(2, base_uri);
	    		  stInsertFoto.bind(3, stSrcFotos.columnString(3));
	    		  stInsertFoto.bind(4, stSrcFotos.columnString(4));
	    		  stInsertFoto.bind(5, roll);
	    		  stInsertFoto.bind(6, stSrcFotos.columnLong(6));
	    		  stInsertFoto.bind(7, stSrcFotos.columnLong(7));
	    		  stInsertFoto.bind(8, stSrcFotos.columnString(8));
	    		  stInsertFoto.step();
	    		  stRemoteFotos.reset();
	    		  stRemoteFotos.bind(1, stSrcFotos.columnString(3));
	    		  if (stRemoteFotos.step()) {
	    			  photo_id=stRemoteFotos.columnLong(0);	    	    	 
	    		  }
	    		  if (photo_id==0L) {
	    			  System.out.println("Error inserting photo (inserted photo not found)!");
					  continue;	    			  
	    		  }
		    	  stLocalVersions.reset();
		    	  stLocalVersions.bind(1, stSrcFotos.columnLong(0));
		    	  while (stLocalVersions.step()) {
		    		  System.out.println("Will insert "+stLocalVersions.columnString(1)+" version");
		    		  String base_uri_ver=stLocalVersions.columnString(2);
		    		  base_uri_ver = base_uri_ver.replaceFirst(local_base, remote_base);
		    		  System.out.println("base_uri for version is now "+base_uri_ver);
		    		  //photo_id,version_id,name,base_uri,filename,md5_sum,protected
		    		  //version_id,name,base_uri,filename,md5_sum,protected
		    		  stInsertVersion.reset();
		    		  stInsertVersion.bind(1, photo_id);
		    		  stInsertVersion.bind(2, stLocalVersions.columnLong(0));
		    		  stInsertVersion.bind(3, stLocalVersions.columnString(1));
		    		  stInsertVersion.bind(4, base_uri_ver);
		    		  stInsertVersion.bind(5, stLocalVersions.columnString(3));
		    		  stInsertVersion.bind(6, stLocalVersions.columnString(4));
		    		  stInsertVersion.bind(7, stLocalVersions.columnString(5));
		    		  stInsertVersion.step();
		    	  }
	    	  }
	      }      
	    } 
	    finally {
	    	stSrcFotos.dispose();
	    	stRemoteFotos.dispose();
	    	stSrcRoll.dispose();
	    	stRemoteRoll.dispose();
	    	stLocalVersions.dispose();
	    }
	    dbSource.dispose();
	    dbDestination.dispose();
		
	}

}
