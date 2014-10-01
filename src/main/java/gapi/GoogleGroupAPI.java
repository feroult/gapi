package gapi;

import java.io.IOException;
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
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;

public class GoogleGroupAPI {
	private Directory directory;
	private static final Random randomGenerator = new Random();

	GoogleGroupAPI(Directory directory) {
		this.directory = directory;
	}

	public Group create(Group group) throws IOException {
		Insert insert = directory.groups().insert(group);
		return (Group) execute(insert);
	}

	public Group update(String groupKey, Group group) throws IOException {
		Update update = directory.groups().update(groupKey, group);
		return (Group) execute(update);
	}

	public void delete(String groupKey) throws IOException {
		Delete delete = directory.groups().delete(groupKey);
		execute(delete);
	}

	public Group getGroup(String groupKey) throws IOException {
		Get get = directory.groups().get(groupKey);
		return (Group) execute(get);
	}

	public Groups getGroups() throws IOException {
		List list = directory.groups().list().setCustomer("my_customer");
		return (Groups) execute(list);
	}

	public void addMemberGroup(Group group, Member member) throws IOException {
		com.google.api.services.admin.directory.Directory.Members.Insert insert = directory.members().insert(group.getEmail(),
		        member);
		execute(insert);
	}

	public void deleteMemberGroup(Group group, String membersEmail) throws IOException {
		com.google.api.services.admin.directory.Directory.Members.Delete delete = directory.members().delete(group.getEmail(),
		        membersEmail);
		execute(delete);
	}

	public Member getMemberGroup(Group group, String membersEmail) throws IOException {
		com.google.api.services.admin.directory.Directory.Members.Get get = directory.members().get(group.getEmail(),
		        membersEmail);
		return (Member) execute(get);
	}

	public Members getMembersGroup(Group group) throws IOException {
		com.google.api.services.admin.directory.Directory.Members.List list = directory.members().list(group.getEmail());
		return (Members) execute(list);
	}

	private static Object execute(DirectoryRequest request) throws IOException {
		return execute(request, 1);
	}

	private static Object execute(DirectoryRequest request, int interval) throws IOException {
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

	private static boolean handleGoogleJsonResponseException(GoogleJsonResponseException ex, int interval)
	        throws GoogleJsonResponseException {
		final GoogleJsonError e = ex.getDetails();
		switch (e.getCode()) {
		case 403:
			String reason1 = e.getErrors().get(0).getReason();
			if (reason1.equals("rateLimitExceeded") || reason1.equals("userRateLimitExceeded")) {
				try {
					Thread.sleep((1 << interval) * 1000 + randomGenerator.nextInt(1001));
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
					Thread.sleep((1 << interval) * 1000 + randomGenerator.nextInt(1001));
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
