package com.baloise.egitblit.gitblit;

import java.net.URL;
import java.util.List;

import com.baloise.egitblit.pref.CloneProtocol;
import com.baloise.egitblit.pref.CloneSettings;

/**
 * Represents one Config Entry (one GitBlit Repo entry)
 * 
 * @author MicBag
 *
 */
public class GitBlitServer{
	// Static fields
    public static final String GITBLIT_URL_PREFIX = "gitblit/r/";

	// Server settings
	public String url;
	public String user;
	public String password;
	public CloneSettings cloneSettings;

	// Status / Confiig fields
	public boolean active = true;
	public boolean serverError = false;

	private List<GitBlitRepository> projectList;

	public GitBlitServer(String url, boolean active, String user, String pwd, CloneSettings cls){
		this.url = url;
		this.user = user;
		this.password = pwd;
		this.active = active;
		this.cloneSettings = cls;
	}

	public GitBlitServer(String url, String user, String pwd, CloneSettings cls){
		this(url, true, user, pwd, cls);
	}

	public GitBlitServer(String url, boolean active, String user, String pwd){
		this(url, active, user, pwd, new CloneSettings());
	}

	public GitBlitServer(){
		this(null, true, null, null);
	}

	public CloneSettings getCloneSettings(){
		return this.cloneSettings;
	}

	public CloneSettings setCloneSettings(CloneProtocol prot, Integer port){
		CloneSettings set = new CloneSettings(prot, port);
		setCloneSettings(set);
		return set;
	}

	public void setCloneSettings(CloneSettings set){
		this.cloneSettings = set;
	}
	

	public String getCloneURL(String gburl){
		CloneProtocol cp = cloneSettings.getCloneProtocol();
		if(cp == null){
			return null;
		}

		try{
			URL url = new URL(gburl);
			String  curl  = cp.schema + "://";
			String  chost = url.getHost();
			String  cpath = url.getFile();
			Integer cport = this.cloneSettings.getPort();
			String  cuser = this.user;
			String  cpwd  = this.password;
			
			// --- File protocol ----------------------------------------------
			if(cp == CloneProtocol.File){
				// No host
				chost = chost == null ? "" : chost;
				cport = null;   // no port
				cuser = null;	// No user/pwd
				curl += "/";	
			}

			// --- Git + SSH --------------------------------------------------
			if(cp == CloneProtocol.GIT || cp == CloneProtocol.SSH){
				// No ctx root / path
				int pos = url.getPath().indexOf(GITBLIT_URL_PREFIX);
				cpath = url.getFile().substring(pos + GITBLIT_URL_PREFIX.length());
			}

			if(cp != CloneProtocol.SSH){
				// Currently, user will be added for ssh only
				cuser = null;
			}
			
			// --- Assembling -------------------------------------------------
			if(cuser != null){
				curl += cuser;
				curl += "@";
				// Do not add password because of clipboard
			}

			curl += chost;
			
			if(cport != null && cport != CloneSettings.NO_PORT){
				curl += ":" + cport;
			}
			if(cpath.startsWith("/") == false){
				curl += "/";
			}
			
			curl += cpath;
			return curl;
		} catch(Exception e){
		}
		return null;
	}

	public void addProject(GitBlitRepository proj){
		this.projectList.add(proj);
	}

	public List<GitBlitRepository> getProjects(){
		return this.projectList;
	}

	public boolean removeProject(GitBlitRepository proj){
		return this.projectList.remove(proj);
	}

	public void clearProjects(){
		this.projectList.clear();
	}

	@Override
	public int hashCode(){
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		GitBlitServer other = (GitBlitServer) obj;
		if(url == null){
			if(other.url != null)
				return false;
		}else if(!url.equals(other.url))
			return false;
		return true;
	}
}