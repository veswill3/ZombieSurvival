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
