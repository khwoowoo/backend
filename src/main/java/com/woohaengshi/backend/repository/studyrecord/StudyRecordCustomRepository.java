package com.woohaengshi.backend.repository.studyrecord;

import com.woohaengshi.backend.domain.StudyRecord;
import com.woohaengshi.backend.dto.result.ShowCalendarResult;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDate;
import java.util.List;

public interface StudyRecordCustomRepository {

    List<ShowCalendarResult> findStudyRecordInCalendar(int year, int month, Long memberId);

    public Slice<StudyRecord> findStudyRecordsByDateSortedByTimeDesc(
            LocalDate date, Pageable pageable);
}
