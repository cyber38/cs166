/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");

			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}

	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 *
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException {
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 *
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 * obtains the metadata object for the returned result set.  The metadata
		 * contains row and column info.
		*/
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;

		//iterates through the result set and saves the data returned by the query.
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>();
		while (rs.next()){
			List<String> record = new ArrayList<String>();
			for (int i=1; i<=numCol; ++i)
				record.add(rs.getString (i));
			result.add(record);
		}//end while
		stmt.close ();
		return result;
	}//end executeQueryAndReturnResult

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 *
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}

	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current
	 * value of sequence used for autogenerated keys
	 *
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */

	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();

		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 *
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if

		DBproject esql = null;

		try{
			System.out.println("(1)");

			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}

			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];

			esql = new DBproject (dbname, dbport, user, "");

			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");

				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		int sid;
		String name;
		String specialty;
		String hospital;
		int did;
		String query;
		String docq;
		String departq;

		//Get doctorid
		do {
			try {
				docq = "SELECT * FROM Doctor;";
				sid = esql.executeQuery(docq) + 1;
				break;
			}catch (Exception e) {
				System.out.println("Error creating doctor_ID.");
				continue;
			}
		}while(true);

		//Get name
		do {
			System.out.print("Please enter the doctor's name: ");
			try {
				name = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);

		//Get specialty
		do {
			System.out.print("Please enter the doctor's specialty: ");
			try {
				specialty = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);

		//Get did
		do {
			System.out.print("Please enter the name of hospital department the doctor works in: ");
			try {
				hospital = in.readLine();
				departq = "SELECT * FROM Department;";
				did = esql.executeQuery(departq) + 1;
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);

		try {
			query = "INSERT INTO Patient (doctor_ID, name, specialty, did) VALUES ";
			query += String.format("('%2d', '%s', '%s', '%2d)", sid, name, specialty, did);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println("Insert Doctor Query Failed");
		}
	}

	public static void AddPatient(DBproject esql) {//2
		int id;
		String name;
		String gender;
		int age;
		String address;
		String query;

		// Get patient id
		do {
			System.out.print("Please enter the patient id: ");
			try {
				id = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input must be an integer.");
				continue;
			}
		}while (true);

		// Get patient name
		do {
			System.out.print("Please enter the patient name: ");
			try {
				name = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while (true);

		// Get Gender
		do {
			System.out.print("Please enter the patient's gender: ");
			try {
				gender = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a character.");
				continue;
			}
		}while (true);

		// Get Age
		do {
			System.out.print("Please enter the patient's age: ");
			try {
				age = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input must be an integer.");
				continue;
			}
		}while (true);
		// Get Address
		do {
			System.out.print("Please enter the patient's address: ");
			try {
				address = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);

		try {
			query = "INSERT INTO Patient (patient_ID, name, gtype, age, address) VALUES ";
			query += String.format("('%2d', '%s', '%s', '%2d', '%s')", id, name, gender, age, address);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println("Insert Patient Query Failed");
		}
	}

	public static void AddAppointment(DBproject esql) {//3
		int aid;
		String date;
		String time;
		Sttring stat;
		String appq;
		String query;

		//get appointment ID
		do {
			try {
				appq = "SELECT * FROM Appointment;"
				aid = esql.executeQuery(appq) + 1;
				break;
			}catch (Exception e) {
				System.out.println("Error creating appnt_ID.");
				continue;
			}
		} while(true);

		// Get date
		do {
			System.out.print("Please enter the date of appointment: ");
			try {
				date = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while (true);

		// Get time slot
		do {
			System.out.print("Please enter the time slot: ");
			try {
				time = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while (true);

		//get status
		do {
			try {
				stat = 'AC';
				break;
			}catch (Exception e) {
				System.out.println("Error creating appointment status.");
				continue;
			}
		}while(true);

		try {
			query = "INSERT INTO Appointment (appnt_ID, adate, time_slot, status) VALUES ";
			query += String.format("('%2d', '%s', '%s', '%2d)", aid, date, time, stat);
			esql.executeUpdate(query);
		}catch(Exception e) {
			System.out.println("Insert Appointment Query Failed");
		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		int patientID;
		int doctorID;
		int appointmentID;
		String query;

		// Get patient id
		do {
			try {
				System.out.print("Please enter the patient ID: ");
				patientID = Integer.parseInt(in.readLine());

				query = "SELECT * FROM Patient WHERE patient_ID = " + patientID;
				int rows = esql.executeQuery(query);
				if (rows != 1) {
					System.out.println("That patient does not exist!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Input is invalid. " + e.getMessage());
				continue;
			}
		}while(true);

		// get doctor id
		do {
			try {
				System.out.print("Please enter the doctor ID: ");
				doctorID = Integer.parseInt(in.readLine());

				query = "SELECT * FROM Doctor WHERE doctor_ID = " + doctorID;
				int rows = esql.executeQuery(query);
				if (rows != 1) {
					System.out.println("That doctor does not exist!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Invalid input. " + e.getMessage());
				continue;
			}
		}while(true);

		// get appointment id AND check if it corresponds to the doctor AND check if it is available
		do {
			try {
				System.out.print("Please enter the appointment ID: " );
				appointmentID = Integer.parseInt(in.readLine());

				// Check if appointment exists in has_appointment
				query = "SELECT * FROM has_appointment WHERE appt_ID = " + appointmentID;
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if (result.isEmpty()) {
					System.out.println("The appointment number does not exist!");
					continue;
				}
				List<String> record = result.get(0);

				// Check if Doctor ID matches for given appointment
				if (doctorID != Integer.parseInt(record.get(1))) {
					System.out.println("The appointment does not correspond to the doctor!");
					continue;
				}

				// Check if appointment is available
				query = "SELECT * FROM Appointment WHERE appnt_ID = " + appointmentID;
				result = esql.executeQueryAndReturnResult(query);
				record = result.get(0);

				if (!record.get(3).equals("AV")) {
					System.out.println("Appointment is not available!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Invalid input." + e.getMessage());
			}
		}while(true);

		// execute updaete here
		System.out.println("Success!");

	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		int docID;
		String date1;
		String date2;
		String query;

		//Get doctor id
		do {
			try {
				System.out.print("Please enter doctor ID: ");
				docID = Integer.parseInt(in.readLine());

				query = "SELECT * FROM Doctor WHERE doctor_ID = " + docID;
				int rows = esql.executeQuery(query);
				if (rows != 1) {
					System.out.println("That doctor doesn't exist!");
					continue;
				}
				break;
			}catch(Exception e) {
				System.out.println("Input is invalid. " + e.getMessage());
				continue;
			}
		}while(true);

		//Get first date
		do {
			try {
				System.out.print("Please enter first date: ");
				date1 = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}

		//Get second date
		do {
			try {
				System.out.print("Please enter second date: ");
				date2 = in.readLine();
				break;
			}catch (Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);

		//Get all active and available appointments
		do {
			try{
				query = "SELECT * FROM has_appointment H, Doctor D, Appointment A WHERE h.doctor_ID = " + docID;
				query += "AND h.appt_id = A.appnt_ID AND A.status = 'AC' OR 'AV' AND A.adate BETWEEN ";
				query = query + date1 + " AND " + date2;
				List<List<String>> result = esql.executeQueryAndReturnResult(query);
				if (result.isEmpty()) {
					System.out.println("No available or active appointments");
					continue;
				}
			}catch(Exception e) {
				System.out.println("Your input must be a string.");
				continue;
			}
		}while(true);
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
	}


	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
	}
}
