package com.baloise.egitblit.common;

import java.util.List;

/**
 * Represents one Config Entry (one GitBlit Repo entry)
 * @author MicBag
 *
 */
public class GitBlitServer{
	public boolean active;
	public String url;
	public String urlSeparator;
	public String user;
	public String password;
	
	public final static String DEF_URL_SEPARATOR = "/";

	private List<GitBlitRepository> projectList;
	
	public GitBlitServer(String url, String urlSep, boolean active, String user, String pwd){
		this.url = url;
		this.user = user;
		this.password = pwd;
		this.active = active;
		this.urlSeparator = urlSep;
	}
	
	
	public GitBlitServer(){
		this(null,null,true,null,null);
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