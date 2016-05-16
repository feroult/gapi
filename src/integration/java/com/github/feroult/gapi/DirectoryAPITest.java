package com.github.feroult.gapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.feroult.gapi.utils.Setup;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;

public class DirectoryAPITest {

	private static final String EMAIL = "group@6spot.com.br";

	private DirectoryAPI directory;

	public DirectoryAPITest() throws GeneralSecurityException, IOException,
			URISyntaxException {
		directory = new GoogleAPI().directory();
	}

	@Before
	@After
	public void setup() throws IOException {
		directory.delete(EMAIL);
	}

	@Test
	public void testDeleteGroup() throws IOException {
		createGroup("group", EMAIL, "group 1");
		assertEquals("group", directory.getGroup(EMAIL).getName());

		directory.delete(EMAIL);
		assertNull(directory.getGroup(EMAIL));
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
		Group groupUpdate = directory.update(group.getEmail(), group);

		assertEquals(group.getName(), groupUpdate.getName());
		assertEquals(group.getEmail(), groupUpdate.getEmail());
		assertEquals(group.getId(), groupUpdate.getId());
	}

	@Test
	public void testGetGroup() throws IOException {
		Group group = createGroup("group", EMAIL, "group 1");

		Group groupNew = directory.getGroup(group.getEmail());
		assertTrue(groupNew != null);
		assertEquals(group.getName(), groupNew.getName());
	}

	@Test
	public void testGetGroups() throws IOException {
		createGroup("group", EMAIL, "group 1");
		Groups groups = directory.getGroupsByCustomer("my_customer");
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

		directory.addMemberGroup(group, member);
		Member memberGroup = directory.getMemberGroup(group, member.getEmail());
		assertEquals(member.getEmail(), memberGroup.getEmail());
		assertEquals(member.getRole(), memberGroup.getRole());

		directory.deleteMemberGroup(group, member.getEmail());
		directory.getMemberGroup(group, member.getEmail());
	}

	@Test
	public void testCreateGroupAndListMembers() throws IOException {
		String description = "group 1";
		String name = "group";
		Group group = createGroup(name, EMAIL, description);

		Member member = new Member();
		member.setEmail(Setup.getServiceAccountUser());
		member.setRole("MEMBER");

		directory.addMemberGroup(group, member);
		Members members = directory.getMembersGroup(group);

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

		directory.addMemberGroup(group, member);
		Members members = directory.getMembersGroup(group);

		List<Member> ms = members.getMembers();
		assertEquals("rsilvamagalhaes@gmail.com", ms.get(0).getEmail());
	}

	private Group createGroup(String name, String email, String description)
			throws IOException {
		Group group = createObjectGroup(name, email, description);
		return directory.create(group);
	}

	private Group createObjectGroup(String name, String email,
			String description) {
		Group group = new Group();
		group.setName(name);
		group.setEmail(email);
		group.setDescription(description);
		return group;
	}
}
