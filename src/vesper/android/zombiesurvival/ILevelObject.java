package vesper.android.zombiesurvival;

public interface ILevelObject {
	
	/**
	 * @return a string representing the level object type
	 */
	public String getLevelType();

	/**
	 * Generate the XML necessary to load this object
	 * @return XML representing object state
	 */
	public String getLevelXML();
	
	/**
	 * Set the objects behavior for level edit mode
	 * @param editMode true for level edit mode, false for game mode
	 */
	public void setLevelEditMode(boolean editMode);
	
	/**
	 * Called when Level Edit mode is enabled
	 * Implementor should ensure objects behavior changes appropriately
	 */
	public void onEnableLevelEditMode();
	
	/**
	 * Called when Level Edit mode is disabled
	 * Implementor should ensure objects behavior changes appropriately
	 */
	public void onDisableLevelEditMode();
	
}
