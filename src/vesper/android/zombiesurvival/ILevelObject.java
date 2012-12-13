package vesper.android.zombiesurvival;

public interface ILevelObject<T> {
	
	/**
	 * @return a string representing the loadable object type
	 */
	public String getXMLType();

	/**
	 * Generate the XML necessary to represent this object on load
	 * @return one line of XML to save to file
	 */
	public String saveToXML();
	
	/**
	 * Create an object from XML
	 * @param xml string to create object from
	 * @return this object with correct state
	 */
	public T loadFromXML(String xml);
	
	/**
	 * Set the objects behavior for level edit mode
	 * @param editMode true for level edit mode, false for game mode
	 */
	public void setLevelEditMode(boolean editMode);
	
}
