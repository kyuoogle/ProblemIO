package com.problemio.quiz.service;

import com.problemio.follow.mapper.FollowMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.question.domain.Question;
import com.problemio.question.domain.QuestionAnswer;
import com.problemio.question.dto.AnswerCreateRequest;
import com.problemio.question.dto.QuestionAnswerDto;
import com.problemio.question.dto.QuestionCreateRequest;
import com.problemio.question.dto.QuestionResponse;
import com.problemio.question.mapper.QuestionAnswerMapper;
import com.problemio.question.mapper.QuestionMapper;
import com.problemio.quiz.domain.Quiz;
import com.problemio.quiz.dto.QuizCreateRequest;
import com.problemio.quiz.dto.QuizResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.dto.QuizUpdateRequest;
import com.problemio.quiz.mapper.QuizLikeMapper;
import com.problemio.quiz.mapper.QuizMapper;
import com.problemio.submission.mapper.SubmissionDetailMapper;
import com.problemio.submission.mapper.SubmissionMapper;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    // 퀴즈 기본 정보 CRUD 및 검색을 담당하는 매퍼
    private final QuizMapper quizMapper;
    // 퀴즈 좋아요(Like) 관련 매퍼
    private final QuizLikeMapper quizLikeMapper;
    // 문제(Question) 관련 매퍼
    private final QuestionMapper questionMapper;
    // 문제 정답(QuestionAnswer) 관련 매퍼
    private final QuestionAnswerMapper questionAnswerMapper;
    // 유저 정보 조회 매퍼
    private final UserMapper userMapper;
    // 팔로우 관계 조회 매퍼
    private final FollowMapper followMapper;
    // 제출(Submission) 관련 매퍼
    private final SubmissionMapper submissionMapper;
    // 제출 상세(SubmissionDetail) 관련 매퍼
    private final SubmissionDetailMapper submissionDetailMapper;

    /**
     * 퀴즈 목록 조회 (페이징 + 정렬 + 검색)
     * - page, size를 기반으로 offset 계산
     * - 정렬 기준(sort)과 검색 키워드(keyword)를 이용해 퀴즈 목록 조회
     * - QuizSummaryDto 리스트와 페이지 정보(totalPages, totalElements) 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getQuizzes(int page, int size, String sort, String keyword) {
        int safePage = Math.max(page, 1);   // 최소 1페이지 보장
        int safeSize = Math.max(size, 1);   // 최소 1건 이상 보장
        int offset = (safePage - 1) * safeSize;

        // 조건에 맞는 퀴즈 목록 조회
        List<Quiz> quizzes = quizMapper.searchQuizzes(offset, safeSize, sort, keyword);
        // 전체 개수 조회
        int total = quizMapper.countQuizzes(keyword);

        // Quiz 엔티티를 요약 DTO로 변환
        List<QuizSummaryDto> content = quizzes.stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) total / safeSize);

        // 목록 + 페이징 정보 응답
        return Map.of(
                "content", content,
                "totalPages", totalPages,
                "totalElements", total
        );
    }

    /**
     * 퀴즈 생성
     * - QuizCreateRequest로부터 Quiz 엔티티 생성
     * - 기본값(좋아요 수, 플레이 수) 0으로 초기화
     * - 퀴즈 저장 후 관련 문제/정답까지 함께 저장
     */
    @Override
    public QuizResponse createQuiz(Long userId, QuizCreateRequest request) {
        Quiz quiz = new Quiz();
        quiz.setUserId(userId);
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setThumbnailUrl(request.getThumbnailUrl());
        quiz.setPublic(request.isPublic());
        quiz.setLikeCount(0);
        quiz.setPlayCount(0);

        // 퀴즈 기본 정보 저장
        quizMapper.insertQuiz(quiz);
        // 하위 문제/정답 저장
        saveQuestions(quiz.getId(), request.getQuestions());

        // 방금 생성한 퀴즈에 대한 응답 DTO 구성 (질문/작성자 정보는 지금은 null)
        return buildQuizResponse(quiz, null, null, null);
    }

    /**
     * 퀴즈 수정 (전체/부분 공용)
     * - 작성자 본인인지 검증
     * - 요청에 들어온 필드만 업데이트
     * - 질문 정보가 들어온 경우, 기존 질문/정답 전체 삭제 후 재생성
     */
    @Override
    public QuizResponse updateQuiz(Long userId, Long quizId, QuizUpdateRequest request) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 작성자 검증
        if (!Objects.equals(quiz.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // null이 아닌 필드들만 선택적으로 수정
        if (request.getTitle() != null) {
            quiz.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            quiz.setDescription(request.getDescription());
        }
        if (request.getThumbnailUrl() != null) {
            quiz.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsPublic() != null) {
            quiz.setPublic(request.getIsPublic());
        }

        // 퀴즈 기본 정보 업데이트
        quizMapper.updateQuiz(quiz);

        // 질문 리스트가 넘어온 경우 기존 질문/정답 전부 삭제 후 새로 저장
        if (request.getQuestions() != null) {
            List<Question> existing = questionMapper.findByQuizId(quizId);
            // 각 질문에 연결된 정답 삭제
            for (Question question : existing) {
                questionAnswerMapper.deleteByQuestionId(question.getId());
            }
            // 질문 목록 삭제
            questionMapper.deleteByQuizId(quizId);
            // 요청 기반으로 질문/정답 다시 저장
            saveQuestions(quizId, request.getQuestions());
        }

        // 수정된 퀴즈 + 질문 목록 + 작성자 정보 포함한 응답
        return buildQuizResponse(quiz, loadQuestions(quizId), findAuthor(quiz.getUserId()), null);
    }

    /**
     * 퀴즈 삭제
     * - 작성자 본인인지 확인
     * - 퀴즈에 속한 모든 질문/정답 삭제 후 퀴즈 삭제
     */
    @Override
    public void deleteQuiz(Long userId, Long quizId) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 작성자 검증
        if (!Objects.equals(quiz.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 관련 좋아요 제거
        quizLikeMapper.deleteByQuizId(quizId);

        // 제출 기록 선삭제 (FK 충돌 방지)
        List<Question> questions = questionMapper.findByQuizId(quizId);
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .collect(Collectors.toList());
        if (!questionIds.isEmpty()) {
            submissionDetailMapper.deleteByQuestionIds(questionIds);
        }
        submissionMapper.deleteByQuizId(quizId);

        // 하위 질문/정답 삭제
        for (Question question : questions) {
            questionAnswerMapper.deleteByQuestionId(question.getId());
        }
        questionMapper.deleteByQuizId(quizId);

        // 퀴즈 삭제
        quizMapper.deleteQuiz(quizId);
    }

    /**
     * 퀴즈 단건 조회
     * - 퀴즈 정보 + 질문/정답 목록 + 작성자 정보 조회
     * - viewerId가 있는 경우
     *   - 내가 좋아요를 눌렀는지
     *   - 내가 작성자를 팔로우하고 있는지
     *   여부를 함께 반환
     */
    @Override
    @Transactional
    public QuizResponse getQuiz(Long quizId, Long viewerId) {
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 조회 시 조회수 1 증가
        quizMapper.incrementPlayCount(quizId);
        quiz.setPlayCount(quiz.getPlayCount() + 1);

        UserResponse author = findAuthor(quiz.getUserId());
        List<QuestionResponse> questions = loadQuestions(quizId);

        Boolean isLikedByMe = null;
        Boolean isFollowedByMe = null;

        // 로그인한 사용자일 때만 좋아요/팔로우 여부 계산
        if (viewerId != null) {
            isLikedByMe = quizLikeMapper.findByUserIdAndQuizId(viewerId, quizId).isPresent();
            isFollowedByMe = followMapper.exists(viewerId, quiz.getUserId()) > 0;
        }

        return buildQuizResponse(quiz, questions, author, isLikedByMe, isFollowedByMe);
    }

    /**
     * 공개 퀴즈 목록 조회
     * - 공개 상태인 퀴즈만 조회
     * - 목록용 요약 DTO로 변환
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDto> getPublicQuizzes() {
        return quizMapper.findPublicQuizzes().stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 유저가 만든 퀴즈 목록 조회
     * - 제작자 프로필 화면에서 사용
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDto> getUserQuizzes(Long userId) {
        return quizMapper.findQuizzesByUserId(userId).stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * 퀴즈 좋아요 등록
     * - 퀴즈 존재 여부 확인
     * - 이미 좋아요 했으면 무시
     * - 좋아요 insert 후 likeCount 증가
     */
    @Override
    public void likeQuiz(Long userId, Long quizId) {
        // 퀴즈 조회
        Quiz quiz = quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 자기 퀴즈면 좋아요 금지
        if (Objects.equals(quiz.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.CANNOT_LIKE_OWN_QUIZ);
        }

        // 이미 좋아요를 눌렀는지 체크
        boolean alreadyLiked = quizLikeMapper.findByUserIdAndQuizId(userId, quizId).isPresent();
        if (alreadyLiked) {
            return; // 중복 좋아요 방지
        }

        // 좋아요 레코드 생성 후 카운트 증가
        quizLikeMapper.insertQuizLike(buildQuizLike(userId, quizId));
        quizMapper.incrementLikeCount(quizId);
    }

    /**
     * 퀴즈 좋아요 취소
     * - 퀴즈 존재 여부 확인
     * - 좋아요 레코드 삭제 후 likeCount 감소
     */
    @Override
    public void unlikeQuiz(Long userId, Long quizId) {
        quizMapper.findById(quizId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        quizLikeMapper.deleteQuizLike(userId, quizId);
        quizMapper.decrementLikeCount(quizId);
    }

    /**
     * Quiz 엔티티 → QuizSummaryDto 변환 공통 헬퍼
     */
    private QuizSummaryDto toSummaryDto(Quiz quiz) {
        return QuizSummaryDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .thumbnailUrl(quiz.getThumbnailUrl())
                .likeCount(quiz.getLikeCount())
                .playCount(quiz.getPlayCount())
                .build();
    }

    /**
     * QuizResponse를 생성하는 헬퍼 메서드 (isFollowedByMe 없는 버전)
     */
    private QuizResponse buildQuizResponse(Quiz quiz,
                                           List<QuestionResponse> questions,
                                           UserResponse author,
                                           Boolean isLikedByMe) {
        return buildQuizResponse(quiz, questions, author, isLikedByMe, null);
    }

    /**
     * QuizResponse를 생성하는 공통 헬퍼 메서드
     * - 퀴즈 기본 정보 + 질문 목록 + 작성자 정보 + 좋아요/팔로우 여부를 포함한 응답 DTO 생성
     */
    private QuizResponse buildQuizResponse(Quiz quiz,
                                           List<QuestionResponse> questions,
                                           UserResponse author,
                                           Boolean isLikedByMe,
                                           Boolean isFollowedByMe) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .userId(quiz.getUserId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .thumbnailUrl(quiz.getThumbnailUrl())
                .isPublic(quiz.isPublic())
                .likeCount(quiz.getLikeCount())
                .playCount(quiz.getPlayCount())
                .questions(questions)
                .author(author)
                .isLikedByMe(isLikedByMe)
                .isFollowedByMe(isFollowedByMe)
                .build();
    }

    /**
     * 퀴즈에 속한 질문/정답들을 저장하는 헬퍼 메서드
     * - 질문 순서(order)가 없으면 index 기반으로 자동 부여
     */
    private void saveQuestions(Long quizId, List<QuestionCreateRequest> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        int index = 1;
        for (QuestionCreateRequest request : questions) {
            Question question = new Question();
            question.setQuizId(quizId);
            // 요청에 order가 없으면 index 값 사용
            int order = request.getQuestionOrder() != null ? request.getQuestionOrder() : index;
            question.setQuestionOrder(order);
            question.setImageUrl(request.getImageUrl());
            question.setDescription(request.getDescription());

            // 질문 저장
            questionMapper.insertQuestion(question);

            // 질문에 대한 정답 목록 저장
            saveAnswers(question.getId(), request.getAnswers());
            index++;
        }
    }

    /**
     * 하나의 질문에 대한 정답 목록 저장 헬퍼 메서드
     * - answerText가 비어있으면 저장하지 않음
     * - 정렬 순서(sortOrder)가 없으면 idx 기반으로 자동 부여
     */
    private void saveAnswers(Long questionId, List<AnswerCreateRequest> answers) {
        if (answers == null || answers.isEmpty()) {
            return;
        }

        int idx = 1;
        for (AnswerCreateRequest answerRequest : answers) {
            // 빈 문자열 정답은 무시
            if (answerRequest.getAnswerText() == null || answerRequest.getAnswerText().isBlank()) {
                continue;
            }

            QuestionAnswer answer = new QuestionAnswer();
            answer.setQuestionId(questionId);
            answer.setAnswerText(answerRequest.getAnswerText());
            // sortOrder가 없으면 idx 사용
            answer.setSortOrder(answerRequest.getSortOrder() != null ? answerRequest.getSortOrder() : idx);

            // 정답 저장
            questionAnswerMapper.insertAnswer(answer);
            idx++;
        }
    }

    /**
     * 퀴즈에 연결된 질문/정답 전체를 로드하는 헬퍼 메서드
     * - Question 엔티티 리스트를 QuestionResponse DTO 리스트로 변환
     */
    private List<QuestionResponse> loadQuestions(Long quizId) {
        List<Question> questions = questionMapper.findByQuizId(quizId);
        return questions.stream()
                .map(q -> QuestionResponse.builder()
                        .id(q.getId())
                        .order(q.getQuestionOrder())
                        .description(q.getDescription())
                        .imageUrl(q.getImageUrl())
                        .answers(loadAnswers(q.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 하나의 질문에 대한 정답 목록을 로드하는 헬퍼 메서드
     * - QuestionAnswer 엔티티를 QuestionAnswerDto로 변환
     */
    private List<QuestionAnswerDto> loadAnswers(Long questionId) {
        List<QuestionAnswer> answers = questionAnswerMapper.findByQuestionId(questionId);
        return answers.stream()
                .map(a -> {
                    QuestionAnswerDto dto = new QuestionAnswerDto();
                    dto.setId(a.getId());
                    dto.setAnswerText(a.getAnswerText());
                    dto.setSortOrder(a.getSortOrder());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 작성자 정보(UserResponse)를 조회하는 헬퍼 메서드
     * - 없으면 null 반환
     */
    private UserResponse findAuthor(Long userId) {
        return userMapper.findById(userId).orElse(null);
    }

    /**
     * QuizLike 엔티티를 생성하는 헬퍼 메서드
     * - userId, quizId만 세팅
     */
    private com.problemio.quiz.domain.QuizLike buildQuizLike(Long userId, Long quizId) {
        com.problemio.quiz.domain.QuizLike like = new com.problemio.quiz.domain.QuizLike();
        like.setUserId(userId);
        like.setQuizId(quizId);
        return like;
    }

    /*
     * 마이 페이지 내 좋아요한 퀴즈, 유저가 팔로우하는 유저의 퀴즈 목록 조회 위한 메서드
     */
    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDto> getQuizzesOfFollowings(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return quizMapper.findQuizzesOfFollowings(userId, offset, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSummaryDto> getLikedQuizzes(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return quizMapper.findLikedQuizzesByUser(userId, offset, size);
    }
}
