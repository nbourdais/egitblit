package com.baloise.egitblit.view.model;

import java.util.Date;
import java.util.List;

import com.baloise.egitblit.gitblit.GitBlitRepository;

/**
 * ViewModel wrapping GitBlitProject (Git Repository entry)
 * @author MicBag
 *
 */
public class ProjectViewModel implements GitBlitViewModel{

	private GitBlitRepository entry;
	private GitBlitViewModel parent;
	private String toolTip;
	
	public ProjectViewModel(GitBlitRepository entry, GitBlitViewModel parent){
		this.entry = entry;
		setParent(parent);
	}
	
	public void setParent(GitBlitViewModel parent){
		this.parent = parent;
	}
	
	public GitBlitViewModel getParent(){
		return this.parent;
	}
	

	public ProjectViewModel(GitBlitRepository entry){
		this.entry = entry;
	}
	
	public String getGitURL(){
		if(this.entry == null){
			return null;
		}
		return this.entry.gitUrl;
	}
	
	public String getServerURL(){
		if(this.entry == null){
			return null;
		}
		return this.entry.serverUrl;
		
	}

	public String getProjectName(){
		if(this.entry == null){
			return null;
		}
		return this.entry.projectName;
	}
	
	public String getGroupName(){
		if(this.entry == null){
			return null;
		}
		return this.entry.groupName;
	}

	public boolean hasCommits(){
		if(this.entry == null){
			return false;
		}
		return this.entry.hasCommits;
	}
	
	@Override
	public String getName(){
		return getProjectName();
	}

	public String getDescription(){
		if(this.entry == null){
			return null;
		}
		return this.entry.description;
	}
	
	public List<String> getOwners(){
		if(this.entry == null){
			return null;
		}
		return this.entry.owners;
	}

	public Date getLastChange(){
		if(this.entry == null){
			return null;
		}
		return this.entry.lastChange;
	}
	
	public Boolean isFrozen(){
		if(this.entry == null){
			return null;
		}
		return this.entry.isFrozen;
	}
	
	public Boolean isFederated(){
		if(this.entry == null){
			return null;
		}
		return this.entry.isFederated;
	}

	public Boolean isBare(){
		if(this.entry == null){
			return null;
		}
		return this.entry.isBare;
	}

	public String getFrequency(){
		if(this.entry == null){
			return null;
		}
		return this.entry.frequency;
	}

	public String getOriginRepository(){
		if(this.entry == null){
			return null;
		}
		return this.entry.originRepository;
	}
	
	public String getSize(){
		if(this.entry == null){
			return null;
		}
		return this.entry.size;
	}

	public void setToolTip(String msg){
		this.toolTip = msg;
	}
	
	public String getToolTip(){
		return this.toolTip;
	}
	
	public String getColor(){
		if(this.entry == null){
			return null;
		}
		return this.entry.repoRGB;
	}
			
}