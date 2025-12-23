package com.problemio.admin.service;

import com.problemio.challenge.domain.Challenge;
import com.problemio.challenge.dto.ChallengeCreateRequest;
import com.problemio.challenge.mapper.ChallengeMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.domain.Quiz;
import com.problemio.quiz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final QuizMapper quizMapper;
    private final ChallengeMapper challengeMapper;

    @Transactional(readOnly = true)
    public List<Quiz> findAdminQuizzes(int page, int size, String keyword) {
        int offset = (Math.max(page, 1) - 1) * size;
        return quizMapper.findAdminQuizzes(offset, size, keyword);
    }

    @Transactional(readOnly = true)
    public int countAdminQuizzes(String keyword) {
        return quizMapper.countAdminQuizzes(keyword);
    }

    @Transactional
    public void toggleQuizVisibility(Long quizId) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        // Toggle isHidden
        quiz.setHidden(!quiz.isHidden());
        
        // Update quiz
        // Note: updateQuiz updates all fields. Ensure mapping is correct.
        // If we want to update ONLY isHidden, we might want a specific update method, 
        // but using updateQuiz is fine if object is fully populated.
        quizMapper.updateQuiz(quiz);
    }

    @Transactional
    public void createChallenge(ChallengeCreateRequest request) {
        // Validation: Check if quiz exists
        if (quizMapper.findById(request.getTargetQuizId()).isEmpty()) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_FOUND);
        }

        Challenge challenge = request.toEntity();
        LocalDateTime now = LocalDateTime.now();
        challenge.setCreatedAt(now);
        challenge.setUpdatedAt(now);
        challengeMapper.insert(challenge);
    }
}
