package com.github.feroult.gapi;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.Directory.Groups.Delete;
import com.google.api.services.admin.directory.Directory.Groups.Get;
import com.google.api.services.admin.directory.Directory.Groups.Insert;
import com.google.api.services.admin.directory.Directory.Groups.List;
import com.google.api.services.admin.directory.Directory.Groups.Update;
import com.google.api.services.admin.directory.DirectoryRequest;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;

public class DirectoryAPI {

	public static java.util.List<String> SCOPES = Arrays
			.asList(DirectoryScopes.ADMIN_DIRECTORY_GROUP);

	private Directory directory;

	private static final Random randomGenerator = new Random();

	DirectoryAPI(Directory directory) {
		this.directory = directory;
	}

	public Group create(Group group) {
		Insert insert;
		try {
			insert = directory.groups().insert(group);
			return (Group) execute(insert);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Group update(String groupKey, Group group) {
		try {
			Update update = directory.groups().update(groupKey, group);
			return (Group) execute(update);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void delete(String groupKey) {
		try {
			Delete delete = directory.groups().delete(groupKey);
			execute(delete);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Group getGroup(String groupKey) {
		try {
			Get get = directory.groups().get(groupKey);
			return (Group) execute(get);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Groups getGroupsByCustomer(String customer) {
		try {
			List list = directory.groups().list().setCustomer(customer);
			return (Groups) execute(list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Groups getGroupsByDomain(String domain) {
		try {
			List list = directory.groups().list().setDomain(domain);
			return (Groups) execute(list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addMemberGroup(Group group, Member member) {
		try {
			com.google.api.services.admin.directory.Directory.Members.Insert insert = directory
					.members().insert(group.getEmail(), member);
			execute(insert);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void deleteMemberGroup(Group group, String membersEmail) {
		try {
			com.google.api.services.admin.directory.Directory.Members.Delete delete = directory
					.members().delete(group.getEmail(), membersEmail);
			execute(delete);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Member getMemberGroup(Group group, String membersEmail) {
		try {
			com.google.api.services.admin.directory.Directory.Members.Get get = directory
					.members().get(group.getEmail(), membersEmail);
			return (Member) execute(get);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Members getMembersGroup(Group group) {
		try {
			com.google.api.services.admin.directory.Directory.Members.List list = directory
					.members().list(group.getEmail());
			return (Members) execute(list);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private static Object execute(DirectoryRequest request) throws IOException {
		return execute(request, 1);
	}

	@SuppressWarnings("rawtypes")
	private static Object execute(DirectoryRequest request, int interval)
			throws IOException {
		try {
			return request.execute();
		} catch (GoogleJsonResponseException ex) {
			if (interval == 7) {
				throw ex;
			} else {
				if (handleGoogleJsonResponseException(ex, interval)) {
					return null;
				} else {
					return execute(request, ++interval);
				}
			}
		} catch (IOException e) {
			if (interval == 7) {
				throw e;
			} else {
				return execute(request, ++interval);
			}
		}
	}

	private static boolean handleGoogleJsonResponseException(
			GoogleJsonResponseException ex, int interval)
			throws GoogleJsonResponseException {
		final GoogleJsonError e = ex.getDetails();
		switch (e.getCode()) {
		case 403:
			String reason1 = e.getErrors().get(0).getReason();
			if (reason1.equals("rateLimitExceeded")
					|| reason1.equals("userRateLimitExceeded")) {
				try {
					Thread.sleep((1 << interval) * 1000
							+ randomGenerator.nextInt(1001));
				} catch (InterruptedException ie) {
				}
			}
			break;

		case 404:
			return true;

		case 503:
			String reason2 = e.getErrors().get(0).getReason();
			if (reason2.equals("backendError")) {
				try {
					Thread.sleep((1 << interval) * 1000
							+ randomGenerator.nextInt(1001));
				} catch (InterruptedException ie) {
				}
			}
			break;

		default:
			throw ex;
		}
		return false;
	}
}
