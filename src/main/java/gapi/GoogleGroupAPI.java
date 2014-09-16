package gapi;

import java.io.IOException;

import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;

public class GoogleGroupAPI {
	private Directory directory;
	
	public GoogleGroupAPI(Directory directory) {
		this.directory = directory;
	}

	public Group create(Group group) throws IOException{
		return directory.groups().insert(group).execute();
	}
	
	public Group update(String groupKey, Group group) throws IOException {
		return directory.groups().update(groupKey, group).execute();
	}
	
	public void delete(String groupKey) throws IOException {
		directory.groups().delete(groupKey).execute();
	}
	
	public Group getGroup(String groupKey) throws IOException {
		return directory.groups().get(groupKey).execute();
	}
	
	public Groups getGroups() throws IOException {
		return directory.groups().list().setCustomer("my_customer").execute();
	}
	
	public void addMemberGroup(Group group, Member member) throws IOException {
		directory.members().insert(group.getEmail(), member).execute();
	}
	
	public void deleteMemberGroup(Group group, String membersEmail) throws IOException {
		directory.members().delete(group.getEmail(), membersEmail).execute();
	}
	
	public Member getMemberGroup(Group group, String membersEmail) throws IOException {
		return directory.members().get(group.getEmail(), membersEmail).execute();
	}
	
	public Members getMembersGroup(Group group) throws IOException {
		return directory.members().list(group.getEmail()).execute(); 
	}
}
