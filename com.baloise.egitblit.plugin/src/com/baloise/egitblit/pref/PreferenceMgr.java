package com.baloise.egitblit.pref;

import java.util.List;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;

/**
 * Handling preference settings
 * 
 * @author MicBag
 * 
 */
public class PreferenceMgr{

	// --- Root node
	public final static String KEY_GITBLIT_ROOT = "com.baloise.gitblit";

	// --- global settings under root node
	public final static String KEY_GITBLIT_DCLICK = "com.baloise.gitblit.dobuleclick";
	public final static String KEY_GITBLIT_OMIT_SERVER_ERROR = "com.baloise.gitblit.general.omitServerInError";
	public final static String KEY_GITBLIT_OMIT_COLOR_COLUMS = "com.baloise.gitblit.general.viewer.coloring";
	public final static String KEY_GITBLIT_SHOW_GROUPS = "com.baloise.gitblit.general.viewer.showGroups";
	

	// Node containing the list of servers
	public final static String KEY_GITBLIT_SERVER_NODE = "com.baloise.gitblit.server";
	// --- Server entry
	public final static String KEY_GITBLIT_SERVER_URL = "com.baloise.gitblit.server.url";
	public final static String KEY_GITBLIT_SERVER_ACTIVE = "com.baloise.gitblit.server.active";
	// public final static String KEY_GITBLIT_SERVER_URL_SEP =
	// "com.baloise.gitblit.server.url.separator";
	public final static String KEY_GITBLIT_SERVER_USER = "com.baloise.gitblit.server.user";
	public final static String KEY_GITBLIT_SERVER_PASSWORD = "com.baloise.gitblit.server.password";

	public final static String VALUE_GITBLIT_URL_SEPERATOR = "/";

	/**
	 * Reads the configuration, scope INSTANCE
	 * 
	 * @return ConfigModel
	 * @throws GitBlitExplorerException
	 */
	public static PreferenceModel readConfig() throws GitBlitExplorerException{
		PreferenceModel prefModel = new PreferenceModel();

		try{
			ISecurePreferences pref = SecurePreferencesFactory.getDefault().node(KEY_GITBLIT_ROOT);

			if(pref == null){
				final String msg = "Can�t access preferences for Gitblit explorer. Continue with new (empty) settings.";
				Activator.logError(msg);
				return prefModel;
			}

			// --- Read global settings
			int val = pref.getInt(KEY_GITBLIT_DCLICK, PreferenceModel.DoubleClickBehaviour.OpenGitBlit.value);
			prefModel.setDoubleClick(PreferenceModel.DoubleClickBehaviour.getValue(val));

			boolean bval = pref.getBoolean(KEY_GITBLIT_OMIT_SERVER_ERROR, false);
			prefModel.setOmitServerErrors(bval);
			
			bval = pref.getBoolean(KEY_GITBLIT_OMIT_COLOR_COLUMS, false);
			prefModel.setColorColumns(bval);

			bval = pref.getBoolean(KEY_GITBLIT_SHOW_GROUPS, true);
			prefModel.setShowGroups(bval);
			
			// --- Column settings
			ISecurePreferences entryNode;
			ISecurePreferences serverNode;
			
			// ---- Read list of servers
			// Root node
			serverNode = pref.node(KEY_GITBLIT_SERVER_NODE);
			if(serverNode != null){
				// An entry
				String url, user, pwd;
				boolean active;
	
				String[] names = serverNode.childrenNames();
				for(String item : names){
					entryNode = serverNode.node(item);
					url = entryNode.get(KEY_GITBLIT_SERVER_URL, null);
					active = entryNode.getBoolean(KEY_GITBLIT_SERVER_ACTIVE, true);
					user = entryNode.get(KEY_GITBLIT_SERVER_USER, null);
					pwd = entryNode.get(KEY_GITBLIT_SERVER_PASSWORD, null);
					prefModel.addRepository(url, active, user, pwd);
				}
			}
			else{
				Activator.logError("Error reading preferences. Missing configuration node of servers");	
			}
		}catch(Exception e){
			Activator.logError("Error reading preferences. Continue with configuration settings which have been read so far.", e);
		}
		return prefModel;
	}

	public static void saveConfig(PreferenceModel prefModel) throws GitBlitExplorerException{
		if(prefModel == null){
			final String msg = "Error saving Gitblit Explorer settings: No preference model avail (internal error). Configuration will not be saved.";
			Activator.logError(msg);
			throw new GitBlitExplorerException(msg);
		}
		ISecurePreferences pref = SecurePreferencesFactory.getDefault().node(KEY_GITBLIT_ROOT);

		if(pref == null){
			final String msg = "Can�t access preferences for Gitblit Explorer. Confriguration will not be saved.";
			Activator.logError(msg);
			throw new GitBlitExplorerException(msg);
		}

		try{
			// --- Saving global settings
			pref.putInt(KEY_GITBLIT_DCLICK, prefModel.getDoubleClick().value, false);
			pref.putBoolean(KEY_GITBLIT_OMIT_SERVER_ERROR, prefModel.isOmitServerErrors(),false);
			pref.putBoolean(KEY_GITBLIT_OMIT_COLOR_COLUMS, prefModel.isColorColumns(), false);
			pref.putBoolean(KEY_GITBLIT_SHOW_GROUPS, prefModel.isShowGroups(), false);

			ISecurePreferences entryNode;
			ISecurePreferences serverNode;
			
			// --- Write new settings
			int count = 0;
			serverNode = pref.node(KEY_GITBLIT_SERVER_NODE);
			serverNode.removeNode();
			pref.flush();
			serverNode = pref.node(KEY_GITBLIT_SERVER_NODE);
			
			for(GitBlitServer item : prefModel.getServerList()){
				entryNode = serverNode.node(KEY_GITBLIT_SERVER_URL + "_" + count++);
				entryNode.put(KEY_GITBLIT_SERVER_URL, trim(item.url), false);
				entryNode.putBoolean(KEY_GITBLIT_SERVER_ACTIVE, item.active, false);
				entryNode.put(KEY_GITBLIT_SERVER_USER, trim(item.user), false);
				entryNode.put(KEY_GITBLIT_SERVER_PASSWORD, item.password, true);
			}
			pref.flush();
		}catch(Exception e){
			Activator.logError("Error saving Gitblit preference settings: Can�t remove older server settings.", e);
		}
	}

	private final static String trim(String value){
		if(value == null){
			return null;
		}
		return value.trim();
	}

}
