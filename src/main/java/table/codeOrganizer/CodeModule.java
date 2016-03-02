package table.codeOrganizer;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class CodeModule
{
	StringProperty name;
	StringProperty lastTab;
	StringProperty comments;
	StringProperty description;
	ReferenceList refs;
	Priority priority;
	Status status;
	ObjectProperty<LocalDate>  modified;
	ObjectProperty<LocalDate>  created;
	ObjectProperty<Image>  screenshot;

	// @Formatter:off
	public String getLastTab()							{ 	return lastTab.getValue();	}
	public void setLastTab(String s)					{ 	lastTab.setValue(s);	}
	public StringProperty getLastTabProperty()			{ 	return lastTab;	}

	public String getName()								{ 	return name.getValue();	}
	public void setName(String s)						{ 	name.setValue(s);	}
	public StringProperty getNameProperty()				{ 	return name;	}

	public String getComments()							{ 	return comments.getValue();	}
	public void setComments(String s)					{ 	comments.setValue(s);	}
	public StringProperty getCommentsProperty()			{ 	return comments;	}

	public String getDescription()						{ 	return description.getValue();	}
	public void setDescription(String s)				{ 	description.setValue(s);	}
	public StringProperty getDescriptionProperty()		{ 	return description;	}

	public LocalDate getModified()						{ 	return modified.getValue();	}
	public void setModified(LocalDate s)				{ 	modified.setValue(s);	}
	public ObjectProperty<LocalDate> getModifiedProperty(){ 	return modified;	}

	public LocalDate getCreated()						{ 	return created.getValue();	}
	public void setCreated(LocalDate s)					{ 	created.setValue(s);	}
	public ObjectProperty<LocalDate> getCreatedProperty(){ 	return created;	}

	// @Formatter:on

}


enum Priority
{
	UNSET,
	LOW,
	NORMAL,
	HIGH,
	CRITICAL
};


enum Status
{
	UNKNOWN,
	PLANNED,
	ACTIVE,
	COMPLETE,
	SUSPENDED,
	DORMANT,
	ABORTED
};


enum Metric
{
	MM,
	CM,
	IN,
	PX,
	PT
};

