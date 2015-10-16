package table.codeOrganizer;

import java.time.LocalDate;

import javafx.beans.property.StringProperty;

public class Reference
{
	private StringProperty name;
	private RefType type;
	private LocalDate added;
	private LocalDate modified;
	
	private StringProperty address;
	private StringProperty other;
	
	
}


enum RefType
{
	UNKNOWN,
	STRING,
	URL,
	DBRECORD,
	USERID,
	FILEID,
	WELLID
};