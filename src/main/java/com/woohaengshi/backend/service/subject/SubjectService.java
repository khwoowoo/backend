package com.woohaengshi.backend.service.subject;

import com.woohaengshi.backend.dto.response.studyrecord.FindTimerResponse;

public interface SubjectService {
    FindTimerResponse findTimer(Long memberId);
}
