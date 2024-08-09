package com.woohaengshi.backend.dto.response.studyrecord;

import com.woohaengshi.backend.dto.response.subject.ShowSubjectsResponse;

import lombok.Getter;

import java.util.List;

@Getter
public class ShowTimerResponse {

    private int time;

    private List<ShowSubjectsResponse> subjects;

    private ShowTimerResponse(int time, List<ShowSubjectsResponse> subjects) {
        this.time = time;
        this.subjects = subjects;
    }

    public static ShowTimerResponse of(int time, List<ShowSubjectsResponse> subjects) {
        return new ShowTimerResponse(time, subjects);
    }
}
