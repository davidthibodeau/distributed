package serversrc.resImpl;

import java.io.Serializable;

public enum Crash implements Serializable {
	NO_CRASH, BEFORE_VOTE, BEFORE_REPLIES, BEFORE_ALL_REPLIES, BEFORE_DECISION, BEFORE_DECISION_SENT, BEFORE_ALL_DECISION_SENT,
	AFTER_DECISIONS
}
