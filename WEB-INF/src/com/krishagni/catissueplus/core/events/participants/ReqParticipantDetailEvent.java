
package com.krishagni.catissueplus.core.events.participants;

import com.krishagni.catissueplus.core.events.RequestEvent;

public class ReqParticipantDetailEvent extends RequestEvent {

	private Long participantId;

	public Long getParticipantId() {
		return participantId;
	}

	public void setParticipantId(Long participantId) {
		this.participantId = participantId;
	}
}
