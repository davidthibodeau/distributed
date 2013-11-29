package serversrc.resImpl;

import java.io.Serializable;

public enum Crash implements Serializable {
	NO_CRASH, BEFORE_VOTE, BEFORE_REPLY, DURING_REPLY, BEFORE_DECISION, BEFORE_DECISION_SENT, DURING_DECISION_SEND,
	AFTER_DECISIONS
}
