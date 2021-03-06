package com.baloise.egitblit.view.action;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.internal.util.ProjectUtil;
import org.eclipse.egit.core.op.CloneOperation;
import org.eclipse.egit.core.op.CloneOperation.PostCloneTask;
import org.eclipse.egit.ui.JobFamilies;
import org.eclipse.egit.ui.internal.clone.ProjectRecord;
import org.eclipse.egit.ui.internal.clone.ProjectUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

import com.baloise.egitblit.common.GitBlitExplorerException;
import com.baloise.egitblit.gitblit.GitBlitRepository;
import com.baloise.egitblit.gitblit.GitBlitServer;
import com.baloise.egitblit.main.Activator;
import com.baloise.egitblit.pref.PreferenceMgr;
import com.baloise.egitblit.pref.PreferenceModel;
import com.baloise.egitblit.view.model.GitBlitViewModel;
import com.baloise.egitblit.view.model.ProjectViewModel;
import com.baloise.egitblit.view.wizard.GitCreateStandardProjectWizard;

/**
 * @see Action
 * @author culmat
 * 
 */
public class CloneOneClickAction extends CloneAction{
	public final static String ID = "com.baloise.egitblit.cmd.import";
	public boolean mkPrefix = true;
	public boolean mkPostfix = true;

	public CloneOneClickAction(Viewer viewer){
		super(viewer);
		setText("Clone && Import Project");
		setActionDefinitionId(ID);
		setImageDescriptorFromURL("platform:/plugin/" + Activator.PLUGIN_ID + "/icons/cloneGitOneClick.gif");
	}

	@Override
	public void doRun(){
		try{
			GitBlitViewModel model = getSelectedModel();
			if(model instanceof ProjectViewModel){
				ProjectViewModel project = (ProjectViewModel)model;
				performClone(project);
			}
		}
		catch(Exception e){
			Activator.logError(e.getMessage(), e);
		}
	}

	@SuppressWarnings("restriction")
	protected void performClone(final ProjectViewModel project) throws URISyntaxException, GitBlitExplorerException{
		//URIish uri = new URIish(project.getGitUrl());

	  CopyAction cca = new CopyAction(getViewer());
    cca.setPrefModel(getPrefModel());
    String copyURL = cca.makeCopyUrl(project);
	  URIish uri = new URIish(copyURL);
		
		final Collection<Ref> selectedBranches;
		selectedBranches = Collections.emptyList();
		final File workdir = getWorkdir(project);
		final String remoteName = "origin";

		int remote_connection_timeout = Platform.getPreferencesService().getInt("org.eclipse.egit.ui", "remote_connection_timeout", 30, null);

		final CloneOperation op = new CloneOperation(uri, true, selectedBranches, workdir, "refs/heads/master", remoteName, remote_connection_timeout);
		UsernamePasswordCredentialsProvider credentialsProvider = getUsernamePasswordCredentialsProvider(project.getServerUrl());
		op.setCredentialsProvider(credentialsProvider);

		op.addPostCloneTask(new PostCloneTask() {
			public void execute(Repository repository, IProgressMonitor monitor) throws CoreException{
				org.eclipse.egit.core.Activator.getDefault().getRepositoryUtil().addConfiguredRepository(new File(workdir, ".git"));
				if(project.hasCommits()){
					String gname = project.getGroupName();
					String pname = project.getProjectName();
					if(gname != null){
						// We have a group name
						if(!GitBlitRepository.GROUP_MAIN.equalsIgnoreCase(gname)){
							// Hide main group, add other group names by default
							// Here: Group name is not main. Therefore add group name by default
							PreferenceModel prefModel = getPrefModel();							
							if(prefModel == null || prefModel.isWSGroupNameEnabled()){
								pname = gname + "/" + pname;
							}
						}
					}
					final IWorkingSet[] sets = new IWorkingSet[] {createWorkingSet(pname)};
					importProjects(repository, sets);
				}
				else{
					final GitCreateStandardProjectWizard wiz = new GitCreateStandardProjectWizard(repository, workdir.getAbsolutePath());
					Display.getDefault().syncExec(new Runnable() {
						public void run(){
							Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
							WizardDialog wizardDialog = new WizardDialog(shell, wiz);
							wizardDialog.open();
						}
					});
				}
			}
		});
		runAsJob(op);
	}

	private UsernamePasswordCredentialsProvider getUsernamePasswordCredentialsProvider(String serverURL) throws GitBlitExplorerException{
		List<GitBlitServer> serverList = PreferenceMgr.readConfig().getServerList();
		for(GitBlitServer server: serverList){
			if(serverURL.equalsIgnoreCase(server.url)){
				return new UsernamePasswordCredentialsProvider(server.user, server.password);
			}
		}
		throw new GitBlitExplorerException("No server config found for URL " + serverURL);
	}

	private void importProjects(final Repository repository, final IWorkingSet[] sets){
		Job importJob = new Job("Importing") {

			@SuppressWarnings("restriction")
			protected IStatus run(IProgressMonitor monitor){
				List<File> files = new ArrayList<File>();
				ProjectUtil.findProjectFiles(files, repository.getWorkTree(), true, monitor);
				if(files.isEmpty()){
				  return Status.OK_STATUS;
				}

				Set<ProjectRecord> records = new LinkedHashSet<ProjectRecord>();
				for(File file: files){
				  records.add(new ProjectRecord(file));
				}
				
				try{
					//ProjectUtils.createProjects(records, false, repository, sets, monitor);
				  ProjectUtils.createProjects(records, sets, monitor);
				}
				catch(Exception e){
					Activator.logError(e.getLocalizedMessage(), e);
					return Status.CANCEL_STATUS; 
				}
				return Status.OK_STATUS;
			}
		};
		importJob.schedule();
	}

	private IWorkingSet createWorkingSet(String name){
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet ws = workingSetManager.getWorkingSet(name);
		if(ws == null){
			ws = workingSetManager.createWorkingSet(name, new IAdaptable[0]);
			ws.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
			workingSetManager.addWorkingSet(ws);
		}
		return ws;
	}

	@SuppressWarnings("restriction")
	private void runAsJob(final CloneOperation op){
		final Job job = new Job("Cloning") {
			@Override
			protected IStatus run(final IProgressMonitor monitor){
				try{
					op.run(monitor);
					return Status.OK_STATUS;
				}
				catch(InterruptedException e){
					return Status.CANCEL_STATUS;
				}
				catch(InvocationTargetException e){
					Throwable thr = e.getCause();
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, thr.getMessage(), thr);
				}
			}

			@Override
			public boolean belongsTo(Object family){
				if(family.equals(JobFamilies.CLONE)) return true;
				return super.belongsTo(family);
			}
		};
		job.setUser(true);
		job.schedule();
	}

	private File getWorkdir(ProjectViewModel project){
		String default_repository_dir = Platform.getPreferencesService().getString("org.eclipse.egit.ui", "default_repository_dir", System.getProperty("user.home"), null);
		File ret = new File(default_repository_dir);
		if(!GitBlitRepository.GROUP_MAIN.equals(project.getGroupName())){
			ret = new File(ret, project.getGroupName());
		}
		return new File(ret, project.getProjectName());
	}

	@Override
	public boolean isEnabled(){
		if(getEGitCommand() == null) return false;

		GitBlitViewModel model = getSelectedModel();
		if(model instanceof ProjectViewModel){
			ProjectViewModel project = (ProjectViewModel)model;
			File workdir = getWorkdir(project);
			if(!workdir.exists() || workdir.list().length == 0) return true;
		}
		return false;
	}

}