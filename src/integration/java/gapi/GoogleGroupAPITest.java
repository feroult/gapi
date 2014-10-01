package gapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gapi.utils.Setup;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;

public class GoogleGroupAPITest {
	private static final String EMAIL = "group@dextra-sw.com";
	private GoogleGroupAPI googleGroup;

	public GoogleGroupAPITest() throws GeneralSecurityException, IOException, URISyntaxException {
		List<String> serviceAccountScopes = new ArrayList<String>();
		serviceAccountScopes.add(DirectoryScopes.ADMIN_DIRECTORY_GROUP);
		
		googleGroup = new GoogleAPI(serviceAccountScopes).group();
	}
	
	@Before 
	@After
	public void setup() throws IOException {
		try {
			googleGroup.delete(EMAIL);
		} catch (GoogleJsonResponseException e) {
		}
	}

	@Test
	public void testDeleteGroup() throws IOException {
		String name = "group";
		String description = "group 1";
	    Group groupNew = createGroup(name, EMAIL, description);
        assertEquals(name, groupNew.getName());
        googleGroup.delete(EMAIL);
        
        try {
        	googleGroup.getGroup(EMAIL);
		} catch (GoogleJsonResponseException e) {
			assertTrue(true);
		}
	}
	
	@Test
	public void testCreateGroup() throws IOException {
        String name = "group";
		String description = "group 1";
		Group groupNew = createGroup(name, EMAIL, description);
        
        assertEquals(name, groupNew.getName());
        assertEquals(EMAIL, groupNew.getEmail());
	}
	
	@Test
	public void testUpdateGroup() throws IOException {
		Group group = createGroup("group", EMAIL, "group 1");
		group.setDescription("group 2 description");
		Group groupUpdate = googleGroup.update(group.getEmail(), group);
		
        assertEquals(group.getName(), groupUpdate.getName());
        assertEquals(group.getEmail(), groupUpdate.getEmail());
        assertEquals(group.getId(), groupUpdate.getId());
	}
	
	@Test
	public void testGetGroup() throws IOException {
		Group group = createGroup("group", EMAIL, "group 1");
        
        Group groupNew = googleGroup.getGroup(group.getEmail());
        assertTrue(groupNew != null);
        assertEquals(group.getName(), groupNew.getName());
	}
	
	@Test
	public void testGetGroups() throws IOException {
		createGroup("group", EMAIL, "group 1");
		Groups groups = googleGroup.getGroups();
		List<Group> results = groups.getGroups();
		assertTrue(results.size() > 0);
	}
	
	@Test
	public void testCreateGroupAndManagerMembers() throws IOException {
		String description = "group 1";
		String name = "group";
		Group group = createGroup(name, EMAIL, description);
		
		Member member = new Member();
		member.setEmail(Setup.getServiceAccountUser());
		member.setRole("MEMBER");
		
		googleGroup.addMemberGroup(group, member);
		Member memberGroup = googleGroup.getMemberGroup(group, member.getEmail());
		assertEquals(member.getEmail(), memberGroup.getEmail());
		assertEquals(member.getRole(), memberGroup.getRole());
		
		googleGroup.deleteMemberGroup(group, member.getEmail());
		
		try {
			googleGroup.getMemberGroup(group, member.getEmail());
		} catch (GoogleJsonResponseException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testCreateGroupAndListMembers() throws IOException {
		String description = "group 1";
		String name = "group";
		Group group = createGroup(name, EMAIL, description);
		
		Member member = new Member();
		member.setEmail(Setup.getServiceAccountUser());
		member.setRole("MEMBER");
		
		googleGroup.addMemberGroup(group, member);
		Members members = googleGroup.getMembersGroup(group);
		
		assertTrue(!members.isEmpty());
	}
	
	@Test
	public void testCreateGroupAndAddExternalMember() throws IOException {
		String description = "group 1";
		String name = "group";
		Group group = createGroup(name, EMAIL, description);
		
		Member member = new Member();
		member.setEmail("rsilvamagalhaes@gmail.com");
		member.setRole("MEMBER");

		googleGroup.addMemberGroup(group, member);
		Members members = googleGroup.getMembersGroup(group);
		
		List<Member> ms = members.getMembers();
		assertEquals("rsilvamagalhaes@gmail.com", ms.get(0).getEmail());
	}
	
	private Group createGroup(String name, String email, String description) throws IOException {
		Group group = createObjectGroup(name, email, description);
        return googleGroup.create(group);
	}
	
	private Group createObjectGroup(String name, String email, String description) {
		Group group = new Group();
		group.setName(name);
		group.setEmail(email);
		group.setDescription(description);
		return group;
	}
}
